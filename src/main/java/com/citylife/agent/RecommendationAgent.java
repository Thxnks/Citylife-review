package com.citylife.agent;

import com.citylife.agent.tool.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class RecommendationAgent {

    private static final String SYSTEM_PROMPT =
            "你是一个本地生活推荐助手，服务于一个城市生活点评平台。\n\n" +
            "你的能力：\n" +
            "- 搜索店铺（名称、区域、类型、排序）\n" +
            "- 查看店铺优惠券\n" +
            "- 查看店铺的用户点评（按店铺ID精确查询）\n" +
            "- 语义搜索点评内容（用自然语言在全部点评中查找相关体验和推荐）\n" +
            "- 了解当前用户的偏好和画像\n\n" +
            "规则：\n" +
            "1. 当用户要推荐时，先调用getUserProfile了解用户偏好，再调用searchShops搜索候选，必要时调用searchReviews语义搜索相关点评，最后用getShopVouchers和getShopBlogs丰富推荐理由\n" +
            "2. 推荐时说明推荐理由，结合用户偏好、评分、价格、优惠券、点评口碑\n" +
            "3. 如果搜索结果为空，建议放宽条件或换个关键词\n" +
            "4. 回复用自然的对话语气，不要列markdown表格，用分段文字描述\n" +
            "5. 价格单位是分，展示时换成元\n" +
            "6. 当searchReviews返回引用编号[1][2]时，在回答中用同样的编号标注信息来源";

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;

    public RecommendationAgent(ChatClient.Builder builder,
                                ChatMemory chatMemory,
                                BlogTool blogTool,
                                ShopSearchTool shopSearchTool,
                                VoucherTool voucherTool,
                                ReviewSearchTool reviewSearchTool,
                                UserProfileTool userProfileTool) {
        this.chatMemory = chatMemory;
        this.chatClient = builder
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory))
                .defaultTools(blogTool, shopSearchTool, voucherTool, reviewSearchTool, userProfileTool)
                .build();
    }

    public String recommend(String userMessage) {
        return recommend(userMessage, null).getReply();
    }

    public RecommendResult recommend(String userMessage, String sessionId) {
        String conversationId = sessionId != null && !sessionId.isEmpty()
                ? sessionId
                : java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 12);

        try {
            String reply = chatClient.prompt()
                    .user(userMessage)
                    .advisors(a -> a.param("chat_memory_conversation_id", conversationId))
                    .call()
                    .content();

            if (reply == null || reply.isEmpty()) {
                reply = "嗯…我说不出什么有用的东西";
            }
            return new RecommendResult(reply, conversationId);
        } catch (Exception e) {
            log.error("Agent调用失败", e);
            return new RecommendResult("出了点问题，我现在连不上脑子了…等会儿再试吧", conversationId);
        }
    }

    public List<Message> getRecentMessages(String conversationId, int lastN) {
        return chatMemory.get(conversationId, lastN);
    }

    public static class RecommendResult {
        private final String reply;
        private final String conversationId;

        RecommendResult(String reply, String conversationId) {
            this.reply = reply;
            this.conversationId = conversationId;
        }

        public String getReply() { return reply; }
        public String getConversationId() { return conversationId; }
    }
}
