package com.citylife.agent.tool;

import cn.hutool.json.JSONUtil;
import com.citylife.rag.RAGService;
import com.citylife.rag.RAGService.RetrievalResult;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class ReviewSearchTool {

    private final RAGService ragService;

    @Tool(description = "语义搜索用户点评内容。用自然语言查询在点评中找到相关的用户体验、评价和推荐。" +
            "适合回答'哪家店XXX好''有没有XXX的推荐'等需要真实用户口碑的问题。" +
            "返回相关点评片段及引用编号 [1] [2] ...，附带观点倾向标注（好评/差评/中性）")
    public String searchReviews(
            @ToolParam(description = "语义搜索查询，用自然语言描述想找的内容") String query,
            @ToolParam(description = "返回条数，默认3，最大5") Integer topK) {

        if (query == null || query.isBlank()) {
            return JSONUtil.toJsonStr(Map.of("error", "query不能为空"));
        }

        int k = Math.min(topK != null ? topK : 3, 5);

        List<RetrievalResult> results = ragService.search(query, k, "blog");

        if (results.isEmpty()) {
            return JSONUtil.toJsonStr(Map.of(
                    "message", "没有找到相关的点评内容",
                    "results", Collections.emptyList()
            ));
        }

        List<Map<String, Object>> resultList = new ArrayList<>();
        for (RetrievalResult r : results) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("citation", "[" + r.citationIndex + "]");
            item.put("blogId", r.document.getMetadata().get("documentId"));
            item.put("content", r.document.getText());
            item.put("score", String.format("%.3f", r.score));
            if (r.sentiment != null && !"neutral".equals(r.sentiment)) {
                item.put("sentiment", r.sentiment);
            }
            if (r.rankReason != null && !r.rankReason.isBlank()) {
                item.put("reason", r.rankReason);
            }
            resultList.add(item);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "找到 " + results.size() + " 条相关点评");
        response.put("results", resultList);

        return JSONUtil.toJsonStr(response);
    }
}
