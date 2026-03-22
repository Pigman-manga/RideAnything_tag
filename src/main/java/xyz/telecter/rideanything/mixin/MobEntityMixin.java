package xyz.telecter.rideanything.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import xyz.telecter.rideanything.util.RideAnythingTags;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin {
    @Shadow
    public abstract Entity getFirstPassenger();

    @Shadow
    public abstract ItemStack getEquippedStack(EquipmentSlot slot);

    @Inject(method = "getControllingPassenger", at = @At("HEAD"), cancellable = true)
    private void rideanything$getControllingPassenger(CallbackInfoReturnable<LivingEntity> cir) {
        MobEntity self = (MobEntity) (Object) this;
        if (!RideAnythingTags.hasRideableCarrotTag(self) || !this.rideanything$hasSaddleEquipped()) {
            return;
        }

        Entity passenger = this.getFirstPassenger();
        if (passenger instanceof PlayerEntity player && player.isHolding(Items.CARROT_ON_A_STICK)) {
            cir.setReturnValue(player);
        }
    }

    private boolean rideanything$hasSaddleEquipped() {
        return this.getEquippedStack(EquipmentSlot.SADDLE).isOf(Items.SADDLE);
    }
}
