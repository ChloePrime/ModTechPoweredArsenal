package mod.chloeprime.modtechpoweredarsenal.common.standard.entities;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nonnull;

public class AbstractShockwave extends Projectile {
    public AbstractShockwave(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public boolean hurt(@Nonnull DamageSource pSource, float pAmount) {
        return false;
    }

    public int getLifeTime() {
        return lifeTime;
    }

    public void setLifeTime(int life) {
        this.lifeTime = life;
    }

    public float getDamage() {
        return damage;
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    public final void setDamage(double damage) {
        setDamage((float) damage);
    }

    private int life;
    private int lifeTime = 40;

    private float damage;

    @Override
    public void tick() {
        super.tick();

        var velocity = getDeltaMovement();
        if (velocity.lengthSqr() > 1e-6) {
            this.setYRot((float)(Math.toDegrees(Mth.atan2(-velocity.x, velocity.z))));
        }

        tickDespawn();
        if (!isAlive()) {
            return;
        }

        move(MoverType.SELF, getDeltaMovement());
        if (horizontalCollision) {
            discard();
        }
        if (!isNoGravity()) {
            setDeltaMovement(getDeltaMovement().add(0, -0.98, 0));
        }
    }

    protected void tickDespawn() {
        ++this.life;
        if (this.life >= lifeTime) {
            this.discard();
        }
    }

    @Override
    protected void defineSynchedData() {

    }

    @Override
    public @Nonnull Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
