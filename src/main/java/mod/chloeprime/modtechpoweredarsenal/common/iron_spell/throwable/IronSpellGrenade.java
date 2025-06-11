package mod.chloeprime.modtechpoweredarsenal.common.iron_spell.throwable;

import com.google.common.base.Suppliers;
import com.mojang.authlib.GameProfile;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.Optional;
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

    @SuppressWarnings("deprecation")
    private static final Supplier<@Nullable Attribute> SPELL_POWER = Suppliers.memoize(
            () -> BuiltInRegistries.ATTRIBUTE.get(new ResourceLocation("irons_spellbooks", "spell_power"))
    );

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
        var container = ISpellContainer.get(stack).getActiveSpells();
        if (container.isEmpty()) {
            return false;
        }
        SpellData spellStack = container.get(0);
        AbstractSpell spell = spellStack.getSpell();
        setSpell(spell);

        double casterGenericSpellPower = Optional.ofNullable(SPELL_POWER.get())
                .map(caster::getAttributeValue)
                .orElse(1.0);
        double casterSchoolSpellPower = spell.getSchoolType().getPowerFor(caster);
        double casterSpellPower = casterGenericSpellPower * casterSchoolSpellPower;

        // 无学派手雷的buff会对所有学派的法术生效
        var grenadeSchoolId = data.getSchoolId();
        var sameSchool = grenadeSchoolId == null || Objects.equals(SchoolRegistry.getSchool(grenadeSchoolId), spell.getSchoolType());
        if (sameSchool) {
            setSpellLevel(spellStack.getLevel() + data.getSchoolAffinityBuff());
            setSpellPower(casterSpellPower * (1 + data.getSchoolPowerBuff()));
        } else {
            boolean debuff = data.willDebuffNonmatchingSchool();
            setSpellLevel(debuff ? 1 : spellStack.getLevel());
            setSpellPower(debuff ? 0.1 : casterSpellPower);
        }
        return true;
    }

    private AbstractSpell spell = SpellRegistry.none();
    private int spellLevel = 1;
    private double spellPower = 1;
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

    public double getSpellPower() {
        return spellPower;
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

    @Override
    public void onDeath() {
        var spell = getSpell();
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
                    ? caster.getEyePosition().add(0, 1, 0)
                    : caster.getEyePosition().add(this.getDeltaMovement().scale(-1));
            caster.lookAt(EntityAnchorArgument.Anchor.EYES, lookTarget);

            Optional.ofNullable(SPELL_POWER.get())
                    .map(caster::getAttribute)
                    .ifPresent(spp -> spp.setBaseValue(getSpellPower()));
            spell.castSpell(level(), getSpellLevel(), caster, CastSource.COMMAND, false);
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
    public void remove(@Nonnull RemovalReason reason) {
        super.remove(reason);
        if (!level().isClientSide() && isRemoved()) {
            MinecraftForge.EVENT_BUS.unregister(handler);
        }
    }

    public final class EventHandler {
        @SubscribeEvent
        public void onEntityJoinLevel(EntityJoinLevelEvent event) {
            if (event.getEntity().level().isClientSide()) {
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
