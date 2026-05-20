package yiz.mc.xg.data;

import net.minecraft.world.entity.player.Player;
import net.minecraft.client.yiz.api.AttributeBalanceRegistry;
import net.minecraft.client.yiz.api.PlayerDataAPI;

/**
 * 星空体玩家数据的统一访问入口
 * 基于前置模组的 PlayerDataAPI，数据自动持久化到玩家 NBT 存档
 */
public class StarDataHelper {
    public static final String KEY_STAR_BODY = "yizxgmod:star_body";
    public static final String KEY_STAR_LEVEL = "yizxgmod:star_level";

    public static boolean hasStarBody(Player player) {
        return PlayerDataAPI.get(player, KEY_STAR_BODY);
    }

    public static int getStarLevel(Player player) {
        return PlayerDataAPI.get(player, KEY_STAR_LEVEL);
    }

    public static void setHasStarBody(Player player, boolean value) {
        PlayerDataAPI.set(player, KEY_STAR_BODY, value);
    }

    public static void setStarLevel(Player player, int level) {
        PlayerDataAPI.set(player, KEY_STAR_LEVEL, Math.max(0, Math.min(10, level)));
    }

    public static boolean canAddLayer(Player player) {
        return hasStarBody(player) && getStarLevel(player) < 10;
    }

    public static boolean canConsumeLayer(Player player) {
        return hasStarBody(player) && getStarLevel(player) > 0;
    }

    public static void incrementLevel(Player player) {
        setStarLevel(player, Math.min(10, getStarLevel(player) + 1));
    }

    public static void decrementLevel(Player player) {
        setStarLevel(player, Math.max(0, getStarLevel(player) - 1));
    }

    public static void giveStarBody(Player player) {
        setHasStarBody(player, true);
        setStarLevel(player, 0);
        AttributeBalanceRegistry.enableFor(player);
    }

    public static void discardAll(Player player) {
        PlayerDataAPI.discardAll(player);
    }
}
