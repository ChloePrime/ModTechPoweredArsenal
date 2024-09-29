package mod.chloeprime.modtechpoweredarsenal.common.standard.util;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.api.item.gun.AbstractGunItem;
import com.tacz.guns.resource.pojo.data.gun.Bolt;
import com.tacz.guns.resource.pojo.data.gun.FeedType;
import com.tacz.guns.util.AttachmentDataUtils;
import mod.chloeprime.gunsmithlib.api.util.GunInfo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;

@Mod.EventBusSubscriber
public class GunHelper {
    public static boolean hasAttachmentInstalled(ItemStack gun, AttachmentType type, @Nonnull ResourceLocation attachmentId) {
        var kun = IGun.getIGunOrNull(gun);
        if (kun == null) {
            return false;
        }
        return attachmentId.equals(kun.getAttachmentId(gun, type));
    }

    public static int getTotalAmmo(GunInfo gun) {
        return gun.getTotalAmmo();
    }

    public static int getTotalMagSize(GunInfo gun) {
        return gun.getTotalMagazineSize();
    }

    /**
     * 增加子弹数：<p>
     * 这个方法会先上膛，然后再添加至弹匣，
     */
    public static int addBullet(GunInfo gun, int count) {
        var isOpenBolt = gun.index().getGunData().getBolt() == Bolt.OPEN_BOLT;
        var doNotConsiderBolt = isOpenBolt || gun.gunItem().hasBulletInBarrel(gun.gunStack());

        var magMax = AttachmentDataUtils.getAmmoCountWithAttachment(gun.gunStack(), gun.index().getGunData());
        var magCur = gun.gunItem().getCurrentAmmoCount(gun.gunStack());
        if (doNotConsiderBolt) {
            var newBulletCount = Math.min(magMax, magCur + count);
            gun.gunItem().setCurrentAmmoCount(gun.gunStack(), newBulletCount);
            return newBulletCount - magCur;
        } else {
            var newBulletCount = Math.min(magMax, magCur + count - 1);
            gun.gunItem().setBulletInBarrel(gun.gunStack(), true);
            gun.gunItem().setCurrentAmmoCount(gun.gunStack(), newBulletCount);
            return newBulletCount - magCur + 1;
        }
    }


    public static int magicReload(LivingEntity shooter, ItemStack gun, int reloadCount) {
        return magicReload(shooter, gun, reloadCount, false);
    }

    public static int magicReload(LivingEntity shooter, ItemStack gun, int count, boolean allowOverflow) {
        var kun = IGun.getIGunOrNull(gun);
        if (kun == null) {
            return 0;
        }
        var index0 = TimelessAPI.getCommonGunIndex(kun.getGunId(gun));
        if (index0.isEmpty()) {
            return 0;
        }
        var index = index0.get();
        // 对燃料罐模式不生效
        if (index.getGunData().getReloadData().getType() != FeedType.MAGAZINE) {
            return 0;
        }
        var op = IGunOperator.fromLivingEntity(shooter);
        // 主手武器在切枪和装弹时不触发
        if (gun == shooter.getMainHandItem()) {
            if (op.getSynReloadState().getCountDown() > 0 || op.getSynDrawCoolDown() != 0) {
                return 0;
            }
            if (op.getSynBoltCoolDown() > 0) {
                return 0;
            }
        }

        var reloadCount = allowOverflow ? count : Math.min(
                count,
                AttachmentDataUtils.getAmmoCountWithAttachment(gun, index.getGunData()) - kun.getCurrentAmmoCount(gun)
        );
        var ammoExtracted = extractAmmo(shooter, gun, reloadCount);
        var needToRefill = ammoExtracted;
        if (needToRefill <= 0) {
            return 0;
        }
        if (index.getGunData().getBolt() == Bolt.CLOSED_BOLT && !kun.hasBulletInBarrel(gun)) {
            kun.setBulletInBarrel(gun, true);
            needToRefill--;
        }
        kun.setCurrentAmmoCount(gun, kun.getCurrentAmmoCount(gun) + needToRefill);
        return ammoExtracted;
    }

    private static int extractAmmo(LivingEntity shooter, ItemStack gun, int reloadCount) {
        if (!IGunOperator.fromLivingEntity(shooter).needCheckAmmo()) {
            return reloadCount;
        }
        var loadCount = 0;
        if (gun.getItem() instanceof AbstractGunItem agi) {
            if (agi.useDummyAmmo(gun)) {
                return agi.findAndExtractDummyAmmo(gun, reloadCount);
            }
            if (reloadCount > 0) {
                return shooter.getCapability(ForgeCapabilities.ITEM_HANDLER, null)
                        .map(inventory -> agi.findAndExtractInventoryAmmos(inventory, gun, reloadCount))
                        .orElse(0);
            }
        }
        return loadCount;
    }
}
