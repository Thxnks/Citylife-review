package com.citylife.agent.memory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class PreferenceExtractor {

    private final ChatClient chatClient;

    private static final String EXTRACT_PROMPT =
            "你是一个用户画像分析助手。根据以下用户与推荐助手的对话历史，提取用户的餐饮偏好。\n\n"
            + "规则：\n"
            + "1. 只提取用户明确表达或强烈暗示的偏好，不要猜测\n"
            + "2. 区分\"用户随口提的\"和\"用户真正喜欢的\"——多次提到或表达喜欢的才是真实偏好\n"
            + "3. 如果对话中缺乏足够信息，字段值设为空数组或null\n"
            + "4. 输出严格JSON格式，不要有任何额外文字\n\n"
            + "输出格式：\n"
            + "{\n"
            + "  \"cuisinePreferences\": [\"菜系名称\"],\n"
            + "  \"budgetLevel\": \"低/中等/高/null\",\n"
            + "  \"atmospherePreferences\": [\"氛围偏好\"],\n"
            + "  \"favoriteShops\": [{\"shopId\": 1, \"reason\": \"原因\"}],\n"
            + "  \"diningHabits\": {\"frequency\": \"每周\", \"groupSize\": \"2-3人\"},\n"
            + "  \"confidence\": 0.0-1.0\n"
            + "}\n\n"
            + "confidence 含义：0.0=完全不确定（对话太短或没涉及偏好），1.0=非常确定（用户明确反复表达）";

    public PreferenceExtractor(ChatClient.Builder builder) {
        this.chatClient = builder
                .defaultSystem(EXTRACT_PROMPT)
                .build();
    }

    public CompletableFuture<PreferenceProfile> extractAsync(Long userId, String dialogue) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return extract(userId, dialogue);
            } catch (Exception e) {
                log.error("偏好提取失败 userId={}", userId, e);
                return null;
            }
        });
    }

    PreferenceProfile extract(Long userId, String dialogue) {
        if (dialogue == null || dialogue.isEmpty()) return null;

        if (dialogue.length() > 3000) {
            dialogue = dialogue.substring(dialogue.length() - 3000);
        }

        String userMsg = "请从以下对话中提取用户偏好：\n\n" + dialogue;

        try {
            PreferenceProfile profile = chatClient.prompt()
                    .user(userMsg)
                    .call()
                    .entity(PreferenceProfile.class);

            if (profile != null) {
                profile.setLastUpdated(System.currentTimeMillis());
                log.info("偏好提取完成 userId={} confidence={} cuisine={}",
                        userId, profile.getConfidence(), profile.getCuisinePreferences());
            }
            return profile;
        } catch (Exception e) {
            log.error("偏好提取LLM调用失败 userId={}", userId, e);
            return null;
        }
    }
}
