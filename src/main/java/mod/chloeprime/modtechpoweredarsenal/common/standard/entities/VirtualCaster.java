package mod.chloeprime.modtechpoweredarsenal.common.standard.entities;

import com.google.common.base.MoreObjects;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.capabilities.magic.TargetEntityCastData;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import mod.chloeprime.modtechpoweredarsenal.common.standard.internal.HateTransferable;
import mod.chloeprime.modtechpoweredarsenal.common.standard.util.MoreMth;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.commands.arguments.EntityAnchorArgument.Anchor;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.JumpControl;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class VirtualCaster extends AbstractSpellCastingMob implements TraceableEntity, HateTransferable {
    public static final EntityType<VirtualCaster> TYPE = EntityType.Builder
            .<VirtualCaster>of(VirtualCaster::new, MobCategory.MISC)
            .sized(1F, 1F)
            .clientTrackingRange(64)
            .fireImmune()
            .noSave()
            .build("virtual_caster");

    public VirtualCaster(Level level, @Nullable Entity owner) {
        this(TYPE, level);
        setOwner(owner);
        if (owner != null) {
            setPos(owner.position());
        }
    }

    public VirtualCaster(EntityType<? extends AbstractSpellCastingMob> entityType, Level level) {
        super(entityType, level);
        lookControl = new LookControl(this) {
            @Override
            public void tick() {
            }
        };
        moveControl = new MoveControl(this) {
            @Override
            public void tick() {
            }
        };
        jumpControl = new JumpControl(this) {
            @Override
            public void tick() {
            }
        };
    }

    private @Nullable UUID ownerUUID;
    private @Nullable Entity cachedOwner;
    private int ticksDecayed;
    private boolean decaying;
    private boolean randomizeHeadDirection;
    private boolean isSlave;

    public void cast(AbstractSpell spell, int spellLevel) {
        if (spell.getCastType() == CastType.CONTINUOUS) {
            initiateCastSpell(spell, spellLevel);
            randomizeHeadDirection = true;
            if (!isSlave && !level().isClientSide()) {
                // 召唤几个从属施法者，朝着360度随机施法，
                // 让场面更壮观一点
                var slaveCount = getRandom().nextInt(4, 7) - 1;
                for (int i = 0; i < slaveCount; i++) {
                    var slave = createSlaveCaster();
                    slave.cast(spell, spellLevel);
                    slave.beginDecay();
                }
            }
        } else {
            MagicData magicData = MagicData.getPlayerMagicData(this);
            if (!spell.checkPreCastConditions(level(), spellLevel, this, magicData)) {
                return;
            }
            if (magicData.getAdditionalCastData() == null) {
                magicData.setAdditionalCastData(new TargetEntityCastData(this));
            }
            spell.onCast(level(), spellLevel, this, CastSource.MOB, magicData);
            spell.onServerCastComplete(level(), spellLevel, this, magicData, false);
        }
    }

    public void beginDecay() {
        decaying = true;
    }

    public @Nonnull VirtualCaster createSlaveCaster() {
        var slave = new VirtualCaster(level(), getOwner());
        slave.isSlave = true;
        var spellPowerAttribute = AttributeRegistry.SPELL_POWER.get();
        var spellPowerInstance = slave.getAttribute(spellPowerAttribute);
        if (spellPowerInstance != null) {
            spellPowerInstance.setBaseValue(this.getAttributeValue(spellPowerAttribute));
        }
        level().addFreshEntity(slave);
        slave.setPos(this.position());
        return slave;
    }

    @Override
    public void tick() {
        if (!level().isClientSide()) {
            if (decaying) {
                ticksDecayed++;
                if (ticksDecayed > 200) {
                    discard();
                }
            }
            if (isCasting() && randomizeHeadDirection) {
                randomizeHeadDirection();
            }
        }
        super.tick();
    }

    public void randomizeHeadDirection() {
        lookAt(Anchor.EYES, getEyePosition().add(MoreMth.randomUnitVector(getRandom()).scale(16)));
    }

    public void setOwner(@Nullable Entity owner) {
        if (owner != null) {
            this.ownerUUID = owner.getUUID();
            this.cachedOwner = owner;
        }
    }

    @Override
    public @Nullable Entity getOwner() {
        if (cachedOwner != null && !cachedOwner.isRemoved()) {
            return cachedOwner;
        } else if (this.ownerUUID != null && this.level() instanceof ServerLevel serverLevel) {
            return this.cachedOwner = serverLevel.getEntity(this.ownerUUID);
        } else {
            return null;
        }
    }

    @Override
    public Entity mtpa$getHateOwner() {
        return MoreObjects.firstNonNull(getOwner(), this);
    }

    @Override
    public void mtpa$setHateOwner(@Nullable Entity owner) {
        setOwner(owner);
    }

    public static AttributeSupplier.Builder createVirtualCasterAttributes() {
        return Player.createAttributes()
                .add(ForgeMod.ENTITY_REACH.get(), 16)
                .add(Attributes.MAX_HEALTH, 100_0000)
                .add(Attributes.FOLLOW_RANGE, 0);
    }

    /**
     * 初始化实体 Attribute
     */
    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class AttributeInitializer {
        @SubscribeEvent
        public static void onCreateAttributes(EntityAttributeCreationEvent event) {
            event.put(TYPE, createVirtualCasterAttributes().build());
        }
    }

    // Entity

    @Override
    public Component getDisplayName() {
        return Optional.ofNullable(getOwner())
                .filter(owner -> owner != this)
                .map(Entity::getDisplayName)
                .orElseGet(super::getDisplayName);
    }

    @Override
    protected float getStandingEyeHeight(Pose pPose, EntityDimensions pDimensions) {
        return 0.5F;
    }

    @Override
    public boolean isAlliedTo(Entity target) {
        Entity owner = getOwner();
        return owner != null ? (owner == target || owner.isAlliedTo(target)) : super.isAlliedTo(target);
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return true;
    }

    @Override
    protected boolean canAddPassenger(Entity p_265289_) {
        return false;
    }

    @Override
    @SuppressWarnings("deprecation")
    protected boolean couldAcceptPassenger() {
        return false;
    }

    @Override
    public PushReaction getPistonPushReaction() {
        return PushReaction.IGNORE;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void push(Entity pEntity) {
    }

    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    public Vec3 getDeltaMovement() {
        return Vec3.ZERO;
    }

    @Override
    public void setDeltaMovement(Vec3 pDeltaMovement) {
    }

    // LivingEntity

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        return false;
    }

    @Override
    public void kill() {
        remove(Entity.RemovalReason.KILLED);
        gameEvent(GameEvent.ENTITY_DIE);
    }

    @Override
    protected void doPush(Entity pEntity) {
    }

    @Override
    protected void pushEntities() {
    }

    @Override
    public void die(DamageSource source) {
    }

    // Mob

    @Override
    public boolean removeWhenFarAway(double pDistanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean canHoldItem(ItemStack pStack) {
        return false;
    }

    // PathfinderMob

    @Override
    public boolean checkSpawnRules(LevelAccessor pLevel, MobSpawnType pSpawnReason) {
        return true;
    }

    @Override
    public boolean isPathFinding() {
        return false;
    }

    @Override
    protected void tickLeash() {
    }

    @Override
    protected boolean shouldStayCloseToLeashHolder() {
        return false;
    }
}
