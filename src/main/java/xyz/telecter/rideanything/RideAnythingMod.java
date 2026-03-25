package xyz.telecter.rideanything;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import xyz.telecter.rideanything.config.RideAnythingConfig;

public class RideAnythingMod implements ModInitializer {
	public static final String MOD_ID = "rideanything";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private static final String RIDEABLE_TAG = "rideble";
	private static final String RIDEABLE_CARROT_TAG = "rideble_carrot";

	@Override
	public void onInitialize() {
		RideAnythingConfig.HANDLER.load();

		UseEntityCallback.EVENT.register((player, world, hand, entity, result) -> {
			if (!world.isClient() && RideAnythingConfig.HANDLER.instance().enabled) {
				if (player.getStackInHand(hand).isEmpty() && shouldRide(player, entity)) {
					if (player.startRiding(entity)) {
						return ActionResult.SUCCESS;
					}
				}
			}
			return ActionResult.PASS;
		});

		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (var player : server.getPlayerManager().getPlayerList()) {
				Entity vehicle = player.getVehicle();
				if (!(vehicle instanceof LivingEntity livingEntity)) {
					continue;
				}

				if (!livingEntity.getCommandTags().contains(RIDEABLE_CARROT_TAG)
						|| !player.getMainHandStack().isOf(Items.CARROT_ON_A_STICK)) {
					continue;
				}

				if (player.getControllingVehicle() != vehicle) {
					continue;
				}

				applyCarrotControl(player, livingEntity);
			}
		});
	}

	public static boolean shouldRide(PlayerEntity player, Entity entity) {
		if (!(entity instanceof LivingEntity)) {
			return false;
		}

		if (!entity.getCommandTags().contains(RIDEABLE_TAG)) {
			return false;
		}

		RideAnythingConfig config = RideAnythingConfig.HANDLER.instance();
		if ((config.mode == RideAnythingConfig.Mode.ANIMALS && entity instanceof AnimalEntity)
				|| (config.mode == RideAnythingConfig.Mode.ALL && entity instanceof LivingEntity)) {
			return true;
		}
		if (config.mode == RideAnythingConfig.Mode.CUSTOM) {
			Identifier origId = EntityType.getId(entity.getType());

			for (String s : config.allowed) {
				Identifier id = Identifier.of(s);

				if (origId.equals(id)) {
					return true;
				}
			}
		}

		return false;
	}

	private static void applyCarrotControl(PlayerEntity player, LivingEntity entity) {
		float forward = Math.max(player.forwardSpeed, 0.0F);
		if (forward <= 0.0F) {
			return;
		}

		float yaw = player.getYaw();
		entity.setYaw(yaw);
		entity.setBodyYaw(yaw);
		entity.setHeadYaw(yaw);

		Vec3d direction = Vec3d.fromPolar(0.0F, yaw);
		double speed = 0.35D * forward;
		Vec3d velocity = entity.getVelocity();
		entity.setVelocity(direction.x * speed, velocity.y, direction.z * speed);
	}
}
