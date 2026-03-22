package xyz.telecter.rideanything.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import xyz.telecter.rideanything.RideAnythingMod;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin {
	@Inject(method = "tickNewAi", at = @At("TAIL"))
	private void rideanything$steerWithCarrot(CallbackInfo ci) {
		MobEntity self = (MobEntity) (Object) this;
		PlayerEntity player = getCarrotSteeringPlayer(self);
		if (player == null) {
			return;
		}

		float forward = player.forwardSpeed;
		float sideways = player.sidewaysSpeed * 0.5F;
		if (forward <= 0.0F) {
			forward *= 0.25F;
		}

		self.setYaw(player.getYaw());
		self.setHeadYaw(player.getYaw());
		self.bodyYaw = self.getYaw();

		if (Math.abs(forward) < 1.0E-3F && Math.abs(sideways) < 1.0E-3F) {
			self.getNavigation().stop();
			self.getMoveControl().moveTo(self.getX(), self.getY(), self.getZ(), 0.0D);
			return;
		}

		Vec3d direction = getInputDirection(player.getYaw(), forward, sideways);
		double speed = Math.max(0.8D, self.getAttributeValue(net.minecraft.entity.attribute.EntityAttributes.MOVEMENT_SPEED) * 1.25D);
		Vec3d target = new Vec3d(self.getX(), self.getY(), self.getZ()).add(direction.multiply(2.5D));

		self.getNavigation().stop();
		MoveControl moveControl = self.getMoveControl();
		moveControl.moveTo(target.x, target.y, target.z, speed);
		if (self instanceof PathAwareEntity pathAwareEntity) {
			pathAwareEntity.getNavigation().startMovingTo(target.x, target.y, target.z, speed);
		}
	}

	private static PlayerEntity getCarrotSteeringPlayer(MobEntity entity) {
		if (!RideAnythingMod.canUseCarrotControl(entity)) {
			return null;
		}

		Entity passenger = entity.getFirstPassenger();
		if (passenger instanceof PlayerEntity player && player.isHolding(Items.CARROT_ON_A_STICK)) {
			return player;
		}

		return null;
	}

	private static Vec3d getInputDirection(float yaw, float forward, float sideways) {
		float radians = yaw * 0.017453292F;
		float sin = MathHelper.sin(radians);
		float cos = MathHelper.cos(radians);
		double x = sideways * cos - forward * sin;
		double z = forward * cos + sideways * sin;
		return new Vec3d(x, 0.0D, z).normalize();
	}
}
