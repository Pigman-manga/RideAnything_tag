package xyz.telecter.rideanything.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SaddledComponent;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.telecter.rideanything.access.CarrotControllable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements CarrotControllable {
    @Unique
    private static final TrackedData<Integer> RIDEANYTHING_BOOST_TIME = DataTracker.registerData(LivingEntity.class,
            TrackedDataHandlerRegistry.INTEGER);

    @Unique
    private final SaddledComponent rideAnything$saddledComponent = new SaddledComponent(this.getDataTracker(),
            RIDEANYTHING_BOOST_TIME);

    protected LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Shadow
    public abstract double getAttributeValue(RegistryEntry<EntityAttribute> attribute);

    @Shadow
    public abstract float getYaw();

    @Shadow
    public float bodyYaw;

    @Shadow
    public float headYaw;

    @Shadow
    public abstract void setRotation(float yaw, float pitch);

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void rideAnything$initDataTracker(DataTracker.Builder builder, CallbackInfo ci) {
        builder.add(RIDEANYTHING_BOOST_TIME, 0);
    }

    @Inject(method = "onTrackedDataSet", at = @At("HEAD"))
    private void rideAnything$onTrackedDataSet(TrackedData<?> data, CallbackInfo ci) {
        if (RIDEANYTHING_BOOST_TIME.equals(data) && this.getEntityWorld().isClient()) {
            this.rideAnything$saddledComponent.boost();
        }
    }

    @Inject(method = "tickControlled", at = @At("HEAD"))
    private void rideAnything$tickControlled(PlayerEntity player, Vec3d movementInput, CallbackInfo ci) {
        if (!this.getCommandTags().contains("rideble_carrot")) {
            return;
        }

        this.setRotation(player.getYaw(), player.getPitch() * 0.5F);
        this.lastYaw = this.getYaw();
        this.bodyYaw = this.getYaw();
        this.headYaw = this.getYaw();
        this.rideAnything$saddledComponent.tickBoost();
    }

    @Inject(method = "getControlledMovementInput", at = @At("HEAD"), cancellable = true)
    private void rideAnything$getControlledMovementInput(PlayerEntity player, Vec3d movementInput,
            CallbackInfoReturnable<Vec3d> cir) {
        if (this.getCommandTags().contains("rideble_carrot")) {
            cir.setReturnValue(new Vec3d(0.0, movementInput.y, 1.0));
        }
    }

    @Inject(method = "getSaddledSpeed", at = @At("HEAD"), cancellable = true)
    private void rideAnything$getSaddledSpeed(PlayerEntity player, CallbackInfoReturnable<Float> cir) {
        if (this.getCommandTags().contains("rideble_carrot")) {
            cir.setReturnValue((float) (this.getAttributeValue(EntityAttributes.MOVEMENT_SPEED) * 0.225D
                    * this.rideAnything$saddledComponent.getMovementSpeedMultiplier()));
        }
    }

    @Override
    public boolean rideAnything$consumeOnAStickItem() {
        return this.rideAnything$saddledComponent.boost(this.getRandom());
    }
}
