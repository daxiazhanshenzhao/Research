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
import org.research.api.provider.ScreenProvider;

@Mod.EventBusSubscriber
public class OpenResearchScreenCommand {
    @SubscribeEvent
    public static void onServerStarting(RegisterCommandsEvent event){
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal(Research.MODID)
                .then(Commands.literal("open")
                        .requires(source -> source.hasPermission(2)) // 需要管理员权限（等级2）
                        .executes(context -> openGui(context, context.getSource().getPlayerOrException()))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(OpenResearchScreenCommand::openGui))));
    }

    /**
     * 打开研究台GUI
     * 命令：/research open [player_name]
     * 仅限服务器管理员（权限等级2）使用
     * 如果不指定玩家，则为命令执行者自己打开
     */
    private static int openGui(CommandContext<CommandSourceStack> context, ServerPlayer player) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        
        ScreenProvider.serverOpenResearchScreen(player);
        
        // 发送成功消息
        if (source.getPlayer() == player) {
            source.sendSuccess(() -> Component.translatable("commands.research.open.success.self"), false);
        } else {
            source.sendSuccess(() -> Component.translatable("commands.research.open.success.other", player.getDisplayName()), false);
        }
        return 1;
    }
    
    private static int openGui(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        
        try {
            // 获取命令中指定的玩家
            ServerPlayer targetPlayer = EntityArgument.getPlayer(context, "player");
            return openGui(context, targetPlayer);
        } catch (CommandSyntaxException e) {
            source.sendFailure(Component.translatable("commands.research.open.player_not_found"));
            return 0;
        }
    }
}
