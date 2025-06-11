package mod.chloeprime.modtechpoweredarsenal.common.iron_spell.throwable;

import me.xjqsh.lrtactical.item.throwable.ThrowableData;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

public class IronSpellGrenadeData extends ThrowableData {
    @SuppressWarnings("unused")
    private ResourceLocation spell;

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

    @SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
    private int spell_level = 1;

    public final ResourceLocation getSpellId() {
        return spell;
    }

    public final int getSpellLevel() {
        return spell_level;
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
}
