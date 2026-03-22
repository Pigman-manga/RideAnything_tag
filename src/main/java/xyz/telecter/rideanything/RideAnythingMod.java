package xyz.telecter.rideanything;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Vec3d;
import xyz.telecter.rideanything.config.RideAnythingConfig;

public class RideAnythingMod implements ModInitializer {
	public static final String MOD_ID = "rideanything";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private static final String RIDEABLE_TAG = "rideble";
	private static final String RIDEABLE_CARROT_TAG = "rideble_carrot";
	private static final double CARROT_CONTROL_SPEED_MULTIPLIER = 0.35D;
	private static final double CARROT_LOOKAHEAD_DISTANCE = 2.5D;

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

		ServerTickEvents.END_SERVER_TICK.register(this::tickCarrotControlledMounts);
	}

	public static boolean shouldRide(PlayerEntity player, Entity entity) {
		return entity instanceof LivingEntity
				&& entity.getCommandTags().contains(RIDEABLE_TAG)
				&& !player.hasVehicle();
	}

	private void tickCarrotControlledMounts(MinecraftServer server) {
		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			Entity vehicle = player.getVehicle();
			if (!(vehicle instanceof LivingEntity livingEntity)) {
				continue;
			}
			if (!vehicle.getCommandTags().contains(RIDEABLE_CARROT_TAG) || !isHoldingCarrotOnAStick(player)) {
				continue;
			}

			steerVehicle(player, livingEntity);
		}
	}

	private boolean isHoldingCarrotOnAStick(PlayerEntity player) {
		return player.getMainHandStack().isOf(Items.CARROT_ON_A_STICK)
				|| player.getOffHandStack().isOf(Items.CARROT_ON_A_STICK);
	}

	private void steerVehicle(PlayerEntity player, LivingEntity vehicle) {
		float yaw = player.getYaw();
		vehicle.setYaw(yaw);
		vehicle.setBodyYaw(yaw);
		vehicle.headYaw = yaw;

		Vec3d riderInput = new Vec3d(player.sidewaysSpeed, 0.0D, player.forwardSpeed);
		if (vehicle instanceof MobEntity mobEntity) {
			steerMobVehicle(mobEntity, riderInput, yaw);
			return;
		}

		steerGenericVehicle(vehicle, riderInput, yaw);
	}

	private void steerMobVehicle(MobEntity vehicle, Vec3d riderInput, float yaw) {
		vehicle.getNavigation().stop();
		vehicle.setTarget(null);

		Vec3d velocity = vehicle.getVelocity();
		if (riderInput.lengthSquared() < 1.0E-4D) {
			vehicle.setForwardSpeed(0.0F);
			vehicle.setVelocity(velocity.x * 0.5D, velocity.y, velocity.z * 0.5D);
			vehicle.velocityDirty = true;
			return;
		}

		Vec3d movementDirection = getMovementDirection(riderInput, yaw);
		double speed = vehicle.getMovementSpeed() + CARROT_CONTROL_SPEED_MULTIPLIER;
		Vec3d moveTarget = new Vec3d(vehicle.getX(), vehicle.getY(), vehicle.getZ()).add(movementDirection.multiply(CARROT_LOOKAHEAD_DISTANCE));
		vehicle.getMoveControl().moveTo(moveTarget.x, vehicle.getY(), moveTarget.z, speed);
		vehicle.setForwardSpeed((float) speed);
		vehicle.velocityDirty = true;
	}

	private void steerGenericVehicle(LivingEntity vehicle, Vec3d riderInput, float yaw) {
		Vec3d velocity = vehicle.getVelocity();
		if (riderInput.lengthSquared() < 1.0E-4D) {
			vehicle.setVelocity(velocity.x * 0.5D, velocity.y, velocity.z * 0.5D);
			vehicle.velocityDirty = true;
			return;
		}

		Vec3d movement = getMovementDirection(riderInput, yaw)
				.multiply(vehicle.getMovementSpeed() + CARROT_CONTROL_SPEED_MULTIPLIER);
		vehicle.move(MovementType.SELF, movement);
		vehicle.setVelocity(movement.x, velocity.y, movement.z);
		vehicle.velocityDirty = true;
	}

	private Vec3d getMovementDirection(Vec3d riderInput, float yaw) {
		return new Vec3d(riderInput.x, 0.0D, riderInput.z)
				.normalize()
				.rotateY((float) Math.toRadians(-yaw));
	}
}
