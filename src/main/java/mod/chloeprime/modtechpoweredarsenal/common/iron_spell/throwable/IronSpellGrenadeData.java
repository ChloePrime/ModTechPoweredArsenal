package mod.chloeprime.modtechpoweredarsenal.common.iron_spell.throwable;

import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;import me.xjqsh.lrtactical.item.throwable.ThrowableData;
import mod.chloeprime.modtechpoweredarsenal.common.standard.pojo.EffekseerEmitterPO;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;

public class IronSpellGrenadeData extends ThrowableData {
    /**
     * 默认法术。这里填入的法术不受学派增益影响
     */
    @SuppressWarnings("unused")
    private ResourceLocation spell;

    /**
     * 默认法术的等级。
     * 这里填入的法术等级不受学派增益影响
     */
    @SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
    private int spell_level = 1;

    /**
     * 手雷爆炸时释放的 Effekseer 特效 ID
     */
    @SuppressWarnings("unused")
    private @Nullable EffekseerEmitterPO explode_fx;

    /**
     * 该手雷的专场学派。
     * 注入学派相同的法术会使得法术大幅增强，注入不同学派的法术会使得法术被大幅削弱。
     * 为空时则增强所有学派的法术。
     */
    @SuppressWarnings("unused")
    private @Nullable ResourceLocation school;

    /**
     * 当这一颗手雷注入和手雷学派相同学派的魔法时，法术等级的增益
     * 只会改变注入的法术的性能。不改变配置文件中指定的法术的性能。
     */
    @SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
    private int school_affinity_buff = 4;

    /**
     * 当这一颗手雷注入和手雷学派相同学派的魔法时，法术强度倍率增益
     * 只会改变注入的法术的性能。不改变配置文件中指定的法术的性能。
     */
    @SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
    private double school_power_buff = 0.5;

    /**
     * 如果为 true，则手雷注入和手雷学派不同学派的魔法时，法术等级会变为 1 级且只有 10% 法术强度。
     * 只会改变注入的法术的性能。不改变配置文件中指定的法术的性能。
     */
    @SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
    private boolean debuff_nonmatching_school = true;

    /**
     * 选取周围目标进行循环施法时候的选取范围（半径）
     */
    @SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
    private double iterative_casting_range = 6;

    public final ResourceLocation getDefaultSpellId() {
        return spell;
    }

    public final int getDefaultSpellLevel() {
        return spell_level;
    }

    public @Nullable EffekseerEmitterPO getExplodeFx() {
        return explode_fx;
    }

    public @Nullable ResourceLocation getSchoolId() {
        return school;
    }

    public double getSchoolPowerBuff() {
        return school_power_buff;
    }

    public int getSchoolAffinityBuff() {
        return school_affinity_buff;
    }

    public boolean willDebuffNonmatchingSchool() {
        return debuff_nonmatching_school;
    }

    public double getIterativeCastingRange() {
        return iterative_casting_range;
    }

    public void adjustSpellPower(AbstractSpell actualSpell, int originalSpellLevel, double originalPower, IntConsumer outLevel, DoubleConsumer outPower) {
        var grenadeSchoolId = getSchoolId();
        var sameSchool = grenadeSchoolId == null || Objects.equals(SchoolRegistry.getSchool(grenadeSchoolId), actualSpell.getSchoolType());
        if (sameSchool) {
            outLevel.accept(originalSpellLevel + getSchoolAffinityBuff());
            outPower.accept(originalPower * (1 + getSchoolPowerBuff()));
        } else {
            boolean debuff = willDebuffNonmatchingSchool();
            outLevel.accept(debuff ? 1 : originalSpellLevel);
            outPower.accept(debuff ? 0.1 : originalPower);
        }
    }
}
