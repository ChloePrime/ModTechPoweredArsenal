package mod.chloeprime.modtechpoweredarsenal.mixin.tacz;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.tacz.guns.api.item.gun.FireMode;
import com.tacz.guns.item.ModernKineticGunItem;
import com.tacz.guns.resource.index.CommonGunIndex;
import mod.chloeprime.modtechpoweredarsenal.common.attachments.BumpfireStockBehavior;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(value = ModernKineticGunItem.class, remap = false)
public class MixinGun {
    @ModifyExpressionValue(
            method = "lambda$fireSelect$4",
            at = @At(value = "INVOKE", target = "Lcom/tacz/guns/resource/pojo/data/gun/GunData;getFireModeSet()Ljava/util/List;"))
    private List<FireMode> bumpfireStock(List<FireMode> original, ItemStack gunItem, CommonGunIndex gunIndex) {
        return BumpfireStockBehavior.injectFireMode(gunItem, gunIndex, original);
    }
}
