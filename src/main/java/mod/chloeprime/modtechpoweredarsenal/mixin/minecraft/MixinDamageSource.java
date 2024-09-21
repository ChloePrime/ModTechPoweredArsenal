package mod.chloeprime.modtechpoweredarsenal.mixin.minecraft;

import mod.chloeprime.modtechpoweredarsenal.common.standard.internal.ClassicDamageSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.LinkedHashSet;
import java.util.Set;

@Mixin(DamageSource.class)
public class MixinDamageSource implements ClassicDamageSource {
    @Override
    public void mtpa$addTag(TagKey<DamageType> tag) {
        mtpa$tags.add(tag);
    }

    @Inject(method = "is(Lnet/minecraft/tags/TagKey;)Z", at = @At("HEAD"), cancellable = true)
    private void perObjectIs(TagKey<DamageType> tag, CallbackInfoReturnable<Boolean> cir) {
        if (mtpa$tags.contains(tag)) {
            cir.setReturnValue(true);
        }
    }

    private final @Unique Set<TagKey<DamageType>> mtpa$tags = new LinkedHashSet<>();
}
