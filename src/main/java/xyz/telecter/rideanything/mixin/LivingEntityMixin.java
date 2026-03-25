package xyz.telecter.rideanything.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import xyz.telecter.rideanything.RideAnythingMod;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
	@Inject(method = "getControllingPassenger", at = @At("HEAD"), cancellable = true)
	private void rideanything$getControllingPassenger(CallbackInfoReturnable<@Nullable LivingEntity> cir) {
		LivingEntity self = (LivingEntity) (Object) this;
		if (!self.getCommandTags().contains(RideAnythingMod.RIDEABLE_CARROT_TAG)) {
			return;
		}

		Entity firstPassenger = self.getFirstPassenger();
		if (firstPassenger instanceof PlayerEntity player && player.isHolding(Items.CARROT_ON_A_STICK)) {
			cir.setReturnValue(player);
		}
	}
}
