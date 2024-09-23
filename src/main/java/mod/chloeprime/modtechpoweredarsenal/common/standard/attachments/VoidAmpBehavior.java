package mod.chloeprime.modtechpoweredarsenal.common.standard.attachments;

import com.google.common.collect.Sets;
import com.tacz.guns.api.entity.KnockBackModifier;
import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
import com.tacz.guns.api.event.common.GunDamageSourcePart;
import com.tacz.guns.api.item.attachment.AttachmentType;
import mod.chloeprime.gunsmithlib.api.common.BulletCreateEvent;
import mod.chloeprime.modtechpoweredarsenal.ModLoadStatus;
import mod.chloeprime.modtechpoweredarsenal.ModTechPoweredArsenal;
import mod.chloeprime.modtechpoweredarsenal.common.lightland.MtpaL2Module;
import mod.chloeprime.modtechpoweredarsenal.common.lightland.attachments.VoidAmpBehaviorL2H;
import mod.chloeprime.modtechpoweredarsenal.common.standard.util.DamageSourceUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Set;

import static mod.chloeprime.modtechpoweredarsenal.common.standard.util.GunPreconditions.hasAttachmentInstalled;

@Mod.EventBusSubscriber
public class VoidAmpBehavior {
    public static Set<ResourceLocation> VALID_ATTACHMENTS = Sets.newConcurrentHashSet(List.of(
            MtpaL2Module.loc("muzzle_mod_void_amp")
    ));
    public static final TagKey<DamageType> VOID_MARK = TagKey.create(Registries.DAMAGE_TYPE, ModTechPoweredArsenal.loc("void_mark"));

    public static Set<TagKey<DamageType>> IMPL_TAGS = Set.of(
            VOID_MARK,
            DamageTypeTags.BYPASSES_ARMOR,
            DamageTypeTags.BYPASSES_SHIELD,
            DamageTypeTags.BYPASSES_INVULNERABILITY,
            DamageTypeTags.BYPASSES_COOLDOWN,
            DamageTypeTags.BYPASSES_ENCHANTMENTS,
            DamageTypeTags.BYPASSES_RESISTANCE,
            DamageTypeTags.BYPASSES_EFFECTS
    );

    public static final String PD_KEY = ModTechPoweredArsenal.MODID + ":void_amped";

    static {
        if (ModLoadStatus.L2H_INSTALLED) {
            MinecraftForge.EVENT_BUS.register(VoidAmpBehaviorL2H.class);
        }
    }

    @SubscribeEvent
    public static void onBulletCreate(BulletCreateEvent event) {
        var hasAnyValid = VALID_ATTACHMENTS.stream().anyMatch(
                attachId -> hasAttachmentInstalled(event.getGun(), AttachmentType.MUZZLE, attachId)
        );
        if (!hasAnyValid) {
            return;
        }
        event.getBullet().getPersistentData().putBoolean(PD_KEY, true);
    }

    private static final ResourceLocation PARAM_ORIGINAL_DAMAGE = ModTechPoweredArsenal.loc("original_damage");

    @SubscribeEvent
    public static void onPostGunshot(EntityHurtByGunEvent.Post event) {
        if (event.getLogicalSide().isClient() || !(event.getHurtEntity() instanceof LivingEntity victim)) {
            return;
        }
        if (event.getBullet() == null || !event.getBullet().getPersistentData().getBoolean(PD_KEY)) {
            return;
        }
        var source0 = event.getDamageSource(GunDamageSourcePart.ARMOR_PIERCING);
        var amount0 = event.getBaseAmount() * (event.isHeadShot() ? event.getHeadshotMultiplier() : 1);
        var source = new DamageSource(source0.typeHolder(), source0.getDirectEntity(), source0.getEntity(), source0.getSourcePosition());
        var amount = amount0 * 0.3F;

        DamageSourceUtil.addTags(source, IMPL_TAGS);
        DamageSourceUtil.setParam(source, PARAM_ORIGINAL_DAMAGE, amount);
        KnockBackModifier.fromLivingEntity(victim).setKnockBackStrength(0);
        victim.hurt(source, amount);
    }

    /**
     * 类似莱特兰虚空之触的真实伤害
     */
    @Mod.EventBusSubscriber
    public static class DamageLocker {
        @SubscribeEvent(receiveCanceled = true, priority = EventPriority.LOWEST)
        public static void onPostAttack(LivingAttackEvent event) {
            fixEvent(event, event.getSource());
        }

        @SubscribeEvent(receiveCanceled = true, priority = EventPriority.LOWEST)
        public static void onPostHurt(LivingHurtEvent event) {
            fixEvent(event, event.getSource());
            fixAmount(event::getAmount, event::setAmount, event.getSource());
        }

        @SubscribeEvent(receiveCanceled = true, priority = EventPriority.LOWEST)
        public static void onPostCommit(LivingDamageEvent event) {
            fixEvent(event, event.getSource());
            fixAmount(event::getAmount, event::setAmount, event.getSource());
        }

        private static void fixEvent(Event event, DamageSource source) {
            if (event.isCanceled() && source.is(VoidAmpBehavior.VOID_MARK)) {
                event.setCanceled(false);
            }
        }

        private static void fixAmount(FloatSupplier getDamage, FloatConsumer setDamage, DamageSource source) {
            DamageSourceUtil.getParam(source, PARAM_ORIGINAL_DAMAGE).ifPresent(amount -> {
                if (getDamage.getAsFloat() < amount) {
                    setDamage.accept((float) amount);
                }
            });
        }

        @FunctionalInterface
        private interface FloatSupplier {
            float getAsFloat();
        }

        @FunctionalInterface
        private interface FloatConsumer {
            void accept(float value);
        }
    }
}
