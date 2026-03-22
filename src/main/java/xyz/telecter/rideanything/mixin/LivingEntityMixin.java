package xyz.telecter.rideanything.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.Vec3d;
import xyz.telecter.rideanything.util.RideAnythingTags;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Shadow
    public abstract Entity getFirstPassenger();

    @Shadow
    public float headYaw;

    @Shadow
    public float bodyYaw;

    @Shadow
    public float lastYaw;

    @Shadow
    protected abstract void setRotation(float yaw, float pitch);

    @Shadow
    public abstract double getAttributeValue(RegistryEntry<EntityAttribute> attribute);

    @Shadow
    public abstract ItemStack getEquippedStack(EquipmentSlot slot);

    @Inject(method = "tickControlled", at = @At("HEAD"), cancellable = true)
    private void rideanything$tickControlled(PlayerEntity controllingPlayer, Vec3d movementInput, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!RideAnythingTags.hasRideableCarrotTag(self)
                || !rideanything$hasSaddleEquipped()
                || this.getFirstPassenger() != controllingPlayer) {
            return;
        }

        this.setRotation(controllingPlayer.getYaw(), controllingPlayer.getPitch() * 0.5F);
        self.headYaw = self.getYaw();
        self.bodyYaw = self.getYaw();
        self.lastYaw = self.getYaw();

        if (self instanceof MobEntity mobEntity) {
            mobEntity.getNavigation().stop();
        }

        ci.cancel();
    }

    @Inject(method = "getControlledMovementInput", at = @At("HEAD"), cancellable = true)
    private void rideanything$getControlledMovementInput(PlayerEntity controllingPlayer, Vec3d movementInput, CallbackInfoReturnable<Vec3d> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!RideAnythingTags.hasRideableCarrotTag(self)
                || !rideanything$hasSaddleEquipped()
                || this.getFirstPassenger() != controllingPlayer) {
            return;
        }

        cir.setReturnValue(new Vec3d(0.0D, 0.0D, 1.0D));
    }

    @Inject(method = "getSaddledSpeed", at = @At("HEAD"), cancellable = true)
    private void rideanything$getSaddledSpeed(PlayerEntity controllingPlayer, CallbackInfoReturnable<Float> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!RideAnythingTags.hasRideableCarrotTag(self)
                || !rideanything$hasSaddleEquipped()
                || this.getFirstPassenger() != controllingPlayer) {
            return;
        }

        cir.setReturnValue((float) (this.getAttributeValue(EntityAttributes.MOVEMENT_SPEED) * 0.225D));
    }

    @Inject(method = "canUseSlot", at = @At("HEAD"), cancellable = true)
    private void rideanything$canUseSaddleSlot(EquipmentSlot slot, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (slot == EquipmentSlot.SADDLE && RideAnythingTags.hasRideableCarrotTag(self)) {
            cir.setReturnValue(self.isAlive() && !self.isBaby());
        }
    }

    private boolean rideanything$hasSaddleEquipped() {
        return this.getEquippedStack(EquipmentSlot.SADDLE).isOf(Items.SADDLE);
    }
}
