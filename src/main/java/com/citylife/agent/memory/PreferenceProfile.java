package com.citylife.agent.memory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PreferenceProfile {

    private List<String> cuisinePreferences;
    private String budgetLevel;
    private List<String> atmospherePreferences;
    private List<Map<String, Object>> favoriteShops;
    private Map<String, Object> diningHabits;
    private double confidence;
    private long lastUpdated;

    public List<String> getCuisinePreferences() { return cuisinePreferences; }
    public void setCuisinePreferences(List<String> cuisinePreferences) { this.cuisinePreferences = cuisinePreferences; }
    public String getBudgetLevel() { return budgetLevel; }
    public void setBudgetLevel(String budgetLevel) { this.budgetLevel = budgetLevel; }
    public List<String> getAtmospherePreferences() { return atmospherePreferences; }
    public void setAtmospherePreferences(List<String> atmospherePreferences) { this.atmospherePreferences = atmospherePreferences; }
    public List<Map<String, Object>> getFavoriteShops() { return favoriteShops; }
    public void setFavoriteShops(List<Map<String, Object>> favoriteShops) { this.favoriteShops = favoriteShops; }
    public Map<String, Object> getDiningHabits() { return diningHabits; }
    public void setDiningHabits(Map<String, Object> diningHabits) { this.diningHabits = diningHabits; }
    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }
    public long getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(long lastUpdated) { this.lastUpdated = lastUpdated; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("cuisinePreferences", cuisinePreferences);
        map.put("budgetLevel", budgetLevel);
        map.put("atmospherePreferences", atmospherePreferences);
        map.put("favoriteShops", favoriteShops);
        map.put("diningHabits", diningHabits);
        map.put("confidence", confidence);
        map.put("lastUpdated", lastUpdated);
        return map;
    }
}
