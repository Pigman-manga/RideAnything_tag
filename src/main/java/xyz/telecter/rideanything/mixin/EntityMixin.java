package xyz.telecter.rideanything.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import xyz.telecter.rideanything.RideAnythingMod;

@Mixin(Entity.class)
public abstract class EntityMixin {
	@Inject(method = "getControllingPassenger", at = @At("HEAD"), cancellable = true)
	private void rideanything$allowCarrotControl(CallbackInfoReturnable<LivingEntity> cir) {
		Entity self = (Entity) (Object) this;
		if (!(self instanceof LivingEntity)) {
			return;
		}

		Entity passenger = self.getFirstPassenger();
		if (!(passenger instanceof PlayerEntity player)) {
			return;
		}

		if (!RideAnythingMod.canUseCarrotControl(self)) {
			return;
		}

		if (player.isHolding(Items.CARROT_ON_A_STICK)) {
			cir.setReturnValue(player);
		}
	}
}
