package mod.chloeprime.modtechpoweredarsenal.mixin.tacz;

import com.tacz.guns.entity.EntityKineticBullet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = EntityKineticBullet.class, remap = false)
public interface KineticBulletAccessor {
    @Accessor float getGravity();
    @Accessor void setGravity(float value);
    @Accessor int getPierce();
    @Accessor void setPierce(int value);
}
