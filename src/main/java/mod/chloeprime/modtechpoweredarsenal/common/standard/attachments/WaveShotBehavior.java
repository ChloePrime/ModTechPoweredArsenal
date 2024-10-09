package mod.chloeprime.modtechpoweredarsenal.common.standard.attachments;

import com.google.common.collect.Sets;
import com.tacz.guns.api.event.server.AmmoHitBlockEvent;
import com.tacz.guns.api.item.attachment.AttachmentType;
import mod.chloeprime.gunsmithlib.api.common.BulletCreateEvent;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.modtechpoweredarsenal.ModTechPoweredArsenal;
import mod.chloeprime.modtechpoweredarsenal.network.ModNetwork;
import mod.chloeprime.modtechpoweredarsenal.network.S2CPlasmaHitBlock;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.ApiStatus;

import java.util.Set;
import java.util.function.IntSupplier;

@Mod.EventBusSubscriber
public class WaveShotBehavior {
    public static final Set<ResourceLocation> WAVE_MAGS = Sets.newConcurrentHashSet(Set.of(
            ModTechPoweredArsenal.loc("energy_mod_wave")
    ));

    public static boolean isWaveWeapon(ItemStack gun) {
        return Gunsmith.getGunInfo(gun)
                .map(gi -> gi.gunItem().getAttachmentId(gi.gunStack(), AttachmentType.EXTENDED_MAG))
                .filter(WAVE_MAGS::contains)
                .isPresent();
    }
}
