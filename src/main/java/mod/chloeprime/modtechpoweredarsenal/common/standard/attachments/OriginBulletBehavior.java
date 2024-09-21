package mod.chloeprime.modtechpoweredarsenal.common.standard.attachments;

import com.google.common.collect.Sets;
import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
import com.tacz.guns.api.event.common.GunDamageSourcePart;
import com.tacz.guns.api.item.attachment.AttachmentType;
import mod.chloeprime.gunsmithlib.api.common.BulletCreateEvent;
import mod.chloeprime.modtechpoweredarsenal.ModTechPoweredArsenal;
import mod.chloeprime.modtechpoweredarsenal.common.standard.util.DamageSourceUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Set;

import static mod.chloeprime.modtechpoweredarsenal.common.standard.util.GunPreconditions.*;

@Mod.EventBusSubscriber
public class OriginBulletBehavior {
    public static Set<ResourceLocation> VALID_ATTACHMENTS = Sets.newConcurrentHashSet(List.of(
            ModTechPoweredArsenal.loc("ammo_mod_antimagic")
    ));
    public static Set<TagKey<DamageType>> IMPL_TAGS = Set.of(
            DamageTypeTags.BYPASSES_ENCHANTMENTS,
            DamageTypeTags.BYPASSES_RESISTANCE,
            DamageTypeTags.BYPASSES_EFFECTS
    );

    public static final String PD_KEY = ModTechPoweredArsenal.MODID+":anti_magic";

    @SubscribeEvent
    public static void onBulletCreate(BulletCreateEvent event) {
        var hasAnyValid = VALID_ATTACHMENTS.stream().anyMatch(
                attachId -> hasAttachmentInstalled(event.getGun(), AttachmentType.EXTENDED_MAG, attachId)
        );
        if (!hasAnyValid) {
            return;
        }
        event.getBullet().getPersistentData().putBoolean(PD_KEY, true);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onPreHurt(EntityHurtByGunEvent.Pre event) {
        if (event.getBullet() == null || !event.getBullet().getPersistentData().getBoolean(PD_KEY)) {
            return;
        }
        var source1 = event.getDamageSource(GunDamageSourcePart.NON_ARMOR_PIERCING);
        var source2 = event.getDamageSource(GunDamageSourcePart.ARMOR_PIERCING);
        IMPL_TAGS.forEach(tag -> {
            DamageSourceUtil.addTag(source1, tag);
            DamageSourceUtil.addTag(source2, tag);
        });
    }
}
