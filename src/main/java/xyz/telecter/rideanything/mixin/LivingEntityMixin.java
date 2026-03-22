package xyz.telecter.rideanything.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import xyz.telecter.rideanything.RideAnythingMod;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
	@Shadow public float bodyYaw;
	@Shadow public float headYaw;
	@Shadow public float lastYaw;
	@Shadow protected abstract void setRotation(float yaw, float pitch);
	@Shadow public abstract float getYaw();
	@Shadow public abstract double getAttributeValue(net.minecraft.registry.entry.RegistryEntry<net.minecraft.entity.attribute.EntityAttribute> attribute);

	@Inject(method = "tickControlled", at = @At("HEAD"))
	private void rideanything$tickControlled(PlayerEntity controllingPlayer, Vec3d movementInput, CallbackInfo ci) {
		LivingEntity self = (LivingEntity) (Object) this;
		if (!RideAnythingMod.canUseCarrotControl(self)) {
			return;
		}

		this.setRotation(controllingPlayer.getYaw(), controllingPlayer.getPitch() * 0.5F);
		this.headYaw = this.getYaw();
		this.bodyYaw = this.getYaw();
		this.lastYaw = this.getYaw();
	}

	@Inject(method = "getControlledMovementInput", at = @At("HEAD"), cancellable = true)
	private void rideanything$getControlledMovementInput(PlayerEntity controllingPlayer, Vec3d movementInput, CallbackInfoReturnable<Vec3d> cir) {
		LivingEntity self = (LivingEntity) (Object) this;
		if (!RideAnythingMod.canUseCarrotControl(self)) {
			return;
		}

		float forward = controllingPlayer.forwardSpeed;
		float sideways = controllingPlayer.sidewaysSpeed * 0.5F;
		if (forward <= 0.0F) {
			forward *= 0.25F;
		}

		cir.setReturnValue(new Vec3d(sideways, 0.0D, forward));
	}

	@Inject(method = "getSaddledSpeed", at = @At("HEAD"), cancellable = true)
	private void rideanything$getSaddledSpeed(PlayerEntity controllingPlayer, CallbackInfoReturnable<Float> cir) {
		LivingEntity self = (LivingEntity) (Object) this;
		if (!RideAnythingMod.canUseCarrotControl(self)) {
			return;
		}

		cir.setReturnValue((float) (this.getAttributeValue(EntityAttributes.MOVEMENT_SPEED) * 1.25D));
	}
}
