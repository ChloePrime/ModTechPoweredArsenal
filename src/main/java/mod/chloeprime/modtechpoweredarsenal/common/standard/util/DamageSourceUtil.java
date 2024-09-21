package mod.chloeprime.modtechpoweredarsenal.common.standard.util;

import mod.chloeprime.modtechpoweredarsenal.common.standard.internal.ClassicDamageSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;

public class DamageSourceUtil {
    public static void addTag(DamageSource source, TagKey<DamageType> tag) {
        ((ClassicDamageSource) source).mtpa$addTag(tag);
    }
}
