package mod.chloeprime.modtechpoweredarsenal.common.standard.enchantments;


import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.resource.pojo.data.gun.ExplosionData;
import mod.chloeprime.gunsmithlib.api.common.BulletCreateEvent;
import mod.chloeprime.modtechpoweredarsenal.MTPA;
import mod.chloeprime.modtechpoweredarsenal.mixin.tacz.KineticBulletAccessor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nonnull;
import java.util.Optional;

public class MicroMissileEnchantment extends Enchantment {
    public MicroMissileEnchantment(Rarity pRarity, EnchantmentCategory pCategory, EquipmentSlot... pApplicableSlots) {
        super(pRarity, pCategory, pApplicableSlots);
        MinecraftForge.EVENT_BUS.addListener(this::onBulletCreate);
    }

    public static MicroMissileEnchantment create() {
        return new MicroMissileEnchantment(Rarity.RARE, MTPA.Enchantments.GUN_PERKS, EquipmentSlot.MAINHAND);
    }

    @Override
    public int getMaxLevel() {
        return 2;
    }

    @Override
    public int getMaxCost(int pLevel) {
        return super.getMaxCost(pLevel) + 25;
    }

    public double getVelocityCoefficient(int level) {
        return 1 + 0.5 * level;
    }

    private void onBulletCreate(BulletCreateEvent event) {
        var level = event.getGun().getEnchantmentLevel(this);
        if (level <= 0) {
            return;
        }
        var bullet = event.getBullet();
        bullet.setNoGravity(true);
        if (bullet instanceof KineticBulletAccessor accessor) {
            accessor.setGravity(0);
        }
        bullet.setDeltaMovement(bullet.getDeltaMovement().scale(getVelocityCoefficient(level)));
    }

    @Override
    public boolean canEnchant(@Nonnull ItemStack stack) {
        return super.canEnchant(stack) && isGrenadeLauncher(stack);
    }

    public static boolean isGrenadeLauncher(ItemStack stack) {
        var kun = IGun.getIGunOrNull(stack);
        if (kun == null) {
            return false;
        }
        return TimelessAPI.getCommonGunIndex(kun.getGunId(stack))
                .filter(MicroMissileEnchantment::isGrenadeLauncher)
                .isPresent();
    }

    public static boolean isGrenadeLauncher(CommonGunIndex index) {
        return index.getBulletData().getGravity() > 0 && Optional.ofNullable(index.getBulletData().getExplosionData())
                .filter(ExplosionData::isExplode)
                .isPresent();
    }
}
