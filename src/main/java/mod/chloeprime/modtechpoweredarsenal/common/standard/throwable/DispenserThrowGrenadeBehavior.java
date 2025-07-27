package mod.chloeprime.modtechpoweredarsenal.common.standard.throwable;

import com.mojang.authlib.GameProfile;
import me.xjqsh.lrtactical.api.item.IThrowable;
import mod.chloeprime.modtechpoweredarsenal.ModTechPoweredArsenal;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.FakePlayerFactory;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DispenserThrowGrenadeBehavior extends AbstractProjectileDispenseBehavior {
    private DispenserThrowGrenadeBehavior() {
    }

    public static final DispenseItemBehavior INSTANCE = new DispenserThrowGrenadeBehavior();
    private static final GameProfile VIRTUAL_THROWER_PROFILE = new GameProfile(
            UUID.fromString("b7b94a4b-9ad2-466c-b095-eb2768ffec8f"), "[Dispenser]"
    );

    public static Optional<DispenseItemBehavior> onGetDispenseMethodHook(ItemStack stack) {
        IThrowable item = IThrowable.of(stack);
        if (item == null || !ModTechPoweredArsenal.MODID.equals(item.getId(stack).getNamespace())) {
            return Optional.empty();
        }
        return Optional.of(INSTANCE);
    }

    @Override
    protected Projectile getProjectile(Level level, Position position, ItemStack stack) {
        var item = IThrowable.of(stack);
        var index = Optional.ofNullable(item).flatMap(item2 -> item2.getThrowableIndex(stack)).orElse(null);
        if (item == null || index == null || !(level instanceof ServerLevel serverLevel)) {
            ModTechPoweredArsenal.LOGGER.warn("Trying to dispense non-throwable item {} in class {}", stack, getClass().getCanonicalName());
            return new Arrow(level, position.x(), position.y(), position.z());
        }
        var grenade = index.createEntity(stack, FakePlayerFactory.get(serverLevel, VIRTUAL_THROWER_PROFILE));
        grenade.setPos(position.x(), position.y(), position.z());
        return grenade;
    }

    @Override
    protected float getPower() {
        return super.getPower() * 1.5F;
    }
}
