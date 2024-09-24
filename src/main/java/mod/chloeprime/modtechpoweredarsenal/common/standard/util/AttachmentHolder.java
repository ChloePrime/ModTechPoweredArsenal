package mod.chloeprime.modtechpoweredarsenal.common.standard.util;

import com.tacz.guns.api.item.attachment.AttachmentType;
import net.minecraft.resources.ResourceLocation;

public record AttachmentHolder(
        ResourceLocation id,
        AttachmentType type
) {
}
