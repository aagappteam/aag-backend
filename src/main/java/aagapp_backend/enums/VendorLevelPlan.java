package aagapp_backend.enums;

import lombok.Getter;

@Getter
public enum VendorLevelPlan {
    STANDARD_A(5, 1000, 4, "Standard A", 1, 1),
    STANDARD_B(5, 2000, 5, "Standard B", 1, 2),
    STANDARD_C(7, 5000, 7, "Standard C", 2, 3),
    STANDARD_D(8, 7000, 8, "Standard D", 3, 4),
    STANDARD_E(10, 10000, 10, "Standard E", 3, 5),

    PRO_A(10, 5000, 4, "Pro A", 1, 1),
    PRO_B(10, 7000, 5, "Pro B", 2, 2),
    PRO_C(12, 10000, 7, "Pro C", 3, 3),
    PRO_D(13, 12000, 8, "Pro D", 4, 4),
    PRO_E(15, 15000, 10, "Pro E", 5, 5),

    ELITE_A(15, 10000, 4, "Elite A", 4, 6),
    ELITE_B(17, 12000, 5, "Elite B", 5, 7),
    ELITE_C(20, 15000, 7, "Elite C", 6, 8),
    ELITE_D(25, 17000, 8, "Elite D", 7, 9),
    ELITE_E(25, 20000, 10, "Elite E", 8, 10);

    private final int dailyGameLimit;
    private final int userCounterRequirement;
    private final int returnMultiplier;
    private final String levelName;
    private final int themeCount;
    private final int featureSlots;

    VendorLevelPlan(int dailyGameLimit, int userCounterRequirement, int returnMultiplier,
                    String levelName, int themeCount, int featureSlots) {
        this.dailyGameLimit = dailyGameLimit;
        this.userCounterRequirement = userCounterRequirement;
        this.returnMultiplier = returnMultiplier;
        this.levelName = levelName;
        this.themeCount = themeCount;
        this.featureSlots = featureSlots;
    }

    public int getDailyGameLimit() {
        return dailyGameLimit;
    }

    public int getUserCounterRequirement() {
        return userCounterRequirement;
    }

    public int getReturnMultiplier() {
        return returnMultiplier;
    }

    public String getLevelName() {
        return levelName;
    }

    public int getThemeCount() {
        return themeCount;
    }

    public int getFeatureSlots() {
        return featureSlots;
    }

    // Method to get the next level
    public VendorLevelPlan getNextLevel() {
        VendorLevelPlan[] levels = VendorLevelPlan.values();
        int index = ordinal();
        if (index < levels.length - 1) {
            return levels[index + 1];
        }
        return null;
    }

    public static VendorLevelPlan getDefaultLevel() {
        return STANDARD_A;
    }
}
