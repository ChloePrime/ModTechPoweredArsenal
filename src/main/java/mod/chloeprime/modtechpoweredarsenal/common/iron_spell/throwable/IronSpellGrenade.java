package mod.chloeprime.modtechpoweredarsenal.common.iron_spell.throwable;

import com.google.common.base.Suppliers;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.entity.mobs.MagicSummon;
import me.xjqsh.lrtactical.entity.ThrowableItemEntity;
import me.xjqsh.lrtactical.item.throwable.ThrowableType;
import me.xjqsh.lrtactical.resource.CommonAssetsManager;
import mod.chloeprime.modtechpoweredarsenal.ModTechPoweredArsenal;
import mod.chloeprime.modtechpoweredarsenal.common.iron_spell.IronSpellProxyImpl;
import mod.chloeprime.modtechpoweredarsenal.common.standard.entities.VirtualCaster;
import mod.chloeprime.modtechpoweredarsenal.common.standard.util.MoreMth;
import mod.chloeprime.modtechpoweredarsenal.common.standard.util.RegistryHelper;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.commons.lang3.reflect.MethodUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static mod.chloeprime.modtechpoweredarsenal.common.iron_spell.throwable.IronSpellGrenadeCompatibilityTags.*;

public class IronSpellGrenade extends ThrowableItemEntity {
    public static EntityType<IronSpellGrenade> TYPE = EntityType.Builder.<IronSpellGrenade>of(IronSpellGrenade::new, MobCategory.MISC)
            .setShouldReceiveVelocityUpdates(true)
            .setTrackingRange(64)
            .setUpdateInterval(1)
            .sized(0.3F, 0.3F)
            .noSave().noSummon().fireImmune()
            .build("iron_spell_grenade");

    @SuppressWarnings("deprecation")
    private static final Supplier<Attribute> SPELL_POWER = RegistryHelper.holder(BuiltInRegistries.ATTRIBUTE, "irons_spellbooks", "spell_power");

    public static ThrowableType<IronSpellGrenadeData, IronSpellGrenade> DATA_TYPE = ThrowableType.Builder
            .<IronSpellGrenadeData, IronSpellGrenade>of()
            .setFactory(IronSpellGrenade::createFromItem)
            .setSerializer((json) -> CommonAssetsManager.GSON.fromJson(json, IronSpellGrenadeData.class))
            .build();

    public static IronSpellGrenade createFromItem(ItemStack stack, LivingEntity thrower, IronSpellGrenadeData data) {
        IronSpellGrenade entity = new IronSpellGrenade(thrower, thrower.level(), data.getEntityData().getLifeTime());
        float initialSpeed = (float)data.getInitialSpeed();
        entity.shootFromRotation(entity, thrower.getXRot(), thrower.getYRot(), 0.0F, initialSpeed, 1.0F);
        entity.setItem(stack);
        entity.setGravity(data.getEntityData().getGravity());
        entity.setBounceFactor(data.getEntityData().getBounceFactor());
        entity.setShouldBounce(data.getEntityData().isShouldBounce());
        entity.setIterativeCastingRange(data.getIterativeCastingRange());
        entity.setGrenadeItem(stack);

        if (!entity.loadSpellOverrideFromNBT(stack, thrower, data)) {
            entity.setSpell(data.getSpellId());
            entity.setSpellLevel(data.getSpellLevel());
        }
        return entity;
    }

    private boolean loadSpellOverrideFromNBT(ItemStack stack, LivingEntity caster, IronSpellGrenadeData data) {
        if (!ISpellContainer.isSpellContainer(stack)) {
            return false;
        }
        var spellStack = IronSpellProxyImpl.getFirstSpell(stack).orElse(null);
        if (spellStack == null) {
            return false;
        }
        AbstractSpell spell = spellStack.getSpell();
        setSpell(spell);

        double casterGenericSpellPower = Optional.ofNullable(SPELL_POWER.get())
                .map(caster::getAttributeValue)
                .orElse(1.0);
        double casterSchoolSpellPower = spell.getSchoolType().getPowerFor(caster);
        double casterSpellPower = casterGenericSpellPower * casterSchoolSpellPower;

        int casterSpellLevel = spell.getLevelFor(spellStack.getLevel(), caster);

        // 无学派手雷的buff会对所有学派的法术生效
        data.adjustSpellPower(
                spell, casterSpellLevel, casterSpellPower,
                this::setSpellLevel, this::setSpellPower
        );
        return true;
    }

    private AbstractSpell spell = SpellRegistry.none();
    private int spellLevel = 1;
    private double spellPower = 1;

    private double iterativeCastingRange = 6;
    private ItemStack grenadeItem;
    private final Supplier<VirtualCaster> caster = Suppliers.memoize(() -> createCaster(level()));
    private final EventHandler handler = new EventHandler();
    private final AtomicInteger isCasterJoiningLevel = new AtomicInteger();
    private boolean casterCreated;
    private boolean keepOwner;
    private Vec3 centerPos = Vec3.ZERO;

    public IronSpellGrenade(LivingEntity thrower, Level level, int lifeTime) {
        this(TYPE, thrower, level, lifeTime);
    }

    public IronSpellGrenade(EntityType<? extends IronSpellGrenade> type, LivingEntity thrower, Level level, int lifeTime) {
        super(type, thrower, level, lifeTime);
    }

    public IronSpellGrenade(EntityType<? extends IronSpellGrenade> type, Level level) {
        super(type, level);
    }

    public AbstractSpell getSpell() {
        return spell;
    }

    public int getSpellLevel() {
        return spellLevel;
    }

    public double getSpellPower() {
        return spellPower;
    }

    public double getIterativeCastingRange() {
        return iterativeCastingRange;
    }

    public void setSpell(AbstractSpell spell) {
        this.spell = spell;
    }

    public void setSpell(ResourceLocation spellId) {
        setSpell(SpellRegistry.getSpell(spellId));
    }

    public void setSpellLevel(int spellLevel) {
        this.spellLevel = spellLevel;
    }

    public void setSpellPower(double spellPower) {
        this.spellPower = spellPower;
    }

    public void setIterativeCastingRange(double iterativeCastingRange) {
        this.iterativeCastingRange = iterativeCastingRange;
    }

    private void setGrenadeItem(ItemStack stack) {
        this.grenadeItem = stack.copy();
    }


    private boolean spellIs(TagKey<AbstractSpell> tag) {
        return RegistryHelper.is(level(), SpellRegistry.SPELL_REGISTRY_KEY, this.spell, tag);
    }

    @Override
    public void tick() {
        super.tick();
        if (casterCreated) {
            var caster = this.caster.get();
            if (caster != null) {
                caster.setPos(this.position().add(0, 0.25, 0));
            }
        }
        if (!level().isClientSide() && spell != null) {
            if (tickCount > 0 && shouldBounce() && spellIs(REQUIRES_ON_GROUND) && getDeltaMovement().lengthSqr() > 0.04) {
                tickCount--;
            }
        }
    }

    @Override
    public void onDeath() {
        explode();
        super.onDeath();
    }

    private void explode() {
        var spell = getSpell();
        if (spell != null && !level().isClientSide()) {
            // buff自身类法术，以周围目标为施法者释放
            if (spellIs(CAST_AS_NEARBY_TARGETS_ON_EXPLODE)) {
                double range = getIterativeCastingRange();
                var explodeCenter = getEyePosition();
                var testArea = AABB.ofSize(getEyePosition(), 0, 0, 0).inflate(range + 2);
                level().getEntities(EntityTypeTest.forClass(LivingEntity.class), testArea, IronSpellGrenade::canEntityBeSelected)
                        .stream()
                        .filter(entity -> minDistanceSqrTo(entity, explodeCenter) <= range * range)
                        .forEach(entity -> forceCast(entity, spell, getSpellLevel()));
                return;
            }

            var caster = this.caster.get();
            if (caster == null) {
                return;
            }

            Entity grenadeOwner = getOwner();
            if (grenadeOwner != null) {
                caster.mtpa$setHateOwner(grenadeOwner);
            }

            prepareCasting(caster);
            gatherCastTargets(caster).forEach(targetPos -> {
                caster.setPos(this.position());
                caster.lookAt(EntityAnchorArgument.Anchor.EYES, targetPos);
                caster.cast(spell, getSpellLevel());
            });
            caster.beginDecay();
        }
    }

    private static boolean canEntityBeSelected(Entity entity) {
        return entity.isPickable() && entity.isAlive();
    }

    private void prepareCasting(LivingEntity caster) {
        // 位置和朝向
        centerPos = position().add(0, 0.25, 0);
        keepOwner = spellIs(KEEP_OWNER_AS_CASTER);

        // 设置魔法强度
        Optional.ofNullable(SPELL_POWER.get())
                .map(caster::getAttribute)
                .ifPresent(spp -> spp.setBaseValue(getSpellPower()));
    }

    private Stream<Vec3> gatherCastTargets(LivingEntity caster) {
        Stream<Vec3> stream = Stream.empty();
        boolean useFallback = true;
        if (spellIs(ITERATE_NEARBY_TARGETS_ON_EXPLODE)) {
            double range = getIterativeCastingRange();
            var explodeCenter = getEyePosition();
            var testArea = AABB.ofSize(getEyePosition(), 0, 0, 0).inflate(range + 2);
            stream = Stream.concat(stream, caster.level().getEntities(caster, testArea, IronSpellGrenade::canEntityBeSelected)
                    .stream()
                    .filter(et -> minDistanceSqrTo(et, explodeCenter) <= range * range)
                    .map(Entity::getEyePosition));
            useFallback = false;
        }

        if (spellIs(ITERATE_RANDOM_POSITION_ON_EXPLODE)) {
            // 让施法中心上移一点，达到些微的空爆效果，
            // 以在不大幅增加弹片数量的情况下改善对地面目标的命中率
            centerPos = centerPos.add(0, 0.75, 0);
            int shrapnel = 8;
            var explodeCenter = getEyePosition();
            stream = Stream.concat(stream, IntStream
                    .range(0, shrapnel)
                    .mapToObj(_i -> explodeCenter.add(MoreMth.randomUnitVector(caster.getRandom()).scale(16))));
            useFallback = false;
        }

        if (useFallback) {
            var lookTarget = shouldBounce()
                    ? caster.getEyePosition().add(0, -1, 0)
                    : caster.getEyePosition().add(this.getDeltaMovement());
            return Stream.of(lookTarget);
        } else {
            return stream;
        }
    }

    private static double minDistanceSqrTo(Entity entity, Vec3 pos) {
        var bb = entity.getBoundingBox();
        return Stream.of(
                        new Vec3(bb.minX, bb.minY, bb.minZ),
                        new Vec3(bb.minX, bb.minY, bb.maxZ),
                        new Vec3(bb.minX, bb.maxY, bb.minZ),
                        new Vec3(bb.minX, bb.maxY, bb.maxZ),
                        new Vec3(bb.maxX, bb.minY, bb.minZ),
                        new Vec3(bb.maxX, bb.minY, bb.maxZ),
                        new Vec3(bb.maxX, bb.maxY, bb.minZ),
                        new Vec3(bb.maxX, bb.maxY, bb.maxZ)
                )
                .mapToDouble(vertex -> pos.distanceToSqr(pos))
                .min()
                .getAsDouble();
    }

    private static void forceCast(LivingEntity caster, AbstractSpell spell, int spellLevel) {
        MagicData magicData = MagicData.getPlayerMagicData(caster);
        if (!spell.checkPreCastConditions(caster.level(), spellLevel, caster, magicData)) {
            return;
        }

        spell.onCast(caster.level(), spellLevel, caster, CastSource.COMMAND, magicData);
        spell.onServerCastComplete(caster.level(), spellLevel, caster, magicData, false);
    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        if (!level().isClientSide()) {
            MinecraftForge.EVENT_BUS.register(handler);
        }
    }

    @Override
    public void onRemovedFromWorld() {
        if (!level().isClientSide()) {
            MinecraftForge.EVENT_BUS.unregister(handler);
        }
    }

    public final class EventHandler {
        @SubscribeEvent
        public void onEntityJoinLevel(EntityJoinLevelEvent event) {
            Entity entity = event.getEntity();
            if (entity.level().isClientSide()) {
                return;
            }
            if (entity == IronSpellGrenade.this || isCasterJoiningLevel.get() > 0) {
                return;
            }
            var caster = IronSpellGrenade.this.caster.get();
            if (caster == null) {
                return;
            }
            if (entity instanceof Projectile projectile && projectile.getOwner() == caster) {
                projectile.setPos(centerPos.add(0, -projectile.getBbHeight() / 2, 0));
                if (!keepOwner) {
                    var grenadeOwner = getOwner();
                    if (grenadeOwner != null) {
                        projectile.setOwner(grenadeOwner);
                    }
                }
            }
            if (entity instanceof MagicSummon summoned && summoned.getSummoner() == caster) {
                entity.setPos(position());
                if (!keepOwner) {
                    var grenadeOwner = getOwner();
                    if (grenadeOwner != null) {
                        try {
                            MethodUtils.invokeMethod(summoned, "setSummoner", grenadeOwner);
                        } catch (ReflectiveOperationException ex) {
                            ModTechPoweredArsenal.LOGGER.warn("Failed to set summoner for grenade caster summoned {}", entity.getDisplayName().getString(), ex);
                        }
                    }
                }
            }
        }
    }

    private @Nullable VirtualCaster createCaster(Level level) {
        if (level.isClientSide() || spellIs(CAST_AS_NEARBY_TARGETS_ON_EXPLODE)) {
            return null;
        }
        VirtualCaster result = setupCaster(level, new VirtualCaster(level, getOwner()));
        casterCreated = true;
        return result;
    }

    private VirtualCaster setupCaster(Level level, @Nonnull VirtualCaster entity) {
        if (!level.isClientSide()) {
            try {
                isCasterJoiningLevel.incrementAndGet();
                level.addFreshEntity(entity);
            } finally {
                isCasterJoiningLevel.decrementAndGet();
            }
        }
        return entity;
    }
}
