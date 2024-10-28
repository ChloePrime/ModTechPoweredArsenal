package mod.chloeprime.modtechpoweredarsenal.client.lightland.guns;

import com.tacz.guns.api.event.common.GunFireEvent;
import mod.chloeprime.aaaparticles.api.common.AAALevel;
import mod.chloeprime.aaaparticles.api.common.ParticleEmitterInfo;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.modtechpoweredarsenal.ModTechPoweredArsenal;
import mod.chloeprime.modtechpoweredarsenal.client.standard.MinecraftHolder;
import mod.chloeprime.modtechpoweredarsenal.common.standard.guns.FlamethrowerBehavior;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class FlamethrowerVisuals {
    public static final ResourceLocation COMMON_FLAME_EFFEK_ID = ModTechPoweredArsenal.loc("explosion/flamethrower");
    public static final ResourceLocation SOUL_FLAME_EFFEK_ID = ModTechPoweredArsenal.loc("explosion/flamethrower_soul");

    @SubscribeEvent
    public static void onClientShot(GunFireEvent event) {
        if (event.getLogicalSide().isServer()) {
            return;
        }
        var gun = Gunsmith.getGunInfo(event.getGunItemStack()).orElse(null);
        if (gun == null) {
            return;
        }
        int type = FlamethrowerBehavior.getType(gun);
        if (type == 0 || event.getShooter().isUnderWater()) {
            return;
        }
        var shooter = event.getShooter();
        if (shooter == null || !shooter.level().isClientSide) {
            return;
        }
        var particle = type == FlamethrowerBehavior.BULLET_FLAME_TYPE_SOUL
                ? SOUL_FLAME_EFFEK_ID
                : COMMON_FLAME_EFFEK_ID;
        var pei = ParticleEmitterInfo.create(shooter.level(), particle)
                .bindOnEntity(shooter)
                .useEntityHeadSpace()
                .entitySpaceRelativePosition(0.06, -0.28, -0.8);
        MinecraftHolder.MC.execute(() -> {
            if (shooter.isAlive()) {
                AAALevel.addParticle(shooter.level(), true, pei);
            }
        });
    }
}
