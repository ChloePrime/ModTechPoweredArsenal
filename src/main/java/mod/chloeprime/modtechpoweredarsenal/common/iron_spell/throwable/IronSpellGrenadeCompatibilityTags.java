package mod.chloeprime.modtechpoweredarsenal.common.iron_spell.throwable;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import mod.chloeprime.modtechpoweredarsenal.ModTechPoweredArsenal;
import net.minecraft.tags.TagKey;

public class IronSpellGrenadeCompatibilityTags {
    public static final TagKey<AbstractSpell> FACES_OUTSIDE = TagKey.create(SpellRegistry.SPELL_REGISTRY_KEY, ModTechPoweredArsenal.loc("faces_outside"));
    public static final TagKey<AbstractSpell> ITERATE_NEARBY_TARGETS_ON_EXPLODE = TagKey.create(SpellRegistry.SPELL_REGISTRY_KEY, ModTechPoweredArsenal.loc("iterate_nearby_targets_on_explode"));
    public static final TagKey<AbstractSpell> ITERATE_RANDOM_POSITION_ON_EXPLODE = TagKey.create(SpellRegistry.SPELL_REGISTRY_KEY, ModTechPoweredArsenal.loc("iterate_random_position_on_explode"));
    public static final TagKey<AbstractSpell> KEEP_OWNER_AS_CASTER = TagKey.create(SpellRegistry.SPELL_REGISTRY_KEY, ModTechPoweredArsenal.loc("keep_owner_as_caster"));
    public static final TagKey<AbstractSpell> REQUIRES_ON_GROUND = TagKey.create(SpellRegistry.SPELL_REGISTRY_KEY, ModTechPoweredArsenal.loc("requires_on_ground"));
}
