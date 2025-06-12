package mod.chloeprime.modtechpoweredarsenal.common.standard.entities;

import com.google.common.base.MoreObjects;
import mod.chloeprime.modtechpoweredarsenal.MTPA;
import mod.chloeprime.modtechpoweredarsenal.common.standard.internal.HateTransferable;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class VirtualCaster extends Mob implements TraceableEntity, HateTransferable {
    public VirtualCaster(Level level, @Nullable Entity owner) {
        this(MTPA.Entities.VIRTUAL_CASTER.get(), level);
        setOwner(owner);
        if (owner != null) {
            setPos(owner.position());
        }
    }

    public VirtualCaster(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
    }

    private @Nullable UUID ownerUUID;
    private @Nullable Entity cachedOwner;
    private int ticksDecayed;
    private boolean decaying;

    public void beginDecay() {
        decaying = true;
    }

    @Override
    public void tick() {
        if (level().isClientSide()) {
            return;
        }
        if (decaying) {
            ticksDecayed++;
            if (ticksDecayed > 200) {
                discard();
            }
        }
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
            event.put(MTPA.Entities.VIRTUAL_CASTER.get(), createVirtualCasterAttributes().build());
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
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    // LivingEntity

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        return false;
    }

    @Override
    public void push(Entity pEntity) {
    }

    @Override
    public void die(DamageSource source) {
    }

    // Mob

    @Override
    public boolean isNoAi() {
        return true;
    }

    @Override
    public boolean removeWhenFarAway(double pDistanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean canHoldItem(ItemStack pStack) {
        return false;
    }
}
