package org.research.api.recipe;

import net.minecraft.client.multiplayer.chat.report.ReportEnvironment;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface IRecipe {

    RecipeWrapper getRecipe();


    static Recipe<?> getRecipeFromWrapper(RecipeWrapper recipeWrapper, MinecraftServer server){
        var recipeId = recipeWrapper.recipe();
        var type = recipeWrapper.type();
        var recipeManager = server.getRecipeManager();
        
        // 使用byKey直接获取配方
        Optional<? extends Recipe<?>> recipeOpt = recipeManager.byKey(recipeId);
        
        if (recipeOpt.isPresent()) {
            Recipe<?> recipe = recipeOpt.get();
            // 检查配方类型是否匹配
            if (recipe.getType().equals(type)) {
                return recipe;
            }
        }
        return null;
    }



}
