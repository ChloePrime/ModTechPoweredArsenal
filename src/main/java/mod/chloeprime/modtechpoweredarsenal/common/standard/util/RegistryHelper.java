package mod.chloeprime.modtechpoweredarsenal.common.standard.util;

import com.google.common.base.Suppliers;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;

import java.util.function.Supplier;

public class RegistryHelper {
    public static <T> Supplier<T> holder(Registry<T> registry, String namespace, String path) {
        return holder(registry, new ResourceLocation(namespace, path));
    }

    public static <T> Supplier<T> holder(Registry<T> registry, ResourceLocation key) {
        return Suppliers.memoize(() -> registry.get(key));
    }
}
