package xyz.telecter.rideanything.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin {
    @Inject(method = "getControllingPassenger", at = @At("HEAD"), cancellable = true)
    private void rideAnything$getControllingPassenger(CallbackInfoReturnable<LivingEntity> cir) {
        MobEntity self = (MobEntity) (Object) this;
        if (!self.getCommandTags().contains("rideble_carrot")) {
            return;
        }

        Entity passenger = self.getFirstPassenger();
        if (passenger instanceof PlayerEntity player && player.isHolding(Items.CARROT_ON_A_STICK)) {
            cir.setReturnValue(player);
        }
    }
}
