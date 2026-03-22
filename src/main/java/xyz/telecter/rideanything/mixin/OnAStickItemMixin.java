package xyz.telecter.rideanything.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.OnAStickItem;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.telecter.rideanything.access.CarrotControllable;

@Mixin(OnAStickItem.class)
public abstract class OnAStickItemMixin {
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void rideAnything$use(World world, PlayerEntity user, Hand hand,
            CallbackInfoReturnable<ActionResult> cir) {
        ItemStack stack = user.getStackInHand(hand);
        if (!stack.isOf(Items.CARROT_ON_A_STICK) || world.isClient()) {
            return;
        }

        Entity vehicle = user.getControllingVehicle();
        if (!user.hasVehicle() || !(vehicle instanceof CarrotControllable controllable)
                || !vehicle.getCommandTags().contains("rideble_carrot")) {
            return;
        }

        if (controllable.rideAnything$consumeOnAStickItem()) {
            ItemStack damagedStack = stack.damage(7, Items.FISHING_ROD, user, hand.getEquipmentSlot());
            cir.setReturnValue(ActionResult.SUCCESS_SERVER.withNewHandStack(damagedStack));
            return;
        }

        user.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
        cir.setReturnValue(ActionResult.PASS);
    }
}
