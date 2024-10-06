package mod.chloeprime.modtechpoweredarsenal.common.standard.attachments;

import com.google.common.collect.Sets;
import com.tacz.guns.api.event.server.AmmoHitBlockEvent;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.entity.EntityKineticBullet;
import mod.chloeprime.gunsmithlib.api.common.BulletCreateEvent;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.modtechpoweredarsenal.ModTechPoweredArsenal;
import mod.chloeprime.modtechpoweredarsenal.network.ModNetwork;
import mod.chloeprime.modtechpoweredarsenal.network.S2CPlasmaHitBlock;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.ApiStatus;
import org.spongepowered.asm.mixin.Unique;

import java.util.Set;
import java.util.function.IntSupplier;

@Mod.EventBusSubscriber
public class PlasmaVisualDispatcher {
    public static final Set<ResourceLocation> PLASMA_MAGS = Sets.newConcurrentHashSet(Set.of(
            ModTechPoweredArsenal.loc("energy_mod_plasma")
    ));

    public static final int BULLET_HOLE_HOT_DURATION = 60;

    public static boolean usePlasmaVisual(ItemStack gun) {
        return Gunsmith.getGunInfo(gun)
                .map(gi -> gi.gunItem().getAttachmentId(gi.gunStack(), AttachmentType.EXTENDED_MAG))
                .filter(PLASMA_MAGS::contains)
                .isPresent();
    }

    public static final String PDK_HAS_PLASMA_VISUAL = ModTechPoweredArsenal.loc("has_plasma_visual").toString();

    @SubscribeEvent
    public static void onBulletCreate(BulletCreateEvent event) {
        if (event.getBullet().level().isClientSide) {
            return;
        }
        if (!usePlasmaVisual(event.getGun())) {
            return;
        }
        event.getBullet().getPersistentData().putBoolean(PDK_HAS_PLASMA_VISUAL, true);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onAmmoHitBlock(AmmoHitBlockEvent event) {
        if (event.getLevel().isClientSide) {
            return;
        }
        var bullet = event.getAmmo();
        if (bullet.getPersistentData().getBoolean(PDK_HAS_PLASMA_VISUAL)) {
            var hit = event.getHitResult();
            ModNetwork.sendToNearby(
                    new S2CPlasmaHitBlock(
                            hit.getLocation(), hit.getDirection(), hit.getBlockPos(),
                            bullet.getAmmoId(), bullet.getGunId()
                    ),
                    bullet
            );
        }
    }

    private static final ResourceLocation BULLET_HOLE_ID = new ResourceLocation("tacz", "bullet_hole");

    @ApiStatus.Internal
    public static int onServerSpawnBulletHole(Projectile bullet, ParticleOptions particle, IntSupplier original) {
        if (bullet.getPersistentData().getBoolean(PDK_HAS_PLASMA_VISUAL)) {
            if (BULLET_HOLE_ID.equals(ForgeRegistries.PARTICLE_TYPES.getKey(particle.getType()))) {
                return 0;
            }
        }
        return original.getAsInt();
    }
}
