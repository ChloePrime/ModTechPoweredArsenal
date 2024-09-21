package mod.chloeprime.modtechpoweredarsenal.common.util;

import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.attachment.AttachmentType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class GunPreconditions {
    public static boolean hasAttachmentInstalled(ItemStack gun, AttachmentType type, @Nonnull ResourceLocation attachmentId) {
        var kun = IGun.getIGunOrNull(gun);
        if (kun == null) {
            return false;
        }
        return attachmentId.equals(kun.getAttachmentId(gun, type));
    }
}
