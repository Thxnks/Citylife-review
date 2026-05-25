package com.citylife.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 查询扩展器：MQE（多查询扩展）+ HyDE（假设文档嵌入）。
 * 针对本地生活点评场景——用户口语查询与点评文本之间存在语义鸿沟，
 * 通过多角度扩展和假设答案桥接来提升召回质量。
 */
@Slf4j
@Component
public class QueryExpander {

    private final ChatClient chatClient;

    public QueryExpander(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    private static final String MQE_PROMPT = """
            你是一个本地生活点评平台的搜索优化助手。
            用户输入了一句口语化的查询，请将其改写成 %d 个更适合在点评文本中做语义检索的查询变体。

            改写规则：
            1. 将口语表达转换为书面点评中常见的表达方式
               - "贵不贵" → "人均消费价格 性价比"
               - "好不好吃" → "口味 菜品质量 推荐菜"
               - "适合约会吗" → "环境氛围 私密 浪漫 适合两个人"
               - "排队长不长" → "排队等候时间 是否需要等位"
               - "服务怎么样" → "服务员态度 上菜速度 服务体验"
            2. 每个变体从不同角度描述同一需求
            3. 保留原始意图，不要引入无关概念
            4. 每个变体不超过20个字

            原始查询：%s

            请只返回 %d 行，每行一个查询变体，不要编号，不要其他内容。""";

    /**
     * MQE：将用户口语化查询扩展为多个标准检索变体。
     * 失败时降级返回空列表，不影响主流程。
     */
    public List<String> expandQuery(String query, int numExpansions) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        String prompt = String.format(MQE_PROMPT, numExpansions, query, numExpansions);

        try {
            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            if (response == null || response.isBlank()) {
                return List.of();
            }

            return Arrays.stream(response.strip().split("\n"))
                    .map(String::strip)
                    .filter(s -> !s.isBlank())
                    .limit(numExpansions)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("MQE查询扩展失败，降级使用原始查询", e);
            return List.of();
        }
    }

    private static final String HYDE_PROMPT = """
            你是一个本地生活点评平台的活跃用户。根据下面的查询意图，写一段假设的用户点评（80-150字）。
            这段点评应该像一个真实用户会写的内容，包含具体的感受、细节和评价。
            不需要完全虚构店铺名称，重点描述体验和感受。

            查询意图：%s

            请只返回点评文本，不要加任何前缀或说明。""";

    /**
     * HyDE：生成假设性点评文本。
     * "用答案找答案"——先让 LLM 想象一段理想点评，再用它去搜真实点评，
     * 桥接口语查询和点评文本之间的语义鸿沟。
     * 失败时返回 null，不影响主流程。
     */
    public String generateHyde(String query) {
        if (query == null || query.isBlank()) {
            return null;
        }

        String prompt = String.format(HYDE_PROMPT, query);

        try {
            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            if (response == null || response.isBlank()) {
                return null;
            }
            return response.strip();
        } catch (Exception e) {
            log.warn("HyDE生成失败，跳过", e);
            return null;
        }
    }
}
