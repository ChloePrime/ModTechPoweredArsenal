package mod.chloeprime.modtechpoweredarsenal.common.iron_spell.throwable;

import com.google.common.base.Suppliers;
import com.mojang.authlib.GameProfile;
import io.redspace.ironsspellbooks.api.events.SpellPreCastEvent;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.magic.SpellSelectionManager;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.capabilities.magic.SyncedSpellData;
import io.redspace.ironsspellbooks.capabilities.magic.TargetEntityCastData;
import me.xjqsh.lrtactical.entity.ThrowableItemEntity;
import me.xjqsh.lrtactical.item.throwable.ThrowableType;
import me.xjqsh.lrtactical.resource.CommonAssetsManager;
import mod.chloeprime.modtechpoweredarsenal.common.standard.internal.HateTransferable;
import mod.chloeprime.modtechpoweredarsenal.common.standard.util.RegistryHelper;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nonnull;
import java.lang.ref.WeakReference;
import java.util.LinkedHashMap;
import java.util.Map;
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
    private static final Supplier<Attribute> SPELL_POWER = RegistryHelper.holder(BuiltInRegistries.ATTRIBUTE, "irons_spellbooks", "spell_power");

    static final Map<UUID, ServerPlayer> CASTERS = new LinkedHashMap<>();

    @SuppressWarnings("deprecation")
    private static final Supplier<Attribute> MAX_MANA = RegistryHelper.holder(BuiltInRegistries.ATTRIBUTE, "irons_spellbooks", "max_mana");

    @SuppressWarnings("deprecation")
    private static final Supplier<Attribute> FAST_CAST = RegistryHelper.holder(BuiltInRegistries.ATTRIBUTE, "irons_spellbooks", "cast_time_reduction");

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
        data.adjustSpellPower(
                spell, spellStack.getLevel(), casterSpellPower,
                this::setSpellLevel, this::setSpellPower
        );
        return true;
    }

    private AbstractSpell spell = SpellRegistry.none();
    private int spellLevel = 1;
    private double spellPower = 1;
    private ItemStack grenadeItem;
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

    private void setGrenadeItem(ItemStack stack) {
        this.grenadeItem = stack.copy();
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

            prepareCasting(caster, spell);
            spell.attemptInitiateCast(grenadeItem, getSpellLevel(), level(), caster, CastSource.COMMAND, false, SpellSelectionManager.MAINHAND);
        }

        super.onDeath();
    }

    private void prepareCasting(LivingEntity caster, @Nonnull AbstractSpell spell) {
        // 位置和朝向
        caster.setPos(this.position());
        var lookTarget = shouldBounce()
                ? caster.getEyePosition().add(0, -1, 0)
                : caster.getEyePosition().add(this.getDeltaMovement().scale(-1));
        caster.lookAt(EntityAnchorArgument.Anchor.EYES, lookTarget);

        // 设置魔法强度
        Optional.ofNullable(SPELL_POWER.get())
                .map(caster::getAttribute)
                .ifPresent(spp -> spp.setBaseValue(getSpellPower()));

        // 学习将要释放的法术
        if (spell.needsLearning()) {
            MagicData.getPlayerMagicData(caster).getSyncedData().learnSpell(spell);
        }

        // 回满魔力
        var magicData = MagicData.getPlayerMagicData(caster);
        if (magicData != null) {
            magicData.setMana(Float.MAX_VALUE);
        }
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
            if (event.getEntity().level().isClientSide()) {
                return;
            }
            if (event.getEntity() == IronSpellGrenade.this || event.getEntity() instanceof FakePlayer) {
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

        @SubscribeEvent
        public void onPreCast(SpellPreCastEvent event) {
            if (event.getEntity().level().isClientSide()) {
                return;
            }
            var caster = IronSpellGrenade.this.caster.get().get();
            if (caster == null || event.getEntity() != caster) {
                return;
            }
            var magicData = MagicData.getPlayerMagicData(caster);
            if (magicData.getAdditionalCastData() == null) {
                magicData.setAdditionalCastData(new TargetEntityCastData(caster));
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
        return new WeakReference<>(setupPlayer(FakePlayerFactory.get(serverLevel, new GameProfile(id, name))));
    }

    private static <P extends ServerPlayer> P setupPlayer(P player) {
        setAttribute(player, MAX_MANA, 1000000);
        setAttribute(player, FAST_CAST, 100);
        ((ServerLevel) player.level()).addNewPlayer(player);
        return player;
    }

    private static void setAttribute(LivingEntity holder, Supplier<Attribute> attribute, double value) {
        var instance = holder.getAttribute(attribute.get());
        if (instance != null) {
            instance.setBaseValue(value);
        }
    }
}
