package mod.chloeprime.modtechpoweredarsenal.common.standard.enchantments;

import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
import com.tacz.guns.api.event.common.EntityKillByGunEvent;
import com.tacz.guns.resource.index.CommonGunIndex;
import mod.chloeprime.gunsmithlib.api.util.GunInfo;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.modtechpoweredarsenal.MTPA;
import mod.chloeprime.modtechpoweredarsenal.ModTechPoweredArsenal;
import mod.chloeprime.modtechpoweredarsenal.common.standard.util.StreamSupportMC;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

@CorePerk
@Mod.EventBusSubscriber
public class RecombinationPerk extends PerkBase {
    protected RecombinationPerk(Rarity pRarity, EnchantmentCategory pCategory, EquipmentSlot... pApplicableSlots) {
        super(pRarity, pCategory, pApplicableSlots);
        MinecraftForge.EVENT_BUS.addListener(this::onKill);
    }

    public static int getMaxBuffAmplifier(int enchantmentLevel) {
        return 9 + 2 * (enchantmentLevel - 1);
    }

    public static boolean isAllowedOn(ItemStack weapon) {
        return Gunsmith.getGunInfo(weapon)
                .map(GunInfo::index)
                .map(CommonGunIndex::getType)
                .filter(ALLOWED_TYPES::contains)
                .isPresent();
    }

    public static RecombinationPerk create() {
        return new RecombinationPerk(Rarity.UNCOMMON, MTPA.Enchantments.GUN_PERKS, EquipmentSlot.MAINHAND);
    }

    public static final Set<String> ALLOWED_TYPES = Set.of(
            "sniper", "shotgun", "rpg"
    );

    public static final int BUFF_DURATION = 20 * 30; // 30s
    public static final String PDK_IS_SHOOTING = ModTechPoweredArsenal.loc("recombination.is_shooting").toString();
    public static final TagKey<DamageType> ANY_MAGIC = TagKey.create(Registries.DAMAGE_TYPE, ModTechPoweredArsenal.loc("any_magic"));

    @Override
    public boolean canApplyAtEnchantingTable(@Nonnull ItemStack stack) {
        return super.canApplyAtEnchantingTable(stack) && isAllowedOn(stack);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onGunshotPre(EntityHurtByGunEvent.Pre event) {
        if (event.getLogicalSide().isClient()) {
            return;
        }
        Optional.ofNullable(event.getAttacker()).ifPresent(shooter -> shooter.getPersistentData().putBoolean(PDK_IS_SHOOTING, true));
    }

    private void onKill(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof LivingEntity shooter)) {
            return;
        }
        var invalid = shooter.getPersistentData().getBoolean(PDK_IS_SHOOTING);
        if (invalid) {
            return;
        }
        int maxBuffAmp = shooter.getCapability(ForgeCapabilities.ITEM_HANDLER, null)
                .map(StreamSupportMC::of)
                .map(items -> items
                        .map(Supplier::get)
                        .mapToInt(stack -> stack.getEnchantmentLevel(this))
                        .filter(lvl -> lvl > 0)
                        .map(RecombinationPerk::getMaxBuffAmplifier)
                        .max()
                        .orElse(-1))
                .orElse(-1);
        if (maxBuffAmp < 0) {
            return;
        }
        var buff = MTPA.MobEffects.RECOMBINATION_BUFF.get();
        var oldBuffAmp = Optional.ofNullable(shooter.getEffect(buff))
                .map(MobEffectInstance::getAmplifier)
                .orElse(-1);
        var newBuffAmp = Mth.clamp(oldBuffAmp + 1, 0, maxBuffAmp);
        shooter.addEffect(new MobEffectInstance(buff, BUFF_DURATION, newBuffAmp, true, true));
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onGunshotPost(EntityHurtByGunEvent.Post event) {
        if (event.getLogicalSide().isClient()) {
            return;
        }
        removeGunshotMark(event.getAttacker());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onGunshotKill(EntityKillByGunEvent event) {
        if (event.getLogicalSide().isClient()) {
            return;
        }
        removeGunshotMark(event.getAttacker());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity().level().isClientSide) {
            return;
        }
        removeGunshotMark(event.getEntity());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity().level().isClientSide) {
            return;
        }
        removeGunshotMark(event.getEntity());
    }

    private static void removeGunshotMark(@Nullable Entity entity) {
        Optional.ofNullable(entity).ifPresent(shooter -> shooter.getPersistentData().remove(PDK_IS_SHOOTING));
    }
}
