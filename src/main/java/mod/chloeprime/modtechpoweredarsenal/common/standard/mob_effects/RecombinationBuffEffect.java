package mod.chloeprime.modtechpoweredarsenal.common.standard.mob_effects;

import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
import mod.chloeprime.gunsmithlib.api.common.BulletCreateEvent;
import mod.chloeprime.modtechpoweredarsenal.MTPA;
import mod.chloeprime.modtechpoweredarsenal.ModTechPoweredArsenal;
import mod.chloeprime.modtechpoweredarsenal.common.standard.enchantments.RecombinationPerk;
import mod.chloeprime.modtechpoweredarsenal.common.standard.enchantments.ReconstructionPerk;
import net.minecraft.nbt.Tag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;

import java.util.Collections;
import java.util.List;

public class RecombinationBuffEffect extends MobEffect {
    public static final String PDK_RECOMBINATION_COEFFICIENT = ModTechPoweredArsenal.loc("recombination_coefficient").toString();
    public static final String PDK_NEEDS_REMOVE_THIS = ModTechPoweredArsenal.loc("recombination_needs_to_be_removed").toString();

    public RecombinationBuffEffect(int pColor) {
        super(MobEffectCategory.BENEFICIAL, pColor);
        MinecraftForge.EVENT_BUS.addListener(this::onGunFire);
        MinecraftForge.EVENT_BUS.addListener(this::onGunshotPre);
        MinecraftForge.EVENT_BUS.addListener(this::onPlayerTickEnd);
    }

    public float getDamageCoefficient(int amplifier) {
        return 1 + (amplifier + 1) * 0.1F;
    }

    @Override
    public boolean isDurationEffectTick(int pDuration, int pAmplifier) {
        return false;
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        return Collections.emptyList();
    }

    protected void onGunFire(BulletCreateEvent event) {
        if (event.getShooter().level().isClientSide) {
            return;
        }
        var instance = event.getShooter().getEffect(this);
        if (instance == null) {
            return;
        }
        var perk = MTPA.Enchantments.RECOMBINATION.get();
        int enchLevel = event.getGun().getEnchantmentLevel(perk);
        if (enchLevel <= 0) {
            return;
        }
        // 防止重组II的12级buff作用在重组I的武器上
        var maxAmplifier = RecombinationPerk.getMaxBuffAmplifier(enchLevel);
        var amplifier = Math.min(maxAmplifier, instance.getAmplifier());

        event.getBullet().getPersistentData().putFloat(PDK_RECOMBINATION_COEFFICIENT, getDamageCoefficient(amplifier));
        if (event.getShooter() instanceof Player player) {
            player.getPersistentData().putBoolean(PDK_NEEDS_REMOVE_THIS, true);
        }
    }

    protected void onGunshotPre(EntityHurtByGunEvent.Pre event) {
        var pd = event.getBullet().getPersistentData();
        if (!pd.contains(PDK_RECOMBINATION_COEFFICIENT, Tag.TAG_ANY_NUMERIC)) {
            return;
        }
        var coefficient = pd.getFloat(PDK_RECOMBINATION_COEFFICIENT);
        event.setBaseAmount(event.getBaseAmount() * coefficient);
    }

    protected void onPlayerTickEnd(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START || event.player.level().isClientSide) {
            return;
        }
        if (event.player.getPersistentData().getBoolean(PDK_NEEDS_REMOVE_THIS)) {
            event.player.getPersistentData().remove(PDK_NEEDS_REMOVE_THIS);
            event.player.removeEffect(this);
        }
    }
}
