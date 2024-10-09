package mod.chloeprime.modtechpoweredarsenal.common.api.standard.events;

import mod.chloeprime.gunsmithlib.api.util.GunInfo;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Cancelable;

@Cancelable
public class GunCoolEvent extends GunThermalEvent {

    public GunCoolEvent(LivingEntity shooter, GunInfo gun, int oldHeat, int newHeat) {
        super(shooter, gun, oldHeat, newHeat);
    }

    @HasResult
    public static class Pre extends GunCoolEvent {
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

    public static class Post extends GunCoolEvent {
        public Post(LivingEntity shooter, GunInfo gun, int oldHeat, int newHeat) {
            super(shooter, gun, oldHeat, newHeat);
        }
    }
}
