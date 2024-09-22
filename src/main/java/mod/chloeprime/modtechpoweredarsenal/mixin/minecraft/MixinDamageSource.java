package mod.chloeprime.modtechpoweredarsenal.mixin.minecraft;

import it.unimi.dsi.fastutil.objects.Object2DoubleLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2FloatLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import mod.chloeprime.modtechpoweredarsenal.common.standard.internal.ClassicDamageSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.common.util.LazyOptional;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.OptionalDouble;
import java.util.Set;

@Mixin(DamageSource.class)
public class MixinDamageSource implements ClassicDamageSource {
    @Override
    public void mtpa$addTag(TagKey<DamageType> tag) {
        mtpa$tags.get().add(tag);
        mtpa$hasTags = true;
    }

    @Override
    public void mtpa$addTags(Collection<TagKey<DamageType>> tags) {
        mtpa$tags.get().addAll(tags);
        mtpa$hasTags = true;
    }

    @Override
    public OptionalDouble mtpa$getParam(ResourceLocation id) {
        if (!mtpa$hasParams) {
            return OptionalDouble.empty();
        }
        var params = mtpa$params.get();
        return params.containsKey(id)
                ? OptionalDouble.of(params.getDouble(id))
                : OptionalDouble.empty();
    }

    @Override
    public void mtpa$setParam(ResourceLocation id, double value) {
        mtpa$hasParams = true;
        mtpa$params.get().put(id, value);
    }

    @Inject(method = "is(Lnet/minecraft/tags/TagKey;)Z", at = @At("HEAD"), cancellable = true)
    private void perObjectIs(TagKey<DamageType> tag, CallbackInfoReturnable<Boolean> cir) {
        if (!mtpa$hasTags) {
            return;
        }
        if (mtpa$tags.get().contains(tag)) {
            cir.setReturnValue(true);
        }
    }

    private @Unique boolean mtpa$hasTags = false;
    private @Unique boolean mtpa$hasParams = false;
    private final @Unique Lazy<Set<TagKey<DamageType>>> mtpa$tags = Lazy.of(LinkedHashSet::new);
    private final @Unique Lazy<Object2DoubleMap<ResourceLocation>> mtpa$params = Lazy.of(Object2DoubleLinkedOpenHashMap::new);
}
