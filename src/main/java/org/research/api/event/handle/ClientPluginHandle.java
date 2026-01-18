package org.research.api.event.handle;


import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.research.Research;
import org.research.api.client.ClientResearchData;
import org.research.api.recipe.helper.ResearchPlugin;
import org.research.api.util.ResearchPluginFinder;

import java.util.List;

/**
 * 客户端配方分类管理器
 *
 * 监听配方加载完毕的时机，用于初始化配方分类系统
 */
@Mod.EventBusSubscriber(modid = Research.MODID, value = Dist.CLIENT)
public class ClientPluginHandle {

    private static boolean recipesLoaded = false;
    private static RecipeManager cachedRecipeManager = null;

    /**
     * 配方加载完毕的最佳时机：TagsUpdatedEvent
     *
     * 这个事件在以下情况触发：
     * 1. 玩家登录世界时（客户端接收服务端数据）
     * 2. /reload 命令后（重新加载数据包）
     * 3. 资源包更新后
     *
     * 此时配方已经从服务端同步到客户端，可以安全访问 RecipeManager
     */
    @SubscribeEvent
    public static void onTagsUpdated(TagsUpdatedEvent event) {


            List<ResearchPlugin> plugins =  ResearchPluginFinder.getModPlugins();

            for (ResearchPlugin plugin : plugins) {
                ClientResearchData.recipePluginData.add(plugin);
                Research.LOGGER.info("正在加载来自模组 {} 的研究配方插件 {}", plugin.getPluginId(), plugin.getClass().getName());
            }

    }






}
