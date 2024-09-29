package mod.chloeprime.modtechpoweredarsenal.common.standard.guns;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mod.chloeprime.gunsmithlib.api.util.GunInfo;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

/**
 * @param shotsBeforeOverheat Maximum shots from fully cool to fully overheat
 * @param partialHeatDelay Delay from fire stopped to cooling start, when not fully overheat
 * @param fullHeatDelay Delay from fire stopped to cooling start, when fully overheat
 * @param coolDelay Delay between cooling calculates, in ticks
 * @param coolCount Cool amount per cooling calculate, in shots
 */
public record OverheatData(
        int shotsBeforeOverheat,
        int partialHeatDelay,
        int fullHeatDelay,
        int coolDelay,
        int coolCount
) {
    public record Runtime(
            OverheatData overheat,
            GunInfo gun
    ) {
    }

    public static Optional<Runtime> runtime(ItemStack stack) {
        return Gunsmith
                .getGunInfo(stack)
                .flatMap(gi -> Optional
                        .ofNullable(OverheatMechanic.DATA_MAP.get(gi.gunId()))
                        .map(ei -> new Runtime(ei, gi))
                );
    }

    public static final Codec<OverheatData> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                    Codec.INT.fieldOf("shots_before_overheat").forGetter(OverheatData::shotsBeforeOverheat),
                    Codec.INT.fieldOf("partial_heat_delay").forGetter(OverheatData::partialHeatDelay),
                    Codec.INT.fieldOf("full_heat_delay").forGetter(OverheatData::fullHeatDelay),
                    Codec.INT.fieldOf("cool_delay").forGetter(OverheatData::coolDelay),
                    Codec.INT.fieldOf("cool_count").forGetter(OverheatData::coolCount)
            )
            .apply(inst, OverheatData::new));
}
