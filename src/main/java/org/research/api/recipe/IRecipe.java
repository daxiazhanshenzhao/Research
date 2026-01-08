package org.research.api.recipe;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.Optional;

public interface IRecipe {

    RecipeWrapper getRecipe();


    static Recipe<?> getRecipeFromWrapper(RecipeWrapper recipeWrapper, MinecraftServer server){
        var recipeId = recipeWrapper.recipe();
        var type = recipeWrapper.type();
        var recipeManager = server.getRecipeManager();
        
        // 使用byKey直接获取配方
        return getRecipe(recipeId, type, recipeManager);
    }



    static Recipe<?> getClientRecipe(RecipeWrapper recipeWrapper, Minecraft client){
        var recipeId = recipeWrapper.recipe();
        var type = recipeWrapper.type();

        // 从客户端获取配方管理器
        if (client.level == null) {
            return null;
        }

        RecipeManager recipeManager = client.level.getRecipeManager();

        // 使用byKey直接获取配方
        return getRecipe(recipeId, type, recipeManager);
    }

    private static Recipe<?> getRecipe(ResourceLocation recipeId, RecipeType<?> type, RecipeManager recipeManager) {
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
