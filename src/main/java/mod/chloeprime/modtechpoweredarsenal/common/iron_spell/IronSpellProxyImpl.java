package mod.chloeprime.modtechpoweredarsenal.common.iron_spell;

import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class IronSpellProxyImpl {
    public static Optional<SpellData> getFirstSpell(ItemStack stack) {
        return ISpellContainer.isSpellContainer(stack)
                ? ISpellContainer.get(stack).getActiveSpells().stream().findFirst()
                : Optional.empty();
    }
}
