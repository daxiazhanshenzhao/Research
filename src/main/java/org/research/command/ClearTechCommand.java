package org.research.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.research.Research;
import org.research.api.tech.capability.ITechTreeCapability;
import org.research.api.util.ResearchApi;

@Mod.EventBusSubscriber
public class ClearTechCommand {

    @SubscribeEvent
    public static void onServerStarting(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal(Research.MODID)
                .then(Commands.literal("clear")
                        .requires(source -> source.hasPermission(2)) // 需要管理员权限（等级2）
                        .executes(context -> clearTech(context, context.getSource().getPlayerOrException()))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ClearTechCommand::clearTech))));
    }

    /**
     * 清除玩家的科技数据
     * 命令：/research clear [player_name]
     * 仅限服务器管理员（权限等级2）使用
     * 如果不指定玩家，则清除命令执行者自己的数据
     */
    private static int clearTech(CommandContext<CommandSourceStack> context, ServerPlayer player) {
        CommandSourceStack source = context.getSource();

        // 获取玩家的科技数据并重置
        ResearchApi.getTechTreeData(player).ifPresent(iTechTreeCapability -> {
            iTechTreeCapability.resetAllTech();
        });

        // 发送成功消息
        if (source.getPlayer() == player) {
            source.sendSuccess(() -> Component.literal("§a已清除你的所有科技数据！"), false);
        } else {
            source.sendSuccess(() -> Component.literal("§a已清除 " + player.getDisplayName().getString() + " 的所有科技数据！"), false);
        }

        return 1;
    }

    private static int clearTech(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        try {
            // 获取命令中指定的玩家
            ServerPlayer targetPlayer = EntityArgument.getPlayer(context, "player");
            return clearTech(context, targetPlayer);
        } catch (CommandSyntaxException e) {
            source.sendFailure(Component.literal("§c玩家不存在！"));
            return 0;
        }
    }
}
