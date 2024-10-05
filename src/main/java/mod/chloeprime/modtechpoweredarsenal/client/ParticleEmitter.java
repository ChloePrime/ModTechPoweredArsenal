package mod.chloeprime.modtechpoweredarsenal.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.TrackingEmitter;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.Random;
import java.util.random.RandomGenerator;

class ParticleEmitter extends TrackingEmitter {
    public record Config(
            int lifetime,
            int countPerSpawn,
            int spawnDelay,
            Vec3 velocity
    ) {}

    private int life;
    private final int salt;
    private final Config config;
    private final ParticleOptions particleType;

    private static final RandomGenerator RNG = new Random();

    public ParticleEmitter(ClientLevel pLevel, Entity nonsense, Vec3 pos, ParticleOptions particle, Config config) {
        super(pLevel, nonsense, particle, config.lifetime());
        this.xo = this.x = pos.x();
        this.yo = this.y = pos.y();
        this.zo = this.z = pos.z();
        this.xd = config.velocity().x();
        this.yd = config.velocity().y();
        this.zd = config.velocity().z();
        this.config = config;
        this.particleType = particle;
        this.salt = RNG.nextInt(config.spawnDelay());
        this.tick();
    }

    public void tick() {
        if (config == null) {
            return;
        }
        if ((life + salt) % config.spawnDelay() == 0) {
            var velX = config.velocity().x();
            var velY = config.velocity().y();
            var velZ = config.velocity().z();
            for (int i = 0; i < config.countPerSpawn(); ++i) {
                this.level.addParticle(this.particleType, false, x, y, z, velX, velY, velZ);
            }
        }

        ++this.life;
        if (this.life >= this.config.lifetime()) {
            this.remove();
        }
    }
}
