package com.example.survival.mobs;

/**
 * An enum representing the different types of custom mobs.
 */
public enum MobType { /*List of mobs */
    SHADOW_ZOMBIE("shadow_zombie", "Shadow Zombie");
    /* Add more mob types here */

    private final String id;
    private final String displayName;

    MobType(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static MobType fromId(String id) {
        for (MobType type : values()) {
            if (type.id.equalsIgnoreCase(id)) {
                return type;
            }
        }
        return null;
    }
}
