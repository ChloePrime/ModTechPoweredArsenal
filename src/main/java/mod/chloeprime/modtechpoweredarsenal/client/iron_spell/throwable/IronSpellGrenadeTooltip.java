package mod.chloeprime.modtechpoweredarsenal.client.iron_spell.throwable;

import io.redspace.ironsspellbooks.api.events.ModifySpellLevelEvent;
import me.xjqsh.lrtactical.api.LrTacticalAPI;
import me.xjqsh.lrtactical.api.item.IThrowable;
import mod.chloeprime.modtechpoweredarsenal.common.iron_spell.throwable.IronSpellGrenadeData;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.BiConsumer;

public final class IronSpellGrenadeTooltip {
    private static final Minecraft MC = Minecraft.getInstance();
    private static final Deque<ItemStack> tooltipCallLayers = new ConcurrentLinkedDeque<>();

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public static void onBeginTooltip(ItemTooltipEvent event) {
        tooltipCallLayers.push(event.getItemStack());
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
