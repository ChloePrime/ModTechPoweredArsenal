package mod.chloeprime.modtechpoweredarsenal.common.iron_spell.throwable;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.tags.TagKey;

import static io.redspace.ironsspellbooks.api.registry.SpellRegistry.SPELL_REGISTRY_KEY;
import static mod.chloeprime.modtechpoweredarsenal.ModTechPoweredArsenal.loc;

public class IronSpellGrenadeCompatibilityTags {
    public static final TagKey<AbstractSpell> CAST_AS_NEARBY_TARGETS_ON_EXPLODE = TagKey.create(SPELL_REGISTRY_KEY, loc("cast_as_nearby_targets_on_explode"));
    public static final TagKey<AbstractSpell> FACES_OUTSIDE = TagKey.create(SPELL_REGISTRY_KEY, loc("faces_outside"));
    public static final TagKey<AbstractSpell> ITERATE_NEARBY_TARGETS_ON_EXPLODE = TagKey.create(SPELL_REGISTRY_KEY, loc("iterate_nearby_targets_on_explode"));
    public static final TagKey<AbstractSpell> ITERATE_RANDOM_POSITION_ON_EXPLODE = TagKey.create(SPELL_REGISTRY_KEY, loc("iterate_random_position_on_explode"));
    public static final TagKey<AbstractSpell> KEEP_OWNER_AS_CASTER = TagKey.create(SPELL_REGISTRY_KEY, loc("keep_owner_as_caster"));
    public static final TagKey<AbstractSpell> REQUIRES_ON_GROUND = TagKey.create(SPELL_REGISTRY_KEY, loc("requires_on_ground"));
    public static final TagKey<AbstractSpell> UNSUPPORTED = TagKey.create(SPELL_REGISTRY_KEY, loc("unsupported"));
}
