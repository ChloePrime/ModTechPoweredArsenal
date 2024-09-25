package mod.chloeprime.modtechpoweredarsenal.common.standard.util;

import org.jetbrains.annotations.ApiStatus;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface Property<T> extends Supplier<T>, Consumer<T> {
    T get();
    void set(T value);

    default void update() {
        set(get());
    }

    @Override
    @ApiStatus.NonExtendable
    default void accept(T t) {
        set(t);
    }
}
