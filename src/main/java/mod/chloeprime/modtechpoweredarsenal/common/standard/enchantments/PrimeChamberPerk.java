package mod.chloeprime.modtechpoweredarsenal.common.standard.enchantments;

import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
import com.tacz.guns.api.event.common.GunReloadEvent;
import mod.chloeprime.gunsmithlib.api.common.BulletCreateEvent;
import mod.chloeprime.modtechpoweredarsenal.MTPA;
import mod.chloeprime.modtechpoweredarsenal.ModTechPoweredArsenal;
import mod.chloeprime.modtechpoweredarsenal.mixin.tacz.KineticBulletAccessor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@CorePerk
@Mod.EventBusSubscriber
public class PrimeChamberPerk extends PerkBase {
    protected PrimeChamberPerk(Rarity pRarity, EnchantmentCategory pCategory, EquipmentSlot... pApplicableSlots) {
        super(pRarity, pCategory, pApplicableSlots);
        var bus = MinecraftForge.EVENT_BUS;
        bus.addListener(this::onReload);
    }

    public static final String TAG_KEY = ModTechPoweredArsenal.loc("prime_chamber_activated").toString();
    public static final float POWER = 0.5F;

    public static PrimeChamberPerk create() {
        return new PrimeChamberPerk(Rarity.UNCOMMON, MTPA.Enchantments.GUN_PERKS, EquipmentSlot.MAINHAND);
    }

    protected void onReload(GunReloadEvent event) {
        if (event.getLogicalSide().isClient()) {
            return;
        }
        var gun = event.getGunItemStack();
        if (gun.getEnchantmentLevel(this) <= 0) {
            return;
        }
        gun.getOrCreateTag().putBoolean(TAG_KEY, true);
    }

    @SubscribeEvent
    @SuppressWarnings("DataFlowIssue")
    public static void onBulletCreate(BulletCreateEvent event) {
        var gun = event.getGun();
        if (gun.hasTag() && gun.getTag().getBoolean(TAG_KEY)) {
            gun.getTag().remove(TAG_KEY);
            event.getBullet().getPersistentData().putBoolean(TAG_KEY, true);
            if (event.getBullet() instanceof KineticBulletAccessor accessor) {
                accessor.setPierce(accessor.getPierce() + 1);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onPreHurt(EntityHurtByGunEvent.Pre event) {
        if (event.getBullet().getPersistentData().getBoolean(TAG_KEY)) {
            event.setBaseAmount(event.getBaseAmount() * (1 + POWER));
        }
    }
}
