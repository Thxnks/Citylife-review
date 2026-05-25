package com.citylife.agent.tool;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.citylife.agent.memory.MemoryService;
import com.citylife.agent.memory.PreferenceProfile;
import com.citylife.dto.UserDTO;
import com.citylife.entity.Follow;
import com.citylife.entity.UserInfo;
import com.citylife.service.IFollowService;
import com.citylife.service.IUserInfoService;
import com.citylife.service.IUserService;
import com.citylife.utils.UserHolder;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserProfileTool {

    private final IUserService userService;
    private final IUserInfoService userInfoService;
    private final IFollowService followService;
    private final StringRedisTemplate stringRedisTemplate;
    private final MemoryService memoryService;

    @Tool(description = "获取当前登录用户的画像信息，包括昵称、城市、积分、签到天数、关注数、粉丝数、历史点赞的店铺类型偏好。用于个性化推荐。")
    public String getUserProfile() {
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            return JSONUtil.toJsonStr(Map.of("error", "用户未登录"));
        }

        JSONObject result = new JSONObject();
        result.set("userId", user.getId());
        result.set("nickName", user.getNickName());

        UserInfo userInfo = userInfoService.getById(user.getId());
        if (userInfo != null) {
            result.set("city", userInfo.getCity());
            result.set("fans", userInfo.getFans());
            result.set("followee", userInfo.getFollowee());
            result.set("level", userInfo.getLevel());
            result.set("credits", userInfo.getCredits());
        }

        String signKey = "sign:" + user.getId();
        String bitField = stringRedisTemplate.opsForValue().get(signKey);
        if (bitField != null) {
            result.set("signKeyExists", true);
        }

        List<Follow> follows = followService.list(
                new LambdaQueryWrapper<Follow>().eq(Follow::getUserId, user.getId()).last("LIMIT 20")
        );
        if (!follows.isEmpty()) {
            result.set("followingIds", follows.stream().map(Follow::getFollowUserId).collect(Collectors.toList()));
            result.set("followingCount", follows.size());
        }

        result.set("preferenceHint", "可根据该用户点赞、浏览的店铺类型推断偏好");

        // 追加语义记忆
        PreferenceProfile profile = memoryService.getSemanticMemory(user.getId());
        if (profile != null && profile.getConfidence() > 0.3) {
            JSONObject semantic = new JSONObject();
            semantic.set("cuisinePreferences", profile.getCuisinePreferences());
            semantic.set("budgetLevel", profile.getBudgetLevel());
            semantic.set("atmospherePreferences", profile.getAtmospherePreferences());
            semantic.set("confidence", profile.getConfidence());
            result.set("semanticMemory", semantic);
        }

        return result.toString();
    }
}
