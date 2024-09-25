package mod.chloeprime.modtechpoweredarsenal.common.standard.enchantments;

import com.tacz.guns.api.event.common.EntityKillByGunEvent;
import com.tacz.guns.util.AttachmentDataUtils;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.modtechpoweredarsenal.MTPA;
import mod.chloeprime.modtechpoweredarsenal.common.standard.util.GunHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.common.MinecraftForge;

/**
 * 维持生计
 */
@CorePerk
public class SubsistencePerk extends PerkBase {
    protected SubsistencePerk(Rarity pRarity, EnchantmentCategory pCategory, EquipmentSlot... pApplicableSlots) {
        super(pRarity, pCategory, pApplicableSlots);
        MinecraftForge.EVENT_BUS.addListener(this::onKill);
    }

    public static SubsistencePerk create() {
        return new SubsistencePerk(Rarity.COMMON, MTPA.Enchantments.GUN_PERKS, EquipmentSlot.MAINHAND);
    }

    public float getFillRate(int level) {
        return (float) (0.1 + 0.07 * (level - 1));
    }

    private void onKill(EntityKillByGunEvent event) {
        var attacker = event.getAttacker();
        if (attacker == null || attacker.level().isClientSide) {
            return;
        }
        var gunStack = attacker.getMainHandItem();
        var level = gunStack.getEnchantmentLevel(this);
        if (level <= 0) {
            return;
        }
        var gun = Gunsmith.getGunInfo(gunStack).orElse(null);
        if (gun == null) {
            return;
        }
        var fullAmmo = AttachmentDataUtils.getAmmoCountWithAttachment(gun.gunStack(), gun.index().getGunData());
        var reloadCount = (int) Math.ceil(fullAmmo * getFillRate(level));
        GunHelper.magicReload(attacker, gun.gunStack(), reloadCount);
    }
}
