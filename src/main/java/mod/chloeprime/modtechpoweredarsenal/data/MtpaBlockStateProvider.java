package mod.chloeprime.modtechpoweredarsenal.data;

import mod.chloeprime.modtechpoweredarsenal.MTPA;
import mod.chloeprime.modtechpoweredarsenal.ModTechPoweredArsenal;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.function.Supplier;

public class MtpaBlockStateProvider extends BlockStateProvider {
    public MtpaBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, ModTechPoweredArsenal.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        simpleCubeAll(MTPA.Blocks.GALLIUM_ORE);
        simpleCubeAll(MTPA.Blocks.DEEPSLATE_GALLIUM_ORE);
        simpleCubeAll(MTPA.Blocks.GALLIUM_BLOCK);
        simpleCubeAll(MTPA.Blocks.RAW_GALLIUM_BLOCK);
    }

    private void simpleCubeAll(Supplier<Block> block) {
        simpleBlock(block.get(), cubeAll(block.get()));
        simpleBlockItem(block.get(), cubeAll(block.get()));
    }
}
