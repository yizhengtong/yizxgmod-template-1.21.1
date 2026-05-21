package yiz.mc.xg;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;

import yiz.mc.xg.data.StarDataHelper;
import yiz.mc.xg.effect.StarBodyEffect;
import net.minecraft.client.yiz.api.StarShaderRegistry;

@Mod(value = yizxgMod.MODID, dist = Dist.CLIENT)
public class yizxgModClient {
    public yizxgModClient() {
        StarBodyEffect.setLevelSupplier(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                return Math.max(1, StarDataHelper.getStarLevel(mc.player));
            }
            return 1;
        });

        // 注册星空效果谓词：星之空物品
        StarShaderRegistry.registerStarItem(stack ->
                stack.is(yizxgMod.STAR_VOID.get())
        );

        // 注册星空效果谓词：拥有星空体的玩家身上所有盔甲
        StarShaderRegistry.registerStarArmor(stack -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return false;
            return StarDataHelper.hasStarBody(mc.player);
        });
    }
}
