package mod.chloeprime.modtechpoweredarsenal.common.iron_spell;

import net.minecraft.nbt.CompoundTag;

public class IronSpellProxy {
    public static CompoundTag getImbuableItemNBT() {
        return ISB_IMBUABLE_NBT;
    }

    private static final CompoundTag ISB_IMBUABLE_NBT = createImbuableItemTag();
    private static CompoundTag createImbuableItemTag() {
        var isbSpellContainerTag = new CompoundTag();
        isbSpellContainerTag.putInt("maxSpells", 1);
        isbSpellContainerTag.putBoolean("mustEquip", true);
        isbSpellContainerTag.putBoolean("spellWheel", false);

        var isbImbuableNbt = new CompoundTag();
        isbImbuableNbt.put("ISB_Spells", isbSpellContainerTag);
        return isbImbuableNbt;
    }
}
