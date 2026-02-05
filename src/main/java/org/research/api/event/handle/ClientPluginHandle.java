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
import org.research.api.recipe.category.CatalystsRegistration;
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


    @SubscribeEvent
    public static void onTagsUpdated(TagsUpdatedEvent event) {

        List<ResearchPlugin> plugins =  ResearchPluginFinder.getModPlugins();
        var registration = ClientResearchData.recipeCategories;
        for (ResearchPlugin plugin : plugins) {
            plugin.registerRecipeCategories(registration);
            ClientResearchData.recipePluginData.add(plugin);
            Research.LOGGER.info("正在加载来自模组 {} 的研究配方插件 {}", plugin.getPluginId(), plugin.getClass().getName());
        }
    }






}
