package com.citylife.agent.memory;

import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemoryService {

    private final StringRedisTemplate stringRedisTemplate;

    private static final String SEMANTIC_KEY = "agent:memory:semantic:%d";
    private static final String SESSION_USER_KEY = "agent:memory:session_user:%s";
    private static final int SESSION_TTL_MINUTES = 30;

    public String generateSessionId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    public void associateSessionUser(String sessionId, Long userId) {
        if (sessionId == null || userId == null) return;
        String key = String.format(SESSION_USER_KEY, sessionId);
        stringRedisTemplate.opsForValue().set(key, String.valueOf(userId), SESSION_TTL_MINUTES, TimeUnit.MINUTES);
    }

    public Long getSessionUser(String sessionId) {
        if (sessionId == null) return null;
        String key = String.format(SESSION_USER_KEY, sessionId);
        String val = stringRedisTemplate.opsForValue().get(key);
        if (val == null) return null;
        try {
            return Long.parseLong(val);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // === Semantic Memory ===

    public void saveSemanticMemory(Long userId, PreferenceProfile profile) {
        if (userId == null || profile == null) return;
        String key = String.format(SEMANTIC_KEY, userId);
        stringRedisTemplate.opsForHash().putAll(key, stringifyMap(profile.toMap()));
    }

    public PreferenceProfile getSemanticMemory(Long userId) {
        if (userId == null) return null;
        String key = String.format(SEMANTIC_KEY, userId);
        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(key);
        if (entries.isEmpty()) return null;

        PreferenceProfile profile = new PreferenceProfile();
        profile.setCuisinePreferences(parseStringList(entries, "cuisinePreferences"));
        profile.setBudgetLevel((String) entries.get("budgetLevel"));
        profile.setAtmospherePreferences(parseStringList(entries, "atmospherePreferences"));
        profile.setConfidence(parseDouble(entries, "confidence"));
        profile.setLastUpdated(parseLong(entries, "lastUpdated"));
        return profile;
    }

    // === Helpers ===

    private Map<String, String> stringifyMap(Map<String, Object> map) {
        Map<String, String> result = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : map.entrySet()) {
            if (e.getValue() != null) {
                result.put(e.getKey(), JSONUtil.toJsonStr(e.getValue()));
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<String> parseStringList(Map<Object, Object> entries, String key) {
        Object val = entries.get(key);
        if (val == null) return Collections.emptyList();
        return JSONUtil.toList(val.toString(), String.class);
    }

    private double parseDouble(Map<Object, Object> entries, String key) {
        Object val = entries.get(key);
        if (val == null) return 0.0;
        try { return Double.parseDouble(val.toString()); } catch (NumberFormatException e) { return 0.0; }
    }

    private long parseLong(Map<Object, Object> entries, String key) {
        Object val = entries.get(key);
        if (val == null) return 0L;
        try { return Long.parseLong(val.toString()); } catch (NumberFormatException e) { return 0L; }
    }
}
