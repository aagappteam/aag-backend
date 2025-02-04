package aagapp_backend.enums;

import lombok.Getter;

@Getter
public enum VendorLevelPlan {
    // Standard Plan
    STANDARD_A("Standard A", 5, 2, 4, 1000, "Rank B", 5, 5, 10000), // Price added here
    STANDARD_B("Standard B", 5, 2, 5, 2000, "Rank C", 5, 6, 10000),
    STANDARD_C("Standard C", 7, 3, 7, 5000, "Rank D", 7, 7, 10000),
    STANDARD_D("Standard D", 8, 4, 8, 7000, "Rank E", 8, 8, 10000),
    STANDARD_E("Standard E", 10, 5, 10, 10000, "League access & Special event entry", 10, 9, 10000), // Price added here

    // Pro Plan
    PRO_A("Pro A", 10, 4, 4, 5000, "Rank B", 10, 5, 20000), // Price added here
    PRO_B("Pro B", 10, 5, 5, 7000, "Rank C", 10, 6, 20000),
    PRO_C("Pro C", 12, 7, 7, 10000, "Rank D", 12, 7, 20000),
    PRO_D("Pro D", 13, 9, 8, 12000, "Rank E", 13, 8, 20000),
    PRO_E("Pro E", 15, 9, 10, 15000, "Tournament & event access & Special event entry", 15, 9, 20000), // Price added here

    // Elite Plan
    ELITE_A("Elite A", 15, 7, 4, 10000, "Rank B", 15, 5, 50000), // Price added here
    ELITE_B("Elite B", 17, 9, 5, 12000, "Rank C", 17, 6, 50000),
    ELITE_C("Elite C", 20, 10, 7, 15000, "Rank D", 20, 7, 50000),
    ELITE_D("Elite D", 25, 10, 8, 17000, "Rank E", 25, 8, 50000),
    ELITE_E("Elite E", 25, 13, 10, 20000, "Tournament & event entry & Two special event entries", 25, 9, 50000); // Price added here

    private final String name;
    private final int dailyGameLimit;
    private final int themeCount;
    private final int returnXSubscription;
    private final int userCounterRequirement;
    private final String progressionCondition;
    private final int resetCounterCondition;
    private final int profitPercentage;
    private final int price; // New field for price

    VendorLevelPlan(String name, int dailyGameLimit, int themeCount, int returnXSubscription, int userCounterRequirement, String progressionCondition, int resetCounterCondition, int profitPercentage, int price) {
        this.name = name;
        this.dailyGameLimit = dailyGameLimit;
        this.themeCount = themeCount;
        this.returnXSubscription = returnXSubscription;
        this.userCounterRequirement = userCounterRequirement;
        this.progressionCondition = progressionCondition;
        this.resetCounterCondition = resetCounterCondition;
        this.profitPercentage = profitPercentage;
        this.price = price;
    }

    public static VendorLevelPlan fromString(String name) {
        for (VendorLevelPlan vendorLevelPlan : VendorLevelPlan.values()) {
            if (vendorLevelPlan.name.equalsIgnoreCase(name)) {
                return vendorLevelPlan;
            }
        }
        throw new IllegalArgumentException("No enum constant with name " + name);
    }
}
