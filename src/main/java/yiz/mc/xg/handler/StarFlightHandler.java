package yiz.mc.xg.handler;

import net.minecraft.world.entity.player.Player;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import yiz.mc.xg.data.StarDataHelper;

public class StarFlightHandler {

    public static void grantFlight(Player player) {
        if (!player.getAbilities().mayfly) {
            player.getAbilities().mayfly = true;
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        if (StarDataHelper.hasStarBody(player) && !player.getAbilities().mayfly) {
            grantFlight(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        if (StarDataHelper.hasStarBody(player) && !player.getAbilities().mayfly) {
            grantFlight(player);
        }
    }
}
