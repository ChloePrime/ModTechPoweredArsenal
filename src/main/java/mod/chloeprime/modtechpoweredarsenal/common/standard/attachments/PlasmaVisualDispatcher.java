package mod.chloeprime.modtechpoweredarsenal.common.standard.attachments;

import com.google.common.collect.Sets;
import com.tacz.guns.api.event.server.AmmoHitBlockEvent;
import com.tacz.guns.api.item.attachment.AttachmentType;
import mod.chloeprime.gunsmithlib.api.common.BulletCreateEvent;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.modtechpoweredarsenal.ModTechPoweredArsenal;
import mod.chloeprime.modtechpoweredarsenal.network.ModNetwork;
import mod.chloeprime.modtechpoweredarsenal.network.S2CPlasmaHitBlock;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Set;

@Mod.EventBusSubscriber
public class PlasmaVisualDispatcher {
    public static final Set<ResourceLocation> PLASMA_MAGS = Sets.newConcurrentHashSet(Set.of(
            ModTechPoweredArsenal.loc("energy_mod_plasma")
    ));

    public static final String PDK_HAS_PLASMA_VISUAL = ModTechPoweredArsenal.loc("has_plasma_visual").toString();

    @SubscribeEvent
    public static void onBulletCreate(BulletCreateEvent event) {
        if (event.getBullet().level().isClientSide) {
            return;
        }
        var gun = Gunsmith.getGunInfo(event.getGun()).orElse(null);
        if (gun == null) {
            return;
        }
        var mag = gun.gunItem().getAttachmentId(gun.gunStack(), AttachmentType.EXTENDED_MAG);
        if (!PLASMA_MAGS.contains(mag)) {
            return;
        }
        event.getBullet().getPersistentData().putBoolean(PDK_HAS_PLASMA_VISUAL, true);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onAmmoHitBlock(AmmoHitBlockEvent event) {
        if (event.getAmmo().getPersistentData().getBoolean(PDK_HAS_PLASMA_VISUAL)) {
            ModNetwork.sendToNearby(
                    new S2CPlasmaHitBlock(event.getHitResult().getLocation(), event.getHitResult().getDirection()),
                    event.getAmmo()
            );
        }
    }
}
