package mod.chloeprime.modtechpoweredarsenal.common.iron_spell.throwable;

import me.xjqsh.lrtactical.item.throwable.ThrowableData;
import net.minecraft.resources.ResourceLocation;

public class IronSpellGrenadeData extends ThrowableData {
    @SuppressWarnings("unused")
    private ResourceLocation spell_id;

    @SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
    private int spell_level = 1;

    public final ResourceLocation getSpellId() {
        return spell_id;
    }

    public final int getSpellLevel() {
        return spell_level;
    }
}
