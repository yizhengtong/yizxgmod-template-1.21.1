package yiz.mc.xg;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import com.mojang.serialization.Codec;

import net.minecraft.client.yiz.api.CounterAttackRegistry;
import net.minecraft.client.yiz.api.DamageAttributeRegistry;
import net.minecraft.client.yiz.api.DamageReductionRegistry;
import net.minecraft.client.yiz.api.FlightAbilityRegistry;
import net.minecraft.client.yiz.api.FlightOptimizationRegistry;
import net.minecraft.client.yiz.api.KnockbackImmunityRegistry;
import net.minecraft.client.yiz.api.NoCollisionRegistry;
import net.minecraft.client.yiz.api.ProjectileImmunityRegistry;
import net.minecraft.client.yiz.api.ProjectileReflectionSystem;
import net.minecraft.client.yiz.api.ShaderManager;
import net.minecraft.client.yiz.api.UndyingRegistry;
import net.minecraft.client.yiz.api.PlayerDataAPI;

import yiz.mc.xg.data.StarDataHelper;
import yiz.mc.xg.effect.StarBodyEffect;
import yiz.mc.xg.handler.StarEventHandler;
import yiz.mc.xg.handler.StarFlightHandler;
import yiz.mc.xg.item.StarVoidItem;
import net.minecraft.world.entity.player.Player;

@Mod(yizxgMod.MODID)
public class yizxgMod {
    public static final String MODID = "yizxgmod";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredItem<StarVoidItem> STAR_VOID =
            ITEMS.register("star_void", StarVoidItem::new);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> STAR_TAB =
            CREATIVE_MODE_TABS.register("star_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.yizxgmod"))
                    .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
                    .icon(() -> STAR_VOID.get().getDefaultInstance())
                    .displayItems((params, output) -> {
                        output.accept(STAR_VOID.get());
                    })
                    .build());

    // 测试用：可动态修改的减免百分比（默认 0.08 = 8%）
    public static float REDUCTION_RATE = 0.08f;

    public yizxgMod(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("{} loading...", MODID);

        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        // 注册玩家数据键到前置模组的 PlayerDataAPI
        PlayerDataAPI.register(StarDataHelper.KEY_STAR_BODY, Codec.BOOL, false);
        PlayerDataAPI.register(StarDataHelper.KEY_STAR_LEVEL, Codec.intRange(0, 10), 0);

        // Agent 级伤害减免注册
        // 效果1: 每层 8% 伤害减免（优先执行）
        DamageReductionRegistry.register((entity, oldHealth, newHealth) -> {
            if (entity instanceof Player player && StarDataHelper.hasStarBody(player)) {
                int level = StarDataHelper.getStarLevel(player);
                if (level <= 0) return newHealth;
                float damage = oldHealth - newHealth;
                float reduction = level * REDUCTION_RATE;
                return oldHealth - damage * (1.0f - reduction);
            }
            return newHealth;
        });
        // 效果5: 固定 -1 减伤（最多3层），后执行
        DamageReductionRegistry.register((entity, oldHealth, newHealth) -> {
            if (entity instanceof Player player && StarDataHelper.hasStarBody(player)) {
                int level = StarDataHelper.getStarLevel(player);
                if (level <= 0) return newHealth;
                int flatReduce = Math.min(level, 3);
                float damage = oldHealth - newHealth;
                return oldHealth - Math.max(0, damage - flatReduce);
            }
            return newHealth;
        });

        // 效果3: 回击 — 每层 1/10，满层 100% 完美复刻
        CounterAttackRegistry.register((player, source) -> {
            if (StarDataHelper.hasStarBody(player)) {
                int level = StarDataHelper.getStarLevel(player);
                return level * 0.1f; // 1层=10% 10层=100%
            }
            return 0f;
        });

        // 效果4: 死亡复活 — 消耗一层星光满血复活（不死图腾动画 + 星之空图标）
        UndyingRegistry.register((entity, source) -> {
            if (entity instanceof Player player && StarDataHelper.canConsumeLayer(player)) {
                StarDataHelper.decrementLevel(player);
                return UndyingRegistry.ReviveResult.revive(player.getMaxHealth(),
                        new net.minecraft.world.item.ItemStack(STAR_VOID.get()));
            }
            return UndyingRegistry.ReviveResult.NONE;
        });

        // 效果5+: 投射物返还 — 3格范围，每5tick扫描，1.0速度
        ProjectileReflectionSystem.register(
                p -> StarDataHelper.hasStarBody(p),
                new ProjectileReflectionSystem.ReflectionConfig(3.0, 5, 1.0f)
        );
        // 效果5+: 投射物免疫（凋灵风格，兜底防线）
        ProjectileImmunityRegistry.register(
                e -> e instanceof Player p && StarDataHelper.hasStarBody(p)
        );
        // 6: 碰撞免疫
        NoCollisionRegistry.register(
                e -> e instanceof Player p && StarDataHelper.hasStarBody(p)
        );
        // 7: 击退霸体
        KnockbackImmunityRegistry.register(
                e -> e instanceof Player p && StarDataHelper.hasStarBody(p)
        );
        // 8: 飞行惯性优化
        FlightOptimizationRegistry.register(
                e -> e instanceof Player p && StarDataHelper.hasStarBody(p)
        );
        // 绝对飞行权：强制 mayFly() = true
        FlightAbilityRegistry.register(
                e -> e instanceof Player p && StarDataHelper.hasStarBody(p)
        );

        // 方法①-⼦：将原版攻击伤害属性绑定为伤害源，缩放系数初始为 0
        DamageAttributeRegistry.register(Attributes.ATTACK_DAMAGE, 0f);

        // 注册前置模组天赋效果
        net.minecraft.client.yiz.api.YizModQZKAPI.registerEffect(StarBodyEffect.INSTANCE);

        NeoForge.EVENT_BUS.register(StarEventHandler.class);
        NeoForge.EVENT_BUS.register(StarFlightHandler.class);

        // ====== 测试指令 ======

        // /yizxg zsq <编号> — 切换着色器预设
        net.minecraft.client.yiz.api.YizModQZKAPI.registerCommand(
                net.minecraft.commands.Commands.literal("yizxg")
                        .then(net.minecraft.commands.Commands.literal("zsq")
                                .then(net.minecraft.commands.Commands.argument("preset",
                                                com.mojang.brigadier.arguments.IntegerArgumentType.integer(1))
                                        .executes(ctx -> {
                                            int preset = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(ctx, "preset");
                                            String name = "cosmic" + (preset == 1 ? "" : preset);
                                            ShaderManager.setActivePreset(name);
                                            ctx.getSource().getPlayerOrException()
                                                    .sendSystemMessage(Component.literal("§a着色器已切换至预设: " + name));
                                            return 1;
                                        })))
        );

        // /yizxgmod agent — 检测 ASM Agent 是否已注入
        net.minecraft.client.yiz.api.YizModQZKAPI.registerSimpleCommand("yizxgmod_agent", ctx -> {
            Player player = ctx.getSource().getPlayerOrException();
            boolean loaded = net.minecraft.client.yiz.core.asm.AsmBootstrapper.isAgentLoaded();
            player.sendSystemMessage(
                    Component.literal("§6[Agent状态] §f已加载: " + (loaded ? "§a✓" : "§c✗"))
            );
            return 1;
        });

        // /yizxgmod test — 对自己造成 100 点伤害，检测减免是否生效
        net.minecraft.client.yiz.api.YizModQZKAPI.registerSimpleCommand("yizxgmod_test", ctx -> {
            Player player = ctx.getSource().getPlayerOrException();
            float before = player.getHealth();
            player.hurt(player.damageSources().generic(), 100);
            float after = player.getHealth();
            float taken = before - after;
            int level = StarDataHelper.getStarLevel(player);
            float expectedReduction = level * REDUCTION_RATE;
            player.sendSystemMessage(
                    Component.literal("§6[Agent测试] §f减免率: §a" + (int)(expectedReduction * 100) + "%"
                            + " §f减免前: §c" + (int)taken + "§f/§c100 §f剩余: §a" + (int)after)
            );
            return 1;
        });

        // /yizxgmod set_reduction <percent> — 修改减免百分比（如 0.1 = 10%）
        net.minecraft.client.yiz.api.YizModQZKAPI.registerCommand(
                net.minecraft.commands.Commands.literal("yizxgmod_set_reduction")
                        .then(net.minecraft.commands.Commands.argument("rate",
                                        com.mojang.brigadier.arguments.FloatArgumentType.floatArg(0, 1))
                                .executes(ctx -> {
                                    float rate = com.mojang.brigadier.arguments.FloatArgumentType.getFloat(ctx, "rate");
                                    REDUCTION_RATE = rate;
                                    ctx.getSource().getPlayerOrException()
                                            .sendSystemMessage(Component.literal("§a减免率已设为: " + (int)(rate * 100) + "%"));
                                    return 1;
                                }))
        );
    }
}
