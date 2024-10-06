package mod.chloeprime.modtechpoweredarsenal.mixin.tacz.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.tacz.guns.client.particle.BulletHoleParticle;
import mod.chloeprime.modtechpoweredarsenal.common.standard.internal.MaybePlasmaBulletHoleParticle;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.TextureSheetParticle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;

import static mod.chloeprime.modtechpoweredarsenal.common.standard.attachments.PlasmaVisualDispatcher.BULLET_HOLE_HOT_DURATION;

@Mixin(value = BulletHoleParticle.class, remap = false)
public abstract class MixinBulletHoleParticle extends TextureSheetParticle implements MaybePlasmaBulletHoleParticle {
    private @Unique boolean mtpa$isPlasma;

    @SuppressWarnings("DefaultAnnotationParam")
    @WrapOperation(
            method = "render", remap = true,
            at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(II)I"),
            slice = @Slice(
                    from = @At(value = "FIELD", remap = true, target = "Lcom/tacz/guns/client/particle/BulletHoleParticle;age:I"),
                    to = @At(value = "INVOKE", remap = true, target = "Lnet/minecraft/client/renderer/LightTexture;pack(II)I")
            ))
    private int enhanceHotTimeOnPlasmaShots(int originalLight, int zero, Operation<Integer> original) {
        var light = Math.min(15, originalLight + (mtpa$isPlasma ? BULLET_HOLE_HOT_DURATION / 2 : 0));
        return original.call(light, zero);
    }

    @Override
    public void mtpa$setPlasma() {
        mtpa$isPlasma = true;
        rCol = 1;
        gCol = 204F / 255;
        bCol = 51F / 255;
    }

    public MixinBulletHoleParticle(ClientLevel pLevel, double pX, double pY, double pZ) {
        super(pLevel, pX, pY, pZ);
    }
}
