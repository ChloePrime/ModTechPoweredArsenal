package mod.chloeprime.modtechpoweredarsenal.common.guns;

import com.google.common.collect.Sets;
import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Mod.EventBusSubscriber
public class VvipxCriticalBehavior {
    public static final float CRITICAL_RATE = 0.03F;
    public static Set<ResourceLocation> CRIT_WEAPONS = Sets.newConcurrentHashSet(List.of(
            VvipxModule.loc("m4a1_dark_knight")
    ));

    @SubscribeEvent
    public static void onPreHurt(EntityHurtByGunEvent.Pre event) {
        if (event.getLogicalSide().isClient() || !CRIT_WEAPONS.contains(event.getGunId())) {
            return;
        }
        if (!(event.getHurtEntity() instanceof LivingEntity victim)) {
            return;
        }
        var rng = victim.getRandom();
        if (rng.nextFloat() > CRITICAL_RATE) {
            return;
        }
        event.setBaseAmount(event.getBaseAmount() * 2.5F);
        Optional.ofNullable(event.getAttacker()).ifPresent(VvipxCriticalBehavior::healAttacker);
        if (victim.level() instanceof ServerLevel level) {
            int k = (int) (event.getAmount() * 0.5);
            level.sendParticles(ParticleTypes.DAMAGE_INDICATOR, victim.getX(), victim.getY(0.5), victim.getZ(), k, 0.1, 0.0, 0.1, 0.2);
        }
    }

    private static void healAttacker(LivingEntity shooter) {
        shooter.heal(shooter.getMaxHealth() / 4);
    }
}
