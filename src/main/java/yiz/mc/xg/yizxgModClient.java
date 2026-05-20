package yiz.mc.xg;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;

import yiz.mc.xg.data.StarDataHelper;
import yiz.mc.xg.effect.StarBodyEffect;

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
    }
}
