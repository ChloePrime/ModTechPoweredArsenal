package mod.chloeprime.modtechpoweredarsenal.common.standard.util;

import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public class MoreMth {
    public static Vec3 randomUnitVector(RandomSource random) {
        return new Vec3(
                random.nextGaussian(),
                random.nextGaussian(),
                random.nextGaussian()
        ).normalize();
    }
}
