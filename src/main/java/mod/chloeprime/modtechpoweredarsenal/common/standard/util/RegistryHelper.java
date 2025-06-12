package mod.chloeprime.modtechpoweredarsenal.common.standard.util;

import com.google.common.base.Suppliers;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Supplier;

public class RegistryHelper {
    public static <T> boolean is(Level level, ResourceKey<Registry<T>> registry, @Nullable T object, TagKey<T> tag) {
        Objects.requireNonNull(level);
        Objects.requireNonNull(registry);
        Objects.requireNonNull(tag);

        if (object == null) {
            return false;
        }
        return level.registryAccess()
                .registry(registry)
                .flatMap(reg -> reg.getResourceKey(object).flatMap(reg::getHolder))
                .filter(holder -> holder.is(tag))
                .isPresent();
    }

    public static <T> Supplier<T> holder(Registry<T> registry, String namespace, String path) {
        return holder(registry, new ResourceLocation(namespace, path));
    }

    public static <T> Supplier<T> holder(Registry<T> registry, ResourceLocation key) {
        return Suppliers.memoize(() -> registry.get(key));
    }
}
