package mod.chloeprime.modtechpoweredarsenal.common.standard.util;

import it.unimi.dsi.fastutil.objects.Object2FloatArrayMap;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import mod.chloeprime.modtechpoweredarsenal.common.standard.internal.ClassicDamageSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;

import java.util.Collection;
import java.util.OptionalDouble;

public class DamageSourceUtil {
    public static void addTag(DamageSource source, TagKey<DamageType> tag) {
        ((ClassicDamageSource) source).mtpa$addTag(tag);
    }
    public static void addTags(DamageSource source, Collection<TagKey<DamageType>> tags) {
        ((ClassicDamageSource) source).mtpa$addTags(tags);
    }

    public static OptionalDouble getParam(DamageSource source, ResourceLocation id) {
        return ((ClassicDamageSource) source).mtpa$getParam(id);
    }

    public static void setParam(DamageSource source, ResourceLocation id, double value) {
        ((ClassicDamageSource) source).mtpa$setParam(id, value);
    }

    private static final Object2FloatMap<ResourceLocation> EMPTY_PARAMS = new Object2FloatArrayMap<>();
}
