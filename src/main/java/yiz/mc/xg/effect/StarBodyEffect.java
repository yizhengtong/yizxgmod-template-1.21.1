package yiz.mc.xg.effect;

import java.util.List;
import java.util.Set;
import java.util.function.IntSupplier;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import net.minecraft.client.yiz.effect.AbstractEffect;
import net.minecraft.client.yiz.effect.activation.PassiveCondition;
import net.minecraft.client.yiz.effect.parent.ParentType;
import net.minecraft.client.yiz.effect.perception.EntityPerception;
import net.minecraft.client.yiz.effect.perception.PerceptionMode;
import net.minecraft.client.yiz.effect.rarity.Rarity;
import net.minecraft.client.yiz.effect.EffectContext;

import static yiz.mc.xg.yizxgMod.MODID;

public class StarBodyEffect extends AbstractEffect {

    private static IntSupplier levelSupplier = () -> 1;

    public static final StarBodyEffect INSTANCE = new StarBodyEffect();

    public StarBodyEffect() {
        super(
                ResourceLocation.parse(MODID + ":star_body"),
                "star_body",
                "§6天体衍化身",         // A
                ParentType.ASCENSION,
                1,
                Set.of(new EntityPerception()),
                new PassiveCondition(),
                Rarity.EPIC
        );
    }

    public static void setLevelSupplier(IntSupplier supplier) {
        levelSupplier = supplier;
    }

    @Override
    public int getLevel() {
        return levelSupplier.getAsInt();
    }

    @Override
    public String getDisplayName() {
        int lv = getLevel();
        return "§6天体衍化身 §f[§e星光×" + lv + "§f]";
    }

    @Override
    public void execute(EffectContext context) {
    }

    @Override
    public List<String> getTalentDetailLines(LivingEntity entity) {
        int lv = getLevel();

        // 格式B：斜黑色小字体（意境描述）
        String lineB1 = "§8§o天官赐福，地官赦罪，水官解厄";
        String lineB2 = "§8§o凌空之躯，火德天上，灰烬之神";

        // 格式C：白色常规字体（实际数值）
        int ampPct = lv * 8;
        int redPct = lv * 8;
        int atkPct = lv * 10;
        int revive = lv;
        int block = Math.min(lv, 3);

        String lineC1 = String.format("§f%d%%伤害增幅，%d%%伤害减免，%d%%回击", ampPct, redPct, atkPct);
        String lineC2 = String.format("§f%d次新生，%d点伤害格挡", revive, block);

        return List.of(lineB1, lineB2, lineC1, lineC2);
    }
}
