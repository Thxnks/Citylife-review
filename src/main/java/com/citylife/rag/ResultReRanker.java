package com.citylife.rag;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * LLM 重排序器。
 * 对向量召回的候选集做语义重排序——向量相似度高不等于回答问题有用。
 * 同时标注每条结果的情感倾向，Agent 推荐时可据此调整话术。
 * <p>
 * 与 ai-code-helper 的规则 HybridRanker（60%向量+40%关键词）形成对比：
 * 那边是规则驱动可解释，这边是 LLM 语义驱动更精准。
 */
@Slf4j
@Component
public class ResultReRanker {

    private final ChatClient chatClient;

    public ResultReRanker(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    /**
     * 对召回候选做 LLM 重排序，返回 topK 条带观点标注的结果。
     * LLM 调用失败时降级为按向量分数排序，不阻断检索管线。
     */
    public List<RankedResult> rerank(String query, List<RAGService.RetrievalResult> candidates, int topK) {
        if (candidates == null || candidates.isEmpty()) {
            return List.of();
        }

        String prompt = buildRerankPrompt(query, candidates, topK);

        try {
            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            if (response == null || response.isBlank()) {
                return fallbackRank(candidates, topK);
            }

            String json = extractJson(response);
            JSONArray arr = JSONUtil.parseArray(json);

            List<RankedResult> results = new ArrayList<>();
            for (int i = 0; i < arr.size() && results.size() < topK; i++) {
                JSONObject obj = arr.getJSONObject(i);
                int index = obj.getInt("index", -1);
                if (index < 0 || index >= candidates.size()) {
                    continue;
                }

                double relevance = obj.getDouble("relevance", candidates.get(index).score);
                String sentiment = obj.getStr("sentiment", "neutral");
                String reason = obj.getStr("reason", "");

                results.add(new RankedResult(
                        candidates.get(index),
                        relevance,
                        sentiment,
                        reason));
            }
            return results;
        } catch (Exception e) {
            log.warn("LLM重排序失败，降级使用向量分数排序", e);
            return fallbackRank(candidates, topK);
        }
    }

    private String buildRerankPrompt(String query, List<RAGService.RetrievalResult> candidates, int topK) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是一个点评检索质量评估员。根据用户查询，判断以下点评片段的相关性。\n\n");
        sb.append("用户查询：").append(query).append("\n\n");
        sb.append("候选点评片段：\n");
        for (int i = 0; i < candidates.size(); i++) {
            var r = candidates.get(i);
            sb.append("--- [").append(i).append("] ---\n");
            String text = r.document.getText();
            // 截断过长文本，节省 token
            if (text != null && text.length() > 300) {
                text = text.substring(0, 300) + "...";
            }
            sb.append("内容：").append(text).append("\n\n");
        }
        sb.append("对每个片段评估：\n");
        sb.append("1. relevance: 与查询的实质相关度 (0.0-1.0)，不是关键词匹配，而是信息是否真的回答了查询\n");
        sb.append("2. sentiment: 情感倾向 (positive/negative/neutral)\n");
        sb.append("3. reason: 一句话判定理由（10字以内）\n\n");
        sb.append("返回紧凑JSON数组，不要markdown代码块：\n");
        sb.append("[{\"index\":0,\"relevance\":0.85,\"sentiment\":\"positive\",\"reason\":\"直接描述了价格水平\"},...]\n\n");
        sb.append("按relevance降序排列，只返回前").append(topK).append("条。");

        return sb.toString();
    }

    /**
     * 降级策略：LLM 不可用时回退到向量分数排序。
     */
    private List<RankedResult> fallbackRank(List<RAGService.RetrievalResult> candidates, int topK) {
        return candidates.stream()
                .sorted((a, b) -> Double.compare(b.score, a.score))
                .limit(topK)
                .map(r -> new RankedResult(r, r.score, "neutral", "向量分数"))
                .toList();
    }

    /**
     * 从 LLM 返回中提取 JSON，处理 markdown 代码块包裹的情况。
     */
    private String extractJson(String response) {
        String trimmed = response.strip();
        if (trimmed.startsWith("```")) {
            int start = trimmed.indexOf("\n");
            int end = trimmed.lastIndexOf("```");
            if (start >= 0 && end > start) {
                trimmed = trimmed.substring(start, end).strip();
            }
        }
        return trimmed;
    }

    /**
     * 重排序结果：原始检索结果 + LLM 评定的相关度、情感倾向和理由。
     */
    public record RankedResult(
            RAGService.RetrievalResult result,
            double relevanceScore,
            String sentiment,
            String reason) {
    }
}
