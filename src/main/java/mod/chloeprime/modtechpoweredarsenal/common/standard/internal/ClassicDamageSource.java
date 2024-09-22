package mod.chloeprime.modtechpoweredarsenal.common.standard.internal;

import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraftforge.common.util.LazyOptional;

import java.util.Collection;
import java.util.OptionalDouble;

public interface ClassicDamageSource {
    void mtpa$addTag(TagKey<DamageType> tag);
    void mtpa$addTags(Collection<TagKey<DamageType>> tags);
    OptionalDouble mtpa$getParam(ResourceLocation id);
    void mtpa$setParam(ResourceLocation id, double value);
}
