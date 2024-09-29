package mod.chloeprime.modtechpoweredarsenal.client.standard.guns;

import com.mojang.blaze3d.systems.RenderSystem;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.modtechpoweredarsenal.ModTechPoweredArsenal;
import mod.chloeprime.modtechpoweredarsenal.common.standard.guns.OverheatMechanic;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;

public final class OverheatVisuals {
    public static final class HUD {
        private static final Minecraft MC = Minecraft.getInstance();
        public static final ResourceLocation HEAT_TEX = ModTechPoweredArsenal.loc("textures/gui/overheat_hud.png");

        public static void render(GuiGraphics graphics, int width, int height) {
            var player = MC.player;
            if (player == null) {
                return;
            }
            var gun = Gunsmith.getGunInfo(player.getMainHandItem()).orElse(null);
            if (gun == null) {
                return;
            }

            var curHeat = OverheatMechanic.getHeat(gun.gunStack());
            var maxHeat = OverheatMechanic.getMaxHeat(gun.gunStack()).orElse(-1);

            if (maxHeat <= 0) {
                return;
            }

            graphics.pose().pushPose();
            {
                var w = 128;
                var h = 128;
                var texW = 128;
                var texH = 128;
                var scaleX = 8F / 128;
                var scaleY = 8F / 128;

                var heatPercent = ((float) curHeat / maxHeat);
                var rx = (int) ((width - 31) / scaleX);
                var ry = (int) ((height - 34) / scaleY);
                var coolHeight = (int) (h * (1 - heatPercent));

                graphics.pose().scale(scaleX, scaleY, 1);

                RenderSystem.disableDepthTest();
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();

                RenderSystem.setShaderColor(0, 0, 0, 1);
                graphics.blit(HEAT_TEX, rx, ry, 0, 0, w, h, texW, texH);
                RenderSystem.setShaderColor(1, 1, 1, 1);
                graphics.blit(HEAT_TEX, rx, ry + coolHeight, w, h - coolHeight, 0, coolHeight, w, h - coolHeight, texW, texH);

                RenderSystem.enableDepthTest();
            }
            graphics.pose().popPose();
        }
    }
}
