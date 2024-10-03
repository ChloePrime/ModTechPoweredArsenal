package mod.chloeprime.modtechpoweredarsenal.mixin.tacz.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.tacz.guns.client.tooltip.ClientGunTooltip;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

/**
 * 解除 tooltip 的行数限制
 */
@Mixin(value = ClientGunTooltip.class, remap = false)
public class GunTooltipRestrictionLifterMixin {
    @WrapOperation(
            method = "getText",
            at = @At(value = "INVOKE", target = "Ljava/util/List;subList(II)Ljava/util/List;"))
    private List<FormattedCharSequence> liftTooltipLineRestriction(List<FormattedCharSequence> instance, int fromIndex, int toIndex, Operation<List<FormattedCharSequence>> original) {
        original.call(instance, fromIndex, toIndex);
        return instance;
    }
}
