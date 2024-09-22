package mod.chloeprime.modtechpoweredarsenal;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoader;

public class ModLoadStatus {
    public static final boolean L2C_INSTALLED = ModList.get().isLoaded("l2complements");
    public static final boolean L2H_INSTALLED = ModList.get().isLoaded("l2hostility");
}
