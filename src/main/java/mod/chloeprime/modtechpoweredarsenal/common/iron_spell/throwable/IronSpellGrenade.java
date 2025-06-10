package mod.chloeprime.modtechpoweredarsenal.common.iron_spell.throwable;

import com.google.common.base.Suppliers;
import com.mojang.authlib.GameProfile;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import me.xjqsh.lrtactical.entity.ThrowableItemEntity;
import me.xjqsh.lrtactical.item.throwable.ThrowableType;
import me.xjqsh.lrtactical.resource.CommonAssetsManager;
import mod.chloeprime.modtechpoweredarsenal.common.standard.internal.HateTransferable;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.UUID;
import java.util.function.Supplier;

public class IronSpellGrenade extends ThrowableItemEntity {
    public static EntityType<IronSpellGrenade> TYPE = EntityType.Builder.<IronSpellGrenade>of(IronSpellGrenade::new, MobCategory.MISC)
            .setShouldReceiveVelocityUpdates(true)
            .setTrackingRange(64)
            .setUpdateInterval(1)
            .sized(0.3F, 0.3F)
            .noSave().noSummon().fireImmune()
            .build("iron_spell_grenade");

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

        if (!entity.loadSpellOverrideFromNBT(stack)) {
            entity.setSpell(data.getSpellId());
            entity.setSpellLevel(data.getSpellLevel());
        }
        return entity;
    }

    private boolean loadSpellOverrideFromNBT(ItemStack stack) {
        if (!ISpellContainer.isSpellContainer(stack)) {
            return false;
        }
        var container = ISpellContainer.get(stack).getActiveSpells();
        if (container.isEmpty()) {
            return false;
        }
        SpellData spell = container.get(0);
        setSpell(spell.getSpell());
        setSpellLevel(spell.getLevel());
        return true;
    }

    private AbstractSpell spell = SpellRegistry.none();
    private int spellLevel = 1;
    private final Supplier<WeakReference<FakePlayer>> caster = Suppliers.memoize(() -> createCaster(level()));
    private final EventHandler handler = new EventHandler();

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

    public void setSpell(AbstractSpell spell) {
        this.spell = spell;
    }

    public void setSpell(ResourceLocation spellId) {
        setSpell(SpellRegistry.getSpell(spellId));
    }

    public void setSpellLevel(int spellLevel) {
        this.spellLevel = spellLevel;
    }

    @Override
    public void onDeath() {
        if (spell != null && !level().isClientSide) {
            FakePlayer caster = this.caster.get().get();
            if (caster == null) {
                return;
            }

            if (caster instanceof HateTransferable hateTransferable) {
                Entity grenadeOwner = getOwner();
                if (grenadeOwner != null) {
                    hateTransferable.mtpa$setHateOwner(grenadeOwner);
                }
            }
            caster.setPos(this.position());
            var lookTarget = shouldBounce()
                    ? caster.getEyePosition().add(0, 1,0)
                    : caster.getEyePosition().add(this.getDeltaMovement().scale(-1));
            caster.lookAt(EntityAnchorArgument.Anchor.EYES, lookTarget);

            spell.castSpell(level(), spellLevel, caster, CastSource.COMMAND, false);
        }

        super.onDeath();
    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        if (!level().isClientSide()) {
            MinecraftForge.EVENT_BUS.register(handler);
        }
    }

    @Override
    public void remove(@NotNull RemovalReason reason) {
        super.remove(reason);
        if (!level().isClientSide() && isRemoved()) {
            MinecraftForge.EVENT_BUS.unregister(handler);
        }
    }

    public final class EventHandler {
        @SubscribeEvent
        public void onEntityJoinLevel(EntityJoinLevelEvent event) {
            if (level().isClientSide() || event.getEntity().level().isClientSide()) {
                return;
            }
            if (event.getEntity() == IronSpellGrenade.this) {
                return;
            }
            var caster = IronSpellGrenade.this.caster.get().get();
            if (caster == null) {
                return;
            }
            if (event.getEntity() instanceof Projectile projectile && projectile.getOwner() == caster) {
                double yCenterOffset = (getBbHeight() - projectile.getBbHeight()) / 2;
                projectile.setPos(caster.position().add(0, yCenterOffset, 0));
                var grenadeOwner = getOwner();
                if (grenadeOwner != null) {
                    projectile.setOwner(grenadeOwner);
                }
            }
        }
    }

    private static final WeakReference<FakePlayer> NULL_CASTER = new WeakReference<>(null);
    private static WeakReference<FakePlayer> createCaster(Level level) {
        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) {
            return NULL_CASTER;
        }
        var id = UUID.randomUUID();
        var name = "§§ Grenade Man %s §§".formatted(id.getMostSignificantBits() ^ id.getLeastSignificantBits());
        return new WeakReference<>(FakePlayerFactory.get(serverLevel, new GameProfile(id, name)));
    }
}
