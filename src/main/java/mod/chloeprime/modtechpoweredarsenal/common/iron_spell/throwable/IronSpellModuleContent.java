package mod.chloeprime.modtechpoweredarsenal.common.iron_spell.throwable;

import me.xjqsh.lrtactical.init.ModRegistries;
import me.xjqsh.lrtactical.item.throwable.ThrowableType;
import mod.chloeprime.modtechpoweredarsenal.ModTechPoweredArsenal;
import mod.chloeprime.modtechpoweredarsenal.common.standard.entities.VirtualCaster;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class IronSpellModuleContent {
    private static final DeferredRegister<EntityType<?>> ENTITY_TYPE_DFR = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ModTechPoweredArsenal.MODID);
    private static final DeferredRegister<ThrowableType<?, ?>> DATA_TYPE_DFR = DeferredRegister.create(ModRegistries.THROWABLE_TYPE, ModTechPoweredArsenal.MODID);

    // 实体

    /**
     * 虚拟施法者
     */
    public static final RegistryObject<EntityType<VirtualCaster>> VIRTUAL_CASTER = ENTITY_TYPE_DFR.register(
            "virtual_caster", () -> VirtualCaster.TYPE
    );

    /**
     * 魔法手雷通用实体
     */
    public static final RegistryObject<EntityType<IronSpellGrenade>> IRON_SPELL_GRENADE = ENTITY_TYPE_DFR.register(
            "iron_spell_grenade", () -> IronSpellGrenade.TYPE
    );

    // 投掷物类型

    /**
     * 魔法手雷
     */
    public static final RegistryObject<ThrowableType<IronSpellGrenadeData, IronSpellGrenade>> IRON_SPELL_GRENADE_DATA = DATA_TYPE_DFR.register("iron_spell", () -> IronSpellGrenade.DATA_TYPE);

    public static void init(IEventBus bus) {
        ENTITY_TYPE_DFR.register(bus);
        DATA_TYPE_DFR.register(bus);
    }
}
