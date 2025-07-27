package mod.chloeprime.modtechpoweredarsenal.mixin.minecraft;

import mod.chloeprime.modtechpoweredarsenal.ModLoadStatus;
import mod.chloeprime.modtechpoweredarsenal.common.standard.throwable.DispenserThrowGrenadeBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DispenserBlock.class)
public class MixinDispenserBlock {
    @Inject(method = "getDispenseMethod", at = @At("HEAD"), cancellable = true)
    private void getDispenseMethodForMTPAGrenades(ItemStack stack, CallbackInfoReturnable<DispenseItemBehavior> cir) {
        if (!ModLoadStatus.LRTAC_INSTALLED) {
            return;
        }
        DispenserThrowGrenadeBehavior.onGetDispenseMethodHook(stack).ifPresent(cir::setReturnValue);
    }
}
