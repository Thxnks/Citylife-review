package com.citylife.rag;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * RAG 检索增强生成服务。
 * <p>
 * 增强管线（search）：
 * MQE//HyDE 并行 → 多路检索并发 → 合并去重 → LLM 重排序 → 观点标注
 * <p>
 * 工程化保障：并行执行、超时控制、配置开关、热门查询缓存、每步可降级。
 */
@Slf4j
@Service
public class RAGService {

    private static final int CANDIDATE_POOL_MULTIPLIER = 3;
    private static final int MQE_EXPANSIONS = 2;

    private final VectorStore vectorStore;
    private final QueryExpander queryExpander;
    private final ResultReRanker resultReRanker;
    private final RagProperties ragProperties;
    private final ExecutorService ragExecutor;
    private final StringRedisTemplate stringRedisTemplate;

    public RAGService(VectorStore vectorStore,
                      QueryExpander queryExpander,
                      ResultReRanker resultReRanker,
                      RagProperties ragProperties,
                      ExecutorService ragExecutor,
                      StringRedisTemplate stringRedisTemplate) {
        this.vectorStore = vectorStore;
        this.queryExpander = queryExpander;
        this.resultReRanker = resultReRanker;
        this.ragProperties = ragProperties;
        this.ragExecutor = ragExecutor;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 增强检索管线。
     * MQE 和 HyDE 并行调用，四路检索并发执行，整体超时保护，热门查询缓存。
     */
    public List<RetrievalResult> search(String query, int topK, String sourceType) {
        long start = System.currentTimeMillis();

        // Step 1+2: MQE + HyDE 并行
        List<String> expansions = List.of();
        String hydeText = null;

        if (ragProperties.isMqeEnabled() || ragProperties.isHydeEnabled()) {
            CompletableFuture<List<String>> mqeFuture = ragProperties.isMqeEnabled()
                    ? CompletableFuture.supplyAsync(() -> expandWithCache(query), ragExecutor)
                    : CompletableFuture.completedFuture(List.of());

            CompletableFuture<String> hydeFuture = ragProperties.isHydeEnabled()
                    ? CompletableFuture.supplyAsync(() -> hydeWithCache(query), ragExecutor)
                    : CompletableFuture.completedFuture(null);

            try {
                expansions = mqeFuture.get(ragProperties.getTimeoutSeconds(), TimeUnit.SECONDS);
                hydeText = hydeFuture.get(ragProperties.getTimeoutSeconds(), TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                log.warn("MQE/HyDE timed out after {}s, using partial results", ragProperties.getTimeoutSeconds());
                mqeFuture.cancel(true);
                hydeFuture.cancel(true);
            } catch (Exception e) {
                log.warn("MQE/HyDE parallel execution failed", e);
            }
            log.debug("MQE→{} variants, HyDE→{} chars, took {}ms",
                    expansions.size(),
                    hydeText != null ? hydeText.length() : 0,
                    System.currentTimeMillis() - start);
        }

        // Step 3: 构建查询集合
        List<String> allQueries = new ArrayList<>();
        allQueries.add(query);
        allQueries.addAll(expansions);
        if (hydeText != null && !hydeText.isBlank()) {
            allQueries.add(hydeText);
        }

        // Step 4: 多路检索并发 + 候选池扩大
        int perQueryLimit = Math.max(
                topK * CANDIDATE_POOL_MULTIPLIER / Math.max(1, allQueries.size()),
                topK);

        Map<String, RetrievalResult> dedup = new LinkedHashMap<>();
        List<CompletableFuture<Void>> searchFutures = new ArrayList<>();

        for (String q : allQueries) {
            searchFutures.add(CompletableFuture.runAsync(() -> {
                try {
                    List<Document> docs = vectorStore.similaritySearch(
                            SearchRequest.builder().query(q).topK(perQueryLimit).build());

                    List<Document> filtered = docs.stream()
                            .filter(d -> sourceType == null
                                    || sourceType.equals(d.getMetadata().get("sourceType")))
                            .toList();

                    synchronized (dedup) {
                        for (int i = 0; i < filtered.size(); i++) {
                            Document doc = filtered.get(i);
                            String docId = (String) doc.getMetadata().get("documentId");
                            if (docId == null) {
                                docId = doc.getId();
                            }
                            double distScore = computeScore(doc, i);
                            if (!dedup.containsKey(docId) || distScore > dedup.get(docId).score) {
                                dedup.put(docId, new RetrievalResult(doc, distScore, 0, null, null));
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("检索查询 '{}' 失败，跳过", q, e);
                }
            }, ragExecutor));
        }

        try {
            CompletableFuture.allOf(searchFutures.toArray(new CompletableFuture[0]))
                    .get(ragProperties.getTimeoutSeconds(), TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            log.warn("多路检索超时 {}s，使用已返回的部分结果", ragProperties.getTimeoutSeconds());
        } catch (Exception e) {
            log.warn("多路检索并行执行失败", e);
        }

        List<RetrievalResult> candidates = new ArrayList<>(dedup.values());
        log.debug("合并去重 {} 条候选，检索耗时 {}ms",
                candidates.size(), System.currentTimeMillis() - start);

        if (candidates.isEmpty()) {
            return List.of();
        }

        // Step 5: LLM 重排序（可关闭）
        List<RetrievalResult> ranked;
        if (ragProperties.isRerankEnabled()) {
            List<ResultReRanker.RankedResult> reranked = resultReRanker.rerank(query, candidates, topK);
            ranked = new ArrayList<>();
            for (int i = 0; i < reranked.size(); i++) {
                ResultReRanker.RankedResult rr = reranked.get(i);
                ranked.add(new RetrievalResult(
                        rr.result().document, rr.relevanceScore(), i + 1,
                        rr.sentiment(), rr.reason()));
            }
        } else {
            // 关闭重排序时直接按向量分数取 topK
            List<RetrievalResult> byScore = candidates.stream()
                    .sorted((a, b) -> Double.compare(b.score, a.score))
                    .limit(topK)
                    .collect(Collectors.toList());
            ranked = new ArrayList<>();
            for (int i = 0; i < byScore.size(); i++) {
                RetrievalResult r = byScore.get(i);
                ranked.add(new RetrievalResult(r.document, r.score, i + 1, null, null));
            }
        }

        log.debug("RAG 总耗时 {}ms，返回 {} 条", System.currentTimeMillis() - start, ranked.size());
        return ranked;
    }

    /**
     * 基础检索（无增强），用于评估对比。
     */
    public List<RetrievalResult> searchBasic(String query, int topK, String sourceType) {
        List<Document> docs = vectorStore.similaritySearch(
                SearchRequest.builder().query(query).topK(topK * 2).build());

        List<Document> filtered = docs.stream()
                .filter(d -> sourceType == null
                        || sourceType.equals(d.getMetadata().get("sourceType")))
                .limit(topK)
                .collect(Collectors.toList());

        List<RetrievalResult> output = new ArrayList<>();
        for (int i = 0; i < filtered.size(); i++) {
            output.add(new RetrievalResult(
                    filtered.get(i), computeScore(filtered.get(i), i), i + 1, null, null));
        }
        return output;
    }

    private List<String> expandWithCache(String query) {
        if (ragProperties.isCacheEnabled()) {
            String cacheKey = "rag:mqe:" + sha256(query);
            String cached = stringRedisTemplate.opsForValue().get(cacheKey);
            if (StrUtil.isNotBlank(cached)) {
                log.debug("MQE 缓存命中");
                return JSONUtil.toList(cached, String.class);
            }
            List<String> result = queryExpander.expandQuery(query, MQE_EXPANSIONS);
            if (!result.isEmpty()) {
                stringRedisTemplate.opsForValue().set(cacheKey, JSONUtil.toJsonStr(result),
                        ragProperties.getCacheTtlMinutes(), TimeUnit.MINUTES);
            }
            return result;
        }
        return queryExpander.expandQuery(query, MQE_EXPANSIONS);
    }

    private String hydeWithCache(String query) {
        if (ragProperties.isCacheEnabled()) {
            String cacheKey = "rag:hyde:" + sha256(query);
            String cached = stringRedisTemplate.opsForValue().get(cacheKey);
            if (StrUtil.isNotBlank(cached)) {
                log.debug("HyDE 缓存命中");
                return cached;
            }
            String result = queryExpander.generateHyde(query);
            if (result != null) {
                stringRedisTemplate.opsForValue().set(cacheKey, result,
                        ragProperties.getCacheTtlMinutes(), TimeUnit.MINUTES);
            }
            return result;
        }
        return queryExpander.generateHyde(query);
    }

    /**
     * 构建注入 Agent 上下文的格式化文本。
     */
    public String buildContext(List<RetrievalResult> results, double minScore) {
        List<RetrievalResult> filtered = results.stream()
                .filter(r -> r.score >= minScore)
                .collect(Collectors.toList());

        if (filtered.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("以下是从用户点评中检索到的相关信息：\n\n");
        for (RetrievalResult r : filtered) {
            Document doc = r.document;
            sb.append("--- [").append(r.citationIndex).append("] ");
            Object headingPath = doc.getMetadata().get("headingPath");
            if (headingPath != null && !headingPath.toString().isEmpty()) {
                sb.append("(").append(headingPath).append(") ");
            }
            if (r.sentiment != null) {
                sb.append("[").append(sentimentLabel(r.sentiment)).append("] ");
            }
            sb.append("---\n");
            sb.append(doc.getText()).append("\n");
            sb.append("(来源: blogId=").append(doc.getMetadata().get("documentId"))
                    .append(", 相似度: ").append(String.format("%.3f", r.score)).append(")\n\n");
        }
        return sb.toString();
    }

    public int getStoreSize() {
        try {
            return (int) vectorStore.similaritySearch(
                    SearchRequest.builder().query("").topK(1000).build()).size();
        } catch (Exception e) {
            return 0;
        }
    }

    private double computeScore(Document doc, int rank) {
        Object distObj = doc.getMetadata().get("distance");
        if (distObj instanceof Number) {
            return ((Number) distObj).doubleValue();
        }
        return 1.0 - rank * 0.05;
    }

    private String sentimentLabel(String sentiment) {
        if (sentiment == null) {
            return "中性";
        }
        return switch (sentiment) {
            case "positive" -> "好评";
            case "negative" -> "差评";
            default -> "中性";
        };
    }

    private static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            return Integer.toHexString(input.hashCode());
        }
    }

    public static class RetrievalResult {
        public final Document document;
        public final double score;
        public final int citationIndex;
        public final String sentiment;
        public final String rankReason;

        public RetrievalResult(Document document, double score, int citationIndex,
                               String sentiment, String rankReason) {
            this.document = document;
            this.score = score;
            this.citationIndex = citationIndex;
            this.sentiment = sentiment;
            this.rankReason = rankReason;
        }
    }
}
