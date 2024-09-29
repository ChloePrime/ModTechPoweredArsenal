package mod.chloeprime.modtechpoweredarsenal.client.standard.guns;

import com.mojang.blaze3d.systems.RenderSystem;
import com.tacz.guns.util.AttachmentDataUtils;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.modtechpoweredarsenal.ModTechPoweredArsenal;
import mod.chloeprime.modtechpoweredarsenal.common.standard.guns.EnergyWeaponBehavior;
import mod.chloeprime.modtechpoweredarsenal.common.standard.guns.EnergyWeaponData;
import mod.chloeprime.modtechpoweredarsenal.common.standard.util.Property;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import java.util.Optional;
import java.util.function.IntSupplier;

public final class EnergyWeaponVisuals {
    public static final class HUD {
        public static int modifyCurrentAmmoDisplay(GuiGraphics gui, float x, float y, int width, int height, IntSupplier oldBehavior) {
            var gun = Optional.ofNullable(Minecraft.getInstance().player)
                    .map(LivingEntity::getMainHandItem)
                    .orElse(ItemStack.EMPTY);
            var isEnergy = EnergyWeaponBehavior.isEnergyWeapon(gun);

            if (isEnergy) {
                gui.pose().pushPose();
                gui.pose().translate(32768, 0, 0);
            }

            var original = oldBehavior.getAsInt();

            if (isEnergy) {
                gui.pose().popPose();
                renderBattery(gui, gun, x, y, width, height);
            }

            return original;
        }

        public static final ResourceLocation BATTERY_BG = ModTechPoweredArsenal.loc("textures/gui/battery_hud_back.png");
        public static final ResourceLocation BATTERY_FG = ModTechPoweredArsenal.loc("textures/gui/battery_hud_front.png");

        public static void renderBattery(GuiGraphics gui, ItemStack gunStack, float x, float y, int width, int height) {
            var gun = Gunsmith.getGunInfo(gunStack).orElse(null);
            if (gun == null) {
                return;
            }

            var curAmmo = gun.getTotalAmmo() + gun.getDummyAmmoAmount();
            var maxAmmo = AttachmentDataUtils.getAmmoCountWithAttachment(gun.gunStack(), gun.index().getGunData());
            var isEmpty = curAmmo == 0;
            var warnLowAmmo = !isEmpty && curAmmo < maxAmmo / 4;

            gui.pose().pushPose();
            {
                var w = 272;
                var h = 112;
                var texW = 272;
                var texH = 112;
                var scaleX = 1F / 16;
                var scaleY = 1F / 16;

                var batteryPercent = Mth.clamp((float) curAmmo / maxAmmo, 0, 1);
                var rx = (int) ((width - 70) / scaleX / 1.5F);
                var ry = (int) ((height - 43) / scaleY / 1.5F);
                var batteryW = (int) Mth.lerp(batteryPercent, 32, 224);

                gui.pose().scale(scaleX, scaleY, 1);

                RenderSystem.disableDepthTest();
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();

                var lowAmmoColor = 1F / 3;

                if (isEmpty) {
                    RenderSystem.setShaderColor(1, lowAmmoColor, lowAmmoColor, 1);
                }
                gui.blit(BATTERY_BG, rx, ry, 0, 0, w, h, texW, texH);
                if (isEmpty) {
                    RenderSystem.setShaderColor(1, 1, 1, 1);
                }

                if (warnLowAmmo) {
                    RenderSystem.setShaderColor(1, lowAmmoColor, lowAmmoColor, 1);
                }
                gui.blit(BATTERY_FG, rx, ry, batteryW, h, 0, 0, batteryW, h, texW, texH);
                if (warnLowAmmo) {
                    RenderSystem.setShaderColor(1, 1, 1, 1);
                }

                RenderSystem.enableDepthTest();
            }
            gui.pose().popPose();
        }

        public static void modifyBackupAmmoDisplay(ItemStack gun, Property<Integer> field) {
            if (!EnergyWeaponBehavior.isEnergyWeapon(gun)) {
                return;
            }

            gun.getCapability(ForgeCapabilities.ENERGY).ifPresent(battery -> {
                EnergyWeaponData.runtime(gun).ifPresent(info -> {
                    field.set(battery.getEnergyStored() / info.energy().energyPerShot());
                });
            });
        }
    }
}
