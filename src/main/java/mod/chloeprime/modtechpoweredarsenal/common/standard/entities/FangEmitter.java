package mod.chloeprime.modtechpoweredarsenal.common.standard.entities;

import mod.chloeprime.modtechpoweredarsenal.MTPA;
import mod.chloeprime.modtechpoweredarsenal.ModTechPoweredArsenal;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;

public class FangEmitter extends AbstractShockwave {
    public FangEmitter(Level level, Entity owner) {
        this(MTPA.Entities.FANG_EMITTER.get(), level);
        setOwner(owner);
    }

    public FangEmitter(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public void setFangData(CompoundTag data) {
        this.fangData = Objects.requireNonNull(data);
    }

    public static final String FANG_DAMAGE_PDK = ModTechPoweredArsenal.loc("damage_override").toString();
    private static final int spawnInterval = 3;
    private CompoundTag fangData;
    private int spawnCounter;

    @Override
    public void tick() {
        super.tick();
        spawnCounter++;
        if (spawnCounter >= 0) {
            spawnCounter -= spawnInterval;
            spawnFang();
        }
    }

    @SuppressWarnings("DataFlowIssue")
    private void spawnFang() {
        var level = level();
        if (!onGround() || level.isClientSide) {
            return;
        }
        var owner = getOwner() instanceof LivingEntity le ? le : null;
        var fang = new EvokerFangs(level, getX(), getY(), getZ(), (float) Math.toRadians(getYRot() + 90), 0, owner);
        var dmg = getDamage();
        if (dmg > 0) {
            fang.getPersistentData().putFloat(FANG_DAMAGE_PDK, dmg);
        }
        Optional.ofNullable(fangData).ifPresent(data -> {
            var pd = fang.getPersistentData();
            for (var key : data.getAllKeys()) {
                pd.put(key, data.get(key));
            }
        });
        level.addFreshEntity(fang);
    }

    public static OptionalDouble getDamageFor(EvokerFangs fangs) {
        return fangs.getPersistentData().contains(FANG_DAMAGE_PDK, Tag.TAG_ANY_NUMERIC)
                ? OptionalDouble.of(fangs.getPersistentData().getFloat(FANG_DAMAGE_PDK))
                : OptionalDouble.empty();
    }
}
