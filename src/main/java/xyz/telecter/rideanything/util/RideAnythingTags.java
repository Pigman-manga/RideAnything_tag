package xyz.telecter.rideanything.util;

import net.minecraft.entity.Entity;

public final class RideAnythingTags {
    public static final String RIDEABLE_TAG = "rideble";
    public static final String RIDEABLE_CARROT_TAG = "rideble_carrot";

    private RideAnythingTags() {
    }

    public static boolean hasRideableTag(Entity entity) {
        return entity.getCommandTags().contains(RIDEABLE_TAG);
    }

    public static boolean hasRideableCarrotTag(Entity entity) {
        return entity.getCommandTags().contains(RIDEABLE_CARROT_TAG);
    }
}
