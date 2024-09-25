package mod.chloeprime.modtechpoweredarsenal.common.standard.enchantments;

import com.tacz.guns.api.event.common.GunShootEvent;
import com.tacz.guns.util.AttachmentDataUtils;
import mod.chloeprime.gunsmithlib.api.util.GunInfo;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.modtechpoweredarsenal.MTPA;
import mod.chloeprime.modtechpoweredarsenal.ModTechPoweredArsenal;
import mod.chloeprime.modtechpoweredarsenal.common.standard.util.GunHelper;
import mod.chloeprime.modtechpoweredarsenal.common.standard.util.StreamSupportMC;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.ApiStatus;

import java.util.Objects;

/**
 * 重建 <p>
 * 缓慢装填弹匣至两倍弹匣容量
 */
@CorePerk
public class ReconstructionPerk extends PerkBase {
    protected ReconstructionPerk(Rarity pRarity, EnchantmentCategory pCategory, EquipmentSlot... pApplicableSlots) {
        super(pRarity, pCategory, pApplicableSlots);
        var bus = MinecraftForge.EVENT_BUS;
        bus.addListener(this::onPlayerShoot);
        bus.addListener(this::onPlayerTick);
    }

    public int getDelayAfterShoot(int level) {
        return Math.max(0, 100 - 20 * level);
    }

    public static final int FILL_DELAY = 20;
    public static final double FILL_RATE = 0.1;
    public static final String TAG_KEY_LAST_SHOOT = ModTechPoweredArsenal.loc("reconstruction_last_shoot").toString();
    public static final String TAG_KEY_LAST_FILL = ModTechPoweredArsenal.loc("reconstruction_last_fill").toString();

    @ApiStatus.Internal
    public static ReconstructionPerk create() {
        return new ReconstructionPerk(Rarity.RARE, MTPA.Enchantments.GUN_PERKS, EquipmentSlot.MAINHAND);
    }

    private void onPlayerShoot(GunShootEvent event) {
        if (event.getLogicalSide().isClient()) {
            return;
        }
        var now = event.getShooter().level().getGameTime();
        event.getGunItemStack().getOrCreateTag().putLong(TAG_KEY_LAST_SHOOT, now);
    }

    private void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.player.level().isClientSide) {
            return;
        }
        var updateInterval = 5;
        var now = event.player.level().getGameTime();
        var salt = event.player.hashCode() % updateInterval;
        if ((now + salt) % updateInterval != 0) {
            return;
        }
        event.player.getCapability(ForgeCapabilities.ITEM_HANDLER, null)
                .resolve()
                .map(inv -> inv instanceof IItemHandlerModifiable mutable ? mutable : null)
                .ifPresent(inventory -> onPlayerTick0(event.player, inventory, now));
    }

    private void onPlayerTick0(LivingEntity shooter, IItemHandlerModifiable inventory, long now) {
        StreamSupportMC.of(inventory).forEach(item -> {
            if (!item.get().hasTag()) {
                return;
            }
            int level = item.get().getEnchantmentLevel(this);
            if (level <= 0) {
                return;
            }
            var gunInfo = Gunsmith.getGunInfo(item.get()).orElse(null);
            if (gunInfo == null) {
                return;
            }
            var delayAfterShoot = getDelayAfterShoot(level);
            var tag = Objects.requireNonNull(item.get().getTag());
            var lastShoot = tag.getLong(TAG_KEY_LAST_SHOOT);
            var lastReload = tag.getLong(TAG_KEY_LAST_FILL);
            if (now - lastShoot <= delayAfterShoot || now - lastReload <= FILL_DELAY) {
                return;
            }
            tag.putLong(TAG_KEY_LAST_FILL, now);
            if (reloadGun(shooter, gunInfo) > 0) {
                item.update();
            }
        });
    }

    private static int reloadGun(LivingEntity shooter, GunInfo gun) {
        var magazineSize = AttachmentDataUtils.getAmmoCountWithAttachment(gun.gunStack(), gun.index().getGunData());
        var maxRefill = 2 * magazineSize;
        var currentAmmo = gun.gunItem().getCurrentAmmoCount(gun.gunStack());
        var fillCount = Math.min((int) Math.ceil(magazineSize * FILL_RATE), maxRefill - currentAmmo);
        return GunHelper.magicReload(shooter, gun.gunStack(), fillCount, true);
    }
}
