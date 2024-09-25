package mod.chloeprime.modtechpoweredarsenal.mixin.minecraft;

import com.tacz.guns.api.item.IGun;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.extensions.IForgeItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public abstract class MakeGunEnchantableMixin implements IForgeItem {
    @Inject(method = "isEnchantable", at = @At("HEAD"), cancellable = true)
    private void gunIsEnchantable(ItemStack pStack, CallbackInfoReturnable<Boolean> cir) {
        if (IGun.getIGunOrNull(pStack) != null) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getEnchantmentValue", at = @At("HEAD"), cancellable = true)
    private void giveEnchantmentValue(CallbackInfoReturnable<Integer> cir) {
        if (this instanceof IGun) {
            cir.setReturnValue(ArmorMaterials.IRON.getEnchantmentValue());
        }
    }
}
