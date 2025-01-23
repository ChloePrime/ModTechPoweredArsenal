package mod.chloeprime.modtechpoweredarsenal.mixin.minecraft;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import mod.chloeprime.modtechpoweredarsenal.MTPA;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(NoteBlock.class)
public abstract class MixinNoteBlock {
    @WrapOperation(
            method = "triggerEvent",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/properties/NoteBlockInstrument;getSoundEvent()Lnet/minecraft/core/Holder;"))
    private Holder<SoundEvent> customNoteBlockSounds(NoteBlockInstrument instrument, Operation<Holder<SoundEvent>> original, BlockState state, Level level, BlockPos pos, int id, int param) {
        Block bottom = level.getBlockState(pos.below()).getBlock();
        if (bottom == MTPA.Blocks.GALLIUM_BLOCK.get()) {
            return MTPA.Sounds.GUN_INSTRUMENT.getHolder().orElseThrow();
        }
        return original.call(instrument);
    }
}
