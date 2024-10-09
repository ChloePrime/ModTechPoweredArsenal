package mod.chloeprime.modtechpoweredarsenal.common.api.standard.events;

import mod.chloeprime.gunsmithlib.api.util.GunInfo;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Cancelable;

@Cancelable
public class GunHeatEvent extends GunThermalEvent {

    public GunHeatEvent(LivingEntity shooter, GunInfo gun, int oldHeat, int newHeat) {
        super(shooter, gun, oldHeat, newHeat);
    }

    @HasResult
    public static class Pre extends GunHeatEvent {
        public Pre(LivingEntity shooter, GunInfo gun, int oldHeat, int newHeat) {
            super(shooter, gun, oldHeat, newHeat);
        }

        public final void setNewHeat(int newHeat) {
            this.newHeat = newHeat;
        }

        @Override
        public boolean hasResult() {
            return true;
        }
    }

    public static class Post extends GunHeatEvent {
        public Post(LivingEntity shooter, GunInfo gun, int oldHeat, int newHeat) {
            super(shooter, gun, oldHeat, newHeat);
        }
    }
}
