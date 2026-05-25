package com.citylife.controller;

import com.citylife.agent.RecommendationAgent;
import com.citylife.agent.memory.MemoryService;
import com.citylife.agent.memory.PreferenceExtractor;
import com.citylife.dto.AgentRequestDTO;
import com.citylife.dto.AgentResponseDTO;
import com.citylife.dto.Result;
import com.citylife.dto.UserDTO;
import com.citylife.utils.UserHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/agent")
@RequiredArgsConstructor
public class AgentController {

    private final RecommendationAgent agent;
    private final MemoryService memoryService;
    private final PreferenceExtractor preferenceExtractor;

    @PostMapping("/recommend")
    public Result<AgentResponseDTO> recommend(@RequestBody AgentRequestDTO request) {
        String sessionId = request.getSessionId();
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = memoryService.generateSessionId();
        }

        // 关联 session 与用户
        UserDTO user = UserHolder.getUser();
        if (user != null) {
            memoryService.associateSessionUser(sessionId, user.getId());
        }

        RecommendationAgent.RecommendResult result = agent.recommend(request.getMessage(), sessionId);

        // 异步提取用户偏好画像
        Long userId = user != null ? user.getId() : memoryService.getSessionUser(sessionId);
        if (userId != null) {
            List<Message> messages = agent.getRecentMessages(sessionId, 20);
            String dialogue = messages.stream()
                    .map(m -> m.getMessageType().name().toLowerCase() + ": " + m.getText())
                    .collect(Collectors.joining("\n"));
            preferenceExtractor.extractAsync(userId, dialogue)
                    .thenAccept(profile -> {
                        if (profile != null) {
                            memoryService.saveSemanticMemory(userId, profile);
                        }
                    });
        }

        return Result.ok(new AgentResponseDTO(result.getReply(), result.getConversationId()));
    }
}
