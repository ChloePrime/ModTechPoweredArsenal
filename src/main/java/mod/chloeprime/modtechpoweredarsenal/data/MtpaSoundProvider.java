package mod.chloeprime.modtechpoweredarsenal.data;

import mod.chloeprime.modtechpoweredarsenal.MTPA;
import mod.chloeprime.modtechpoweredarsenal.ModTechPoweredArsenal;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.SoundDefinition;
import net.minecraftforge.common.data.SoundDefinitionsProvider;

public class MtpaSoundProvider extends SoundDefinitionsProvider {
    public MtpaSoundProvider(PackOutput output, ExistingFileHelper helper) {
        super(output, ModTechPoweredArsenal.MODID, helper);
    }

    @Override
    public void registerSounds() {
        this.add(MTPA.Sounds.GUN_INSTRUMENT.getId(), definition()
                .subtitle("subtitles.block.note_block.note")
                .with(snd("instrument/glock17_shoot")));
    }

    @SuppressWarnings("SameParameterValue")
    private static SoundDefinition.Sound snd(String path) {
        return sound(ModTechPoweredArsenal.loc(path));
    }
}
