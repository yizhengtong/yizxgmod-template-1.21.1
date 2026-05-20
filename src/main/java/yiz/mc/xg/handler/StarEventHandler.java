package yiz.mc.xg.handler;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import net.minecraft.client.yiz.api.DamageAttributeRegistry;
import yiz.mc.xg.data.StarDataHelper;
import yiz.mc.xg.data.StarWorldData;
import yiz.mc.xg.effect.StarBodyEffect;
import static yiz.mc.xg.yizxgMod.STAR_VOID;

public class StarEventHandler {

    private static void updateStarScale(Player player) {
        if (StarDataHelper.hasStarBody(player) && StarDataHelper.getStarLevel(player) > 0) {
            DamageAttributeRegistry.register(Attributes.ATTACK_DAMAGE, StarDataHelper.getStarLevel(player) * 0.08f);
        } else {
            DamageAttributeRegistry.register(Attributes.ATTACK_DAMAGE, 0f);
        }
    }

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (event.getEntity() instanceof Player player && !player.level().isClientSide()) {
            updateStarScale(player);
        }
    }

    @SubscribeEvent
    public static void onWitherDrops(LivingDropsEvent event) {
        if (event.getEntity() instanceof WitherBoss wither && !wither.level().isClientSide()) {
            ItemEntity starVoid = new ItemEntity(
                    wither.level(),
                    wither.getX(), wither.getY(), wither.getZ(),
                    new ItemStack(STAR_VOID.get())
            );
            event.getDrops().add(starVoid);
        }
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        ItemStack stack = event.getItemStack();
        if (!stack.is(Items.NETHER_STAR)) return;

        if (!StarDataHelper.canAddLayer(player)) {
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§c星光已至巅峰，无法继续提升"),
                    true
            );
            return;
        }

        stack.shrink(1);
        StarDataHelper.incrementLevel(player);
        updateStarScale(player);
        StarFlightHandler.grantFlight(player);

        player.level().playSound(null, player.blockPosition(),
                SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 1.0F);
        player.displayClientMessage(
                net.minecraft.network.chat.Component.literal(
                        "§b星光汇聚！当前层数: " + StarDataHelper.getStarLevel(player) + "/10"),
                true
        );
    }

    @SubscribeEvent
    public static void onStarVoidUse(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        ItemStack stack = event.getItemStack();
        if (!stack.is(STAR_VOID.get())) return;

        StarWorldData worldData = StarWorldData.get(player.level());

        if (worldData.hasStarBodyClaimed() && !worldData.getStarBodyPlayerUUID().equals(player.getUUID())) {
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§c星之空已寻得主人，你无法使用"),
                    true
            );
            event.setCanceled(true);
            return;
        }

        if (StarDataHelper.hasStarBody(player)) {
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§e你已拥有星空体"),
                    true
            );
            event.setCanceled(true);
            return;
        }

        stack.shrink(1);
        worldData.claimStarBody(player);
        StarDataHelper.giveStarBody(player);

        updateStarScale(player);
        net.minecraft.client.yiz.api.YizModQZKAPI.unlockEffect(player, StarBodyEffect.INSTANCE.getId());
        StarFlightHandler.grantFlight(player);

        player.level().playSound(null, player.blockPosition(),
                SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1.0F, 1.0F);
        player.displayClientMessage(
                net.minecraft.network.chat.Component.literal("§d你获得了星空体！"),
                true
        );
        event.setCanceled(true);
    }
}
