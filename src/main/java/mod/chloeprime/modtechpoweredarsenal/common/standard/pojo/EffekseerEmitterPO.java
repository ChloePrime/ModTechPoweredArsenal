package mod.chloeprime.modtechpoweredarsenal.common.standard.pojo;

import net.minecraft.resources.ResourceLocation;

import java.io.Serializable;

@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
public class EffekseerEmitterPO implements Serializable {
    private ResourceLocation id;
    private double scale = 1;

    public ResourceLocation getId() {
        return id;
    }

    public double getScale() {
        return scale;
    }
}
