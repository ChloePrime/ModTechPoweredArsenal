package mod.chloeprime.modtechpoweredarsenal.common.standard.attachments;

import com.google.common.collect.ImmutableList;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.event.common.AttachmentPropertyEvent;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.api.item.gun.FireMode;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.resource.pojo.data.gun.Bolt;
import mod.chloeprime.modtechpoweredarsenal.ModTechPoweredArsenal;
import mod.chloeprime.modtechpoweredarsenal.common.standard.util.GunPreconditions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber
public class BumpfireStockBehavior {
    public static final ResourceLocation BUMPFIRE_STOCK_ID = ModTechPoweredArsenal.loc("stock_bumpfire");
    public static final List<FireMode> FULLAUTO_ONLY = ImmutableList.of(FireMode.AUTO);

    public static List<FireMode> injectFireMode(
            ItemStack gunStack,
            CommonGunIndex gunIndex,
            List<FireMode> original
    ) {
        if (gunIndex.getGunData().getBolt() == Bolt.MANUAL_ACTION) {
            return original;
        }
        // 判断是否安装了撞火枪托
        if (!GunPreconditions.hasAttachmentInstalled(gunStack, AttachmentType.STOCK, BUMPFIRE_STOCK_ID)) {
            return original;
        }
        if (original.contains(FireMode.AUTO)) {
            return original;
        }
        // 加入全自动射击模式
//        var newModes = new ArrayList<FireMode>(original.size() + 1);
//        newModes.addAll(original);
//        newModes.add(FireMode.AUTO);
//        return newModes;
        return FULLAUTO_ONLY;
    }

    /**
     * 修复切换到全自动后拆下撞火枪托能保留全自动的bug
     */
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onModifyComplete(AttachmentPropertyEvent event) {
        var gun = event.getGunItem();
        var kun = IGun.getIGunOrNull(gun);
        if (kun == null) {
            return;
        }
        TimelessAPI.getCommonGunIndex(kun.getGunId(gun)).ifPresent(index -> {
            if (GunPreconditions.hasAttachmentInstalled(gun, AttachmentType.STOCK, BUMPFIRE_STOCK_ID)) {
                kun.setFireMode(gun, FireMode.AUTO);
            } else {
                var gunpackSupported = index.getGunData().getFireModeSet();
                if (kun.getFireMode(gun) == FireMode.AUTO && !gunpackSupported.contains(FireMode.AUTO)) {
                    kun.setFireMode(gun, gunpackSupported.get(0));
                }
            }
        });
    }
}
