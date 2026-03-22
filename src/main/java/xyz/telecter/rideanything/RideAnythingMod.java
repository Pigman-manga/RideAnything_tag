package xyz.telecter.rideanything;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import xyz.telecter.rideanything.config.RideAnythingConfig;

public class RideAnythingMod implements ModInitializer {
	public static final String MOD_ID = "rideanything";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final String RIDEABLE_TAG = "rideble";
	public static final String CARROT_CONTROL_TAG = "rideble_carrot";
	private static final double BACKWARD_SPEED_MULTIPLIER = 0.25D;
	private static final double AIR_CONTROL_MULTIPLIER = 0.6D;

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

		ServerTickEvents.END_WORLD_TICK.register(world -> {
			if (!RideAnythingConfig.HANDLER.instance().enabled) {
				return;
			}

			for (Entity entity : world.iterateEntities()) {
				if (entity instanceof MobEntity mob) {
					tickControlledMob(mob);
				}
			}
		});
	}

	public static boolean shouldRide(Entity entity) {
		return entity instanceof LivingEntity living && living.getCommandTags().contains(RIDEABLE_TAG);
	}

	private static void tickControlledMob(MobEntity mob) {
		PlayerEntity rider = getControllingPlayer(mob);
		if (rider == null) {
			return;
		}

		mob.getNavigation().stop();
		mob.setTarget(null);
		mob.getMoveControl().setWaiting();

		if (!canUseCarrotControl(mob, rider)) {
			return;
		}

		float riderYaw = rider.getYaw();
		mob.setYaw(riderYaw);
		mob.setBodyYaw(riderYaw);
		mob.setHeadYaw(riderYaw);
		mob.setForwardSpeed(0.0F);
		mob.setSidewaysSpeed(0.0F);

		float forwardInput = rider.forwardSpeed;
		float sidewaysInput = rider.sidewaysSpeed * 0.5F;
		if (forwardInput < 0.0F) {
			forwardInput *= BACKWARD_SPEED_MULTIPLIER;
		}

		double inputMagnitude = MathHelper.clamp(Math.sqrt(forwardInput * forwardInput + sidewaysInput * sidewaysInput), 0.0D,
				1.0D);
		if (inputMagnitude < 1.0E-3D) {
			mob.setSprinting(false);
			return;
		}

		float movementYaw = riderYaw;
		if (forwardInput < 0.0F) {
			movementYaw += 180.0F;
		}
		if (sidewaysInput > 0.0F) {
			movementYaw -= 90.0F;
		} else if (sidewaysInput < 0.0F) {
			movementYaw += 90.0F;
		}
		if (forwardInput != 0.0F && sidewaysInput != 0.0F) {
			movementYaw += forwardInput > 0.0F ? (sidewaysInput > 0.0F ? -45.0F : 45.0F)
					: (sidewaysInput > 0.0F ? 45.0F : -45.0F);
		}

		double baseSpeed = getMoveSpeed(mob);
		double speed = baseSpeed * inputMagnitude;
		if (!mob.isOnGround()) {
			speed *= AIR_CONTROL_MULTIPLIER;
		}

		Vec3d horizontalVelocity = Vec3d.fromPolar(0.0F, movementYaw).multiply(speed);
		Vec3d currentVelocity = mob.getVelocity();
		mob.setVelocity(horizontalVelocity.x, currentVelocity.y, horizontalVelocity.z);
		mob.setSprinting(rider.isSprinting());

		if (mob instanceof SlimeEntity slime && mob.isOnGround()) {
			slime.jump();
		}
	}

	private static PlayerEntity getControllingPlayer(MobEntity mob) {
		if (!shouldRide(mob)) {
			return null;
		}

		Entity passenger = mob.getFirstPassenger();
		return passenger instanceof PlayerEntity player ? player : null;
	}

	private static boolean canUseCarrotControl(MobEntity mob, PlayerEntity rider) {
		return mob.getCommandTags().contains(CARROT_CONTROL_TAG) && isCarrotOnAStick(rider.getMainHandStack())
				|| mob.getCommandTags().contains(CARROT_CONTROL_TAG) && isCarrotOnAStick(rider.getOffHandStack());
	}

	private static boolean isCarrotOnAStick(ItemStack stack) {
		return stack.isOf(Items.CARROT_ON_A_STICK);
	}

	private static double getMoveSpeed(MobEntity mob) {
		var attribute = mob.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED);
		if (attribute != null) {
			return attribute.getValue();
		}
		return mob.isSprinting() ? 0.15D : 0.1D;
	}
}
