package xyz.telecter.rideanything;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ActionResult;
import xyz.telecter.rideanything.config.RideAnythingConfig;

public class RideAnythingMod implements ModInitializer {
	public static final String MOD_ID = "rideanything";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		RideAnythingConfig.HANDLER.load();

		UseEntityCallback.EVENT.register((player, world, hand, entity, result) -> {
			if (!world.isClient() && RideAnythingConfig.HANDLER.instance().enabled) {
				if (player.getStackInHand(hand).isEmpty() && shouldRide(entity)) {
					if (player.startRiding(entity)) {
						return ActionResult.SUCCESS;
					}
				}
			}
			return ActionResult.PASS;
		});
	}

	public static boolean shouldRide(Entity entity) {
		return entity instanceof LivingEntity livingEntity && livingEntity.getCommandTags().contains("rideble");
	}
}
