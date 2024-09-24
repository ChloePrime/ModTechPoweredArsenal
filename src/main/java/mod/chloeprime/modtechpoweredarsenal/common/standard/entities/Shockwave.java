package mod.chloeprime.modtechpoweredarsenal.common.standard.entities;

import com.tacz.guns.util.ExplodeUtil;
import mod.chloeprime.modtechpoweredarsenal.MTPA;
import mod.chloeprime.modtechpoweredarsenal.client.MtpaClient;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;

import java.util.List;

public class Shockwave extends AbstractShockwave {
    public Shockwave(Level level, Entity owner) {
        this(MTPA.Entities.SHOCKWAVE.get(), level);
        setOwner(owner);
    }

    public Shockwave(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        setDamage(6);
    }

    public double getSize() {
        return 2.5;
    }

    @Override
    public void tick() {
        super.tick();
        if (!isAlive()) {
            return;
        }
        var level = level();
        if (level.isClientSide) {
            MtpaClient.onShockwaveClientTick(this, level);
        } else {
            var chainDmg = getChainReactionDamage();
            var chainRange = getChainReactionRange();
            var hasChainAct = chainDmg>0&&chainRange>0;
            for (var victim : getOverlappingEntities()) {
                var damageType = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.SONIC_BOOM);
                var damageSrc = new DamageSource(damageType, this, getOwner());
                var hurt = victim.hurt(damageSrc, getDamage());
                if (!hasChainAct || !hurt || victim.isAlive()) {
                    continue;
                }
                // 连锁反应
                ExplodeUtil.createExplosion(
                        getOwner(), this,
                        chainDmg, chainRange,
                        true, false, victim.position()
                );
            }
        }
    }

    private List<LivingEntity> getOverlappingEntities() {
        var bb = getBoundingBox();
        var inflate = (getSize() - Math.max(bb.getXsize(), bb.getZsize())) / 2;
        return level().getEntitiesOfClass(LivingEntity.class, bb.inflate(inflate, 0, inflate));
    }
}
