package mod.chloeprime.modtechpoweredarsenal.common.standard.guns;

import com.google.common.collect.Sets;
import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
import com.tacz.guns.api.item.IGun;
import mod.chloeprime.gunsmithlib.api.common.BulletCreateEvent;
import mod.chloeprime.modtechpoweredarsenal.ModTechPoweredArsenal;
import mod.chloeprime.modtechpoweredarsenal.common.lightland.MtpaL2Module;
import mod.chloeprime.modtechpoweredarsenal.network.ModNetwork;
import mod.chloeprime.modtechpoweredarsenal.network.S2CEnchantedHit;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;
import java.util.Set;

@Mod.EventBusSubscriber
public class Albert01Behavior {
    public static final Set<ResourceLocation> VALID_GUNS = Sets.newConcurrentHashSet(Set.of(
            MtpaL2Module.loc("albert_01")
    ));

    public static final String PD_KEY = ModTechPoweredArsenal.loc("smite_v").toString();

    @SubscribeEvent
    public static void onBulletCreate(BulletCreateEvent event) {
        if (event.getBullet().level().isClientSide) {
            return;
        }
        var gun = event.getGun();
        var isValidGun = Optional.ofNullable(IGun.getIGunOrNull(gun))
                .map(kun -> kun.getGunId(gun))
                .filter(VALID_GUNS::contains)
                .isPresent();
        if (!isValidGun) {
            return;
        }
        event.getBullet().getPersistentData().putBoolean(PD_KEY, true);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void smite(EntityHurtByGunEvent.Pre event) {
        if (event.getLogicalSide().isClient()) {
            return;
        }
        if (!(event.getHurtEntity() instanceof LivingEntity victim)) {
            return;
        }
        if (victim.getMobType() != MobType.UNDEAD) {
            return;
        }
        if (!event.getBullet().getPersistentData().getBoolean(PD_KEY)) {
            return;
        }
        event.setBaseAmount(event.getBaseAmount() + 12.5F);
        ModNetwork.sendToNearby(new S2CEnchantedHit(victim.getId()), victim);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void antiRegen(EntityHurtByGunEvent.Pre event) {
        if (event.getLogicalSide().isClient()) {
            return;
        }
        if (!(event.getHurtEntity() instanceof LivingEntity victim)) {
            return;
        }
        if (event.getBullet().getPersistentData().getBoolean(Albert01Behavior.PD_KEY)) {
            var curse = MtpaL2Module.getAntiRegenEffect();
            if (!victim.hasEffect(curse)) {
                ModNetwork.sendToNearby(new S2CEnchantedHit(victim.getId()), victim);
            }
            var HALF_MINUTE = 20 * 30;
            victim.addEffect(new MobEffectInstance(curse, HALF_MINUTE));
        }
    }
}
