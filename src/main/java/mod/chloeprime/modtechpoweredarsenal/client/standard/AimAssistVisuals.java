package mod.chloeprime.modtechpoweredarsenal.client.standard;

import com.mojang.blaze3d.vertex.PoseStack;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.modtechpoweredarsenal.common.standard.guns.AimAssistMechanic;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class AimAssistVisuals {
    private static @Nullable AimAssistMechanic.AimResult aiming;
    private static long lastAimCalculationTime;
    private static float lastAimCalculationPartialTicks;

    @SubscribeEvent
    public static void onRenderLiving(RenderLivingEvent.Post<?, ?> event) {
        refreshCachedAimingEntity(event.getPartialTick());
        if (aiming == null || aiming.entity() != event.getEntity()) {
            return;
        }
        render(event.getEntity(), aiming.pos(),
                MinecraftHolder.MC.getEntityRenderDispatcher(), event.getRenderer().getFont(),
                event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight());
    }

    private static void refreshCachedAimingEntity(float partialTick) {
        var level = MinecraftHolder.MC.level;
        if (level == null) {
            return;
        }

        long now = level.getGameTime();
        if (lastAimCalculationTime != now || lastAimCalculationPartialTicks != partialTick) {
            lastAimCalculationTime = now;
            lastAimCalculationPartialTicks = partialTick;
            if (!(MinecraftHolder.MC.getCameraEntity() instanceof LivingEntity player)) {
                aiming = null;
                return;
            }
            aiming = Gunsmith.getGunInfo(player.getMainHandItem())
                    // 用PartialTick计算会有bug，传入1反而更稳定
                    .flatMap(gun -> AimAssistMechanic.getAimedPosition(player, gun, 1))
                    .orElse(null);
        }
    }

    private static final Component AIMING_GLYPH = Component.literal("[+]").withStyle(ChatFormatting.RED);

    private static void render(Entity pEntity, Vec3 aimPos,
                               EntityRenderDispatcher dispatcher, Font font,
                               PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        var pDisplayName = AIMING_GLYPH;
        var offset = aimPos.subtract(pEntity.position());
        int i = 0;
        poseStack.pushPose();
        {
            poseStack.translate(offset.x(), offset.y(), offset.z());
            poseStack.mulPose(dispatcher.cameraOrientation());
            poseStack.scale(-0.05F, -0.05F, 0.05F);
            Matrix4f matrix4f = poseStack.last().pose();
            float f2 = (float) (-font.width(pDisplayName) / 2);
            font.drawInBatch(pDisplayName, f2, (float) i, 0xFFFF_FFFF, false, matrix4f, buffer, Font.DisplayMode.SEE_THROUGH, 0, packedLight);
        }
        poseStack.popPose();
    }
}
