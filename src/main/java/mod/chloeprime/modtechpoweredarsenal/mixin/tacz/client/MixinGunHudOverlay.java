package mod.chloeprime.modtechpoweredarsenal.mixin.tacz.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.gui.overlay.GunHudOverlay;
import com.tacz.guns.client.resource.index.ClientGunIndex;
import mod.chloeprime.modtechpoweredarsenal.client.standard.guns.EnergyWeaponVisuals;
import mod.chloeprime.modtechpoweredarsenal.common.standard.util.Property;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GunHudOverlay.class, remap = false)
public class MixinGunHudOverlay {
    @WrapOperation(
            method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;FFIZ)I"),
            slice = @Slice(
                    from = @At(value = "INVOKE", target = "Lcom/tacz/guns/api/item/IGun;getCurrentAmmoCount(Lnet/minecraft/world/item/ItemStack;)I"),
                    to = @At(value = "FIELD", opcode = Opcodes.GETSTATIC, target = "Lcom/tacz/guns/client/gui/overlay/GunHudOverlay;cacheInventoryAmmoCount:I")
            ))
    private int energyWeaponShowHeat(
            GuiGraphics gui, Font pFont, @Nullable String pText, float pX, float pY, int pColor, boolean pDropShadow, Operation<Integer> original,
            ForgeGui forgeGui, GuiGraphics graphics, float partialTick, int width, int height
    ) {
        return EnergyWeaponVisuals.HUD.modifyCurrentAmmoDisplay(gui, pX, pY, width, height, () -> original.call(gui, pFont, pText, pX, pY, pColor, pDropShadow));
    }

    @Inject(method = "handleCacheCount", at = @At("TAIL"))
    private static void energyWeaponShowTotalAmmo(LocalPlayer player, ItemStack stack, ClientGunIndex gunIndex, IGun iGun, CallbackInfo ci) {
        EnergyWeaponVisuals.HUD.modifyBackupAmmoDisplay(stack, Property.of(() -> cacheInventoryAmmoCount, v -> cacheInventoryAmmoCount = v));
    }

    @Shadow private static int cacheInventoryAmmoCount;
}
