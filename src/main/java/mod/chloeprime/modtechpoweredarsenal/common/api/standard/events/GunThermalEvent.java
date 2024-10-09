package mod.chloeprime.modtechpoweredarsenal.common.api.standard.events;

import mod.chloeprime.gunsmithlib.api.util.GunInfo;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;

public class GunThermalEvent extends LivingEvent {
    protected final GunInfo gun;
    protected final int oldHeat;
    protected int newHeat;

    public GunThermalEvent(LivingEntity entity, GunInfo gun, int oldHeat, int newHeat) {
        super(entity);
        this.gun = gun;
        this.oldHeat = oldHeat;
        this.newHeat = newHeat;
    }

    public final int getOldHeat() {
        return oldHeat;
    }

    public final int getNewHeat() {
        return newHeat;
    }

    public final GunInfo getGun() {
        return gun;
    }
}
