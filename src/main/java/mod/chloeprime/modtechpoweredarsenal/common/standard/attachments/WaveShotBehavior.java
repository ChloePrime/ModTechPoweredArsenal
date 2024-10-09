package mod.chloeprime.modtechpoweredarsenal.common.standard.attachments;

import com.google.common.collect.Sets;
import com.tacz.guns.api.item.attachment.AttachmentType;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.modtechpoweredarsenal.ModTechPoweredArsenal;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.common.Mod;

import java.util.Set;

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
