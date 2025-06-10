package mod.chloeprime.modtechpoweredarsenal;

import net.minecraftforge.fml.ModList;

public class ModLoadStatus {
    public static final boolean L2C_INSTALLED = ModList.get().isLoaded("l2complements");
    public static final boolean L2H_INSTALLED = ModList.get().isLoaded("l2hostility");
    public static final boolean LRTAC_INSTALLED = ModList.get().isLoaded("lrtactical");
    public static final boolean IRON_SPELLBOOKS_INSTALLED = ModList.get().isLoaded("irons_spellbooks");
}
