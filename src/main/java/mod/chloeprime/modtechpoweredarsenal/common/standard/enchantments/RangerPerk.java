package mod.chloeprime.modtechpoweredarsenal.common.standard.enchantments;

import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
import mod.chloeprime.gunsmithlib.api.common.BulletCreateEvent;
import mod.chloeprime.modtechpoweredarsenal.MTPA;
import mod.chloeprime.modtechpoweredarsenal.ModTechPoweredArsenal;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@CorePerk
@Mod.EventBusSubscriber
public class RangerPerk extends PerkBase {
    protected RangerPerk(Rarity pRarity, EnchantmentCategory pCategory, EquipmentSlot... pApplicableSlots) {
        super(pRarity, pCategory, pApplicableSlots);
        MinecraftForge.EVENT_BUS.addListener(this::onBulletCreate);
    }

    public float getDamageBoostPerMeter(int level) {
        return 0.02F / Math.max(0.001F, 6 - level);
    }

    public static RangerPerk create() {
        return new RangerPerk(Rarity.UNCOMMON, MTPA.Enchantments.GUN_PERKS, EquipmentSlot.MAINHAND);
    }

    public static final String PDK_LAUNCH_POS = ModTechPoweredArsenal.loc("ranger.launch_pos").toString();
    public static final String PDK_BOOST_PER_METER = ModTechPoweredArsenal.loc("ranger.boost").toString();

    public void onBulletCreate(BulletCreateEvent event) {
        if (event.getShooter().level().isClientSide) {
            return;
        }
        var level = event.getGun().getEnchantmentLevel(this);
        if (level <= 0) {
            return;
        }
        var pos = event.getShooter().getEyePosition();
        var boost = getDamageBoostPerMeter(level);
        var pd = event.getBullet().getPersistentData();
        var posTag = new long[]{
                Double.doubleToLongBits(pos.x()),
                Double.doubleToLongBits(pos.y()),
                Double.doubleToLongBits(pos.z()),
        };
        pd.putLongArray(PDK_LAUNCH_POS, posTag);
        pd.putFloat(PDK_BOOST_PER_METER, boost);
    }

    @SubscribeEvent
    public static void onGunshotPre(EntityHurtByGunEvent.Pre event) {
        if (event.getLogicalSide().isClient()) {
            return;
        }
        var victim = event.getHurtEntity();
        if (victim == null) {
            return;
        }
        var pd = event.getBullet().getPersistentData();
        if (!pd.contains(PDK_LAUNCH_POS, Tag.TAG_LONG_ARRAY) || !pd.contains(PDK_BOOST_PER_METER, Tag.TAG_ANY_NUMERIC)) {
            return;
        }
        var posTag = pd.getLongArray(PDK_LAUNCH_POS);
        var initialPos = new Vec3(
                Double.longBitsToDouble(posTag[0]),
                Double.longBitsToDouble(posTag[1]),
                Double.longBitsToDouble(posTag[2])
        );
        var boost = pd.getFloat(PDK_BOOST_PER_METER);
        var distance = victim.getEyePosition().subtract(initialPos).length();
        var coefficient = 1 + boost * (float) distance;
        event.setBaseAmount(event.getBaseAmount() * coefficient);
    }
}
