package mod.chloeprime.modtechpoweredarsenal.common.standard.guns;

import com.tacz.guns.api.entity.IGunOperator;
import mod.chloeprime.gunsmithlib.api.common.BulletCreateEvent;
import mod.chloeprime.gunsmithlib.api.util.GunInfo;
import mod.chloeprime.modtechpoweredarsenal.common.api.standard.EnhancedGunData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Comparator;
import java.util.Optional;

import static mod.chloeprime.modtechpoweredarsenal.common.api.standard.MtpaExtraConfigurations.AIM_ASSIST;

@Mod.EventBusSubscriber
public class AimAssistMechanic {
    @SubscribeEvent
    public static void onBulletCreate(BulletCreateEvent event) {
        var gun = event.getGunInfo();
        var bulletSpeed = event.getBullet().getDeltaMovement().length();
        if (bulletSpeed <= 1e-3) {
            return;
        }
        var assistPos = getAimedPosition(event.getShooter(), gun, 1).map(AimResult::pos).orElse(null);
        if (assistPos == null) {
            return;
        }

        var muzzlePos = event.getShooter().getEyePosition();
        if (assistPos.distanceToSqr(muzzlePos) < 1e-6) {
            return;
        }
        var newBulletMotion = assistPos.subtract(muzzlePos).normalize().scale(bulletSpeed);
        event.getBullet().setDeltaMovement(newBulletMotion);
    }

    public static Optional<AimResult> getAimedPosition(LivingEntity shooter, GunInfo gun, float partialTicks) {
        var ass = ((EnhancedGunData) gun.index().getGunData()).getConfiguration(AIM_ASSIST);
        if (ass.isEmpty()) {
            return Optional.empty();
        }
        var attributes = IGunOperator.fromLivingEntity(shooter).getCacheProperty();
        if (attributes == null) {
            return Optional.empty();
        }
        if (!(attributes.getCache("effective_range") instanceof Number range)) {
            return Optional.empty();
        }
        var assistAngle = Math.toRadians(ass.getAsDouble());
        return getAimedPosition(shooter, range.doubleValue(), assistAngle, partialTicks);
    }

    public record AimResult(
            Entity entity,
            Vec3 pos
    ) {
    }

    /**
     * @param coneAngle Cone angle in radians
     */
    public static Optional<AimResult> getAimedPosition(LivingEntity shooter, double range, double coneAngle, float partialTicks) {
        var lookAngle = shooter.getViewVector(partialTicks);
        var muzzle = shooter.getEyePosition(partialTicks);
        var testAreaAabb = shooter.getBoundingBox().expandTowards(lookAngle.scale(range)).inflate(2);
        var cosConeAngle = Math.cos(coneAngle);
        var rangeSqr = range * range;
        var candidates = shooter.level().getEntities(EntityTypeTest.forClass(LivingEntity.class), testAreaAabb, candidate -> candidate != shooter && shooter.canAttack(candidate));
        return candidates.stream()
                .flatMap(entity -> getEstimatedHitPos(shooter, entity, partialTicks).map(pos -> new AimResult(entity, pos)).stream())
                // 距离 <= 范围
                .filter(record -> record.pos().distanceToSqr(muzzle) <= rangeSqr)
                // 计算夹角
                .map(record -> {
                    var offset = record.pos().subtract(muzzle);
                    if (offset.lengthSqr() < 1e-6) {
                        return Pair.of(record, (Double)null);
                    }
                    var cos = offset.dot(lookAngle) / (offset.length() * 1/*lookAngle.length()*/);
                    return Pair.of(record, cos);
                })
                // 夹角 <= 自瞄范围
                .filter(pair -> pair.getRight() != null && pair.getRight() >= cosConeAngle)
                .min(Comparator.comparingDouble(pair -> getAimPriority(pair.getLeft().pos(), pair.getRight(), muzzle, range, coneAngle)))
                .map(Pair::getLeft);
    }

    public static Optional<Vec3> getEstimatedHitPos(LivingEntity shooter, Entity target, float partialTicks) {
        if (!(target instanceof LivingEntity)) {
            return getEstimatedHitPosForNonHumanoidTarget(shooter, target, partialTicks);
        } else {
            var targetBb = target.getBoundingBox();
            var maybeHumanoid = target.getPose() == Pose.STANDING && targetBb.getYsize() > Math.max(targetBb.getXsize(), targetBb.getZsize());
            if (!maybeHumanoid) {
                return getEstimatedHitPosForNonHumanoidTarget(shooter, target, partialTicks);
            }
            var footPos = target.getPosition(partialTicks);
            var eyePos = target.getEyePosition(partialTicks);
            for (var candidatePos : new Vec3[] {eyePos, eyePos.add(footPos).scale(0.5), footPos}) {
                if (hasLineOfSight(shooter, candidatePos, partialTicks)) {
                    return Optional.of(candidatePos);
                }
            }
            return Optional.empty();
        }
    }

    private static double getAimPriority(Vec3 targetPos, double cosAngle, Vec3 muzzle, double range, double aimConeAngle) {
        var distanceRatio = targetPos.distanceTo(muzzle) / range;
        var angleRatio = Math.acos(cosAngle) / aimConeAngle;
        return (distanceRatio * distanceRatio * 0.5 + angleRatio * angleRatio);
    }

    private static Optional<Vec3> getEstimatedHitPosForNonHumanoidTarget(LivingEntity shooter, Entity target, float partialTicks) {
        var position = target.getEyePosition(partialTicks);
        return hasLineOfSight(shooter, position, partialTicks)
                ? Optional.of(position)
                : Optional.empty();
    }

    private static boolean hasLineOfSight(LivingEntity shooter, Vec3 point, float partialTicks) {
        var start = shooter.getEyePosition(partialTicks);
        return shooter.level().clip(new ClipContext(start, point, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, shooter)).getType() == HitResult.Type.MISS;
    }
}
