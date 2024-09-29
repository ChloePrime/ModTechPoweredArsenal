package mod.chloeprime.modtechpoweredarsenal.client.standard.guns;

import com.mojang.blaze3d.systems.RenderSystem;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.modtechpoweredarsenal.ModTechPoweredArsenal;
import mod.chloeprime.modtechpoweredarsenal.common.standard.guns.EnergyWeaponBehavior;
import mod.chloeprime.modtechpoweredarsenal.common.standard.guns.EnergyWeaponData;
import mod.chloeprime.modtechpoweredarsenal.common.standard.util.GunHelper;
import mod.chloeprime.modtechpoweredarsenal.common.standard.util.Property;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
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
            var isEnergy = false; //EnergyWeaponBehavior.isEnergyWeapon(gun);

            if (isEnergy) {
                gui.pose().pushPose();
                gui.pose().translate(32768, 0, 0);
            }

            var original = oldBehavior.getAsInt();

            if (isEnergy) {
                gui.pose().popPose();
//                renderHeat(gui, gun, x, y, width, height);
            }

            return original;
        }

        public static final ResourceLocation HEAT_TEX = ModTechPoweredArsenal.loc("textures/gui/overheat_hud.png");

        public static void renderHeat(GuiGraphics gui, ItemStack gunStack, float x, float y, int width, int height) {
            var gun = Gunsmith.getGunInfo(gunStack).orElse(null);
            if (gun == null) {
                return;
            }

            var curAmmo = GunHelper.getTotalAmmo(gun) + 0;
            var maxAmmo = GunHelper.getTotalMagSize(gun);

            gui.pose().pushPose();
            {
                var w = 128;
                var h = 128;
                var texW = 128;
                var texH = 128;
                var scaleX = 8F / 128;
                var scaleY = 8F / 128;

                var heatPercent = 1 - ((float) curAmmo / maxAmmo);
                var rx = (int) ((width - 63) / scaleX / 1.5F);
                var ry = (int) ((height - 43) / scaleY / 1.5F);
                var coolHeight = (int) (h * (1 - heatPercent));

                gui.pose().scale(scaleX, scaleY, 1);

                RenderSystem.disableDepthTest();
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();

                RenderSystem.setShaderColor(0, 0, 0, 1);
                gui.blit(HEAT_TEX, rx, ry, 0, 0, w, h, texW, texH);
                RenderSystem.setShaderColor(1, 1, 1, 1);
                gui.blit(HEAT_TEX, rx, ry + coolHeight, w, h - coolHeight, 0, coolHeight, w, h - coolHeight, texW, texH);

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
                    field.set(battery.getEnergyStored() / info.energy().shootCost());
                });
            });
        }
    }
}
