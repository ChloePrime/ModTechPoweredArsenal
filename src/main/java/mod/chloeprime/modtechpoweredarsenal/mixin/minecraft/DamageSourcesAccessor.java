package mod.chloeprime.modtechpoweredarsenal.mixin.minecraft;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import javax.annotation.Nullable;

@Mixin(DamageSources.class)
public interface DamageSourcesAccessor {
    @Invoker
    DamageSource invokeSource(ResourceKey<DamageType> id, @Nullable Entity var1, @Nullable Entity var2);
}
