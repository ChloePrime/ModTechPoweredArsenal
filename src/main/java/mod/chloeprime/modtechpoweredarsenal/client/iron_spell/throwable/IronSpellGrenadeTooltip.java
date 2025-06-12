package mod.chloeprime.modtechpoweredarsenal.client.iron_spell.throwable;

import io.redspace.ironsspellbooks.api.events.ModifySpellLevelEvent;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import me.xjqsh.lrtactical.api.LrTacticalAPI;
import me.xjqsh.lrtactical.api.item.IThrowable;
import mod.chloeprime.modtechpoweredarsenal.common.iron_spell.throwable.IronSpellGrenadeData;
import mod.chloeprime.modtechpoweredarsenal.common.standard.util.RegistryHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.commons.lang3.mutable.MutableDouble;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.Deque;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

@SuppressWarnings("deprecation")
public final class IronSpellGrenadeTooltip {
    private static final Minecraft MC = Minecraft.getInstance();
    private static final Deque<ItemStack> tooltipCallLayers = new ConcurrentLinkedDeque<>();
    private static final Supplier<Attribute> SPELL_POWER = RegistryHelper.holder(BuiltInRegistries.ATTRIBUTE, "irons_spellbooks", "spell_power");
    private static final UUID GRENADE_SPELL_POWER_DISPLAY_MODIFIER_ID = UUID.fromString("91f3fb2c-634b-4bc8-a7ac-2f495e536eff");

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public static void onBeginTooltip(ItemTooltipEvent event) {
        tooltipCallLayers.push(event.getItemStack());
        var spellPowerAttribute = SPELL_POWER.get();
        if (spellPowerAttribute == null) {
            return;
        }
        ifIsGrenade(event.getItemStack(), IronSpellGrenadeTooltip::onBeginTooltip0);
    }

    private static void onBeginTooltip0(Player player, IronSpellGrenadeData data) {
        var spellPowerAttribute = Objects.requireNonNull(SPELL_POWER.get());
        var spell = SpellRegistry.getSpell(data.getSpellId());
        var attributeInstance = player.getAttribute(spellPowerAttribute);
        if (attributeInstance == null) {
            return;
        }
        var oldPower = attributeInstance.getValue();
        if (oldPower == 0) {
            return;
        }
        var newPower = new MutableDouble(oldPower);
        data.adjustSpellPower(spell, 1, oldPower, _i -> {}, newPower::setValue);

        if (newPower.doubleValue() == oldPower) {
            return;
        }

        var modifier = new AttributeModifier(
                GRENADE_SPELL_POWER_DISPLAY_MODIFIER_ID,
                "Magic Grendade Tooltip Power Modifier",
                newPower.doubleValue() / oldPower - 1,
                AttributeModifier.Operation.MULTIPLY_TOTAL
        );
        attributeInstance.addTransientModifier(modifier);
    }

    @SubscribeEvent
    public static void modifyDisplayedSpellLevel(ModifySpellLevelEvent event) {
        ItemStack current = tooltipCallLayers.peekFirst();
        if (current == null) {
            return;
        }
        ifIsGrenade(current, ((player, data) -> {
            var level = new MutableInt();
            data.adjustSpellPower(event.getSpell(), event.getLevel(), 1, level::setValue, _p -> {});
            event.setLevel(level.intValue());
        }));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void onEndTooltip(ItemTooltipEvent event) {
        if (tooltipCallLayers.pollFirst() == null) {
            return;
        }
        var spellPowerAttribute = SPELL_POWER.get();
        if (spellPowerAttribute == null) {
            return;
        }
        ifIsGrenade(event.getItemStack(), IronSpellGrenadeTooltip::onEndTooltip0);
    }

    /**
     * 移除用于反映手雷 tooltip 强度增强的临时 modifier
     */
    private static void onEndTooltip0(Player player, IronSpellGrenadeData data) {
        var spellPowerAttribute = Objects.requireNonNull(SPELL_POWER.get());
        var attributeInstance = player.getAttribute(spellPowerAttribute);
        if (attributeInstance == null) {
            return;
        }
        attributeInstance.removeModifier(GRENADE_SPELL_POWER_DISPLAY_MODIFIER_ID);
    }

    private static void ifIsGrenade(ItemStack grenade, BiConsumer<Player, IronSpellGrenadeData> code) {
        var localPlayer = MC.player;
        if (localPlayer == null) {
            return;
        }
        var grenadeItem = IThrowable.of(grenade);
        if (grenadeItem == null) {
            return;
        }
        LrTacticalAPI.getThrowableIndex(grenade).ifPresent(index -> {
            if (index.getData() instanceof IronSpellGrenadeData data) {
                code.accept(localPlayer, data);
            }
        });
    }
}
