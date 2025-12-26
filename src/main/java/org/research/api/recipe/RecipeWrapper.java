package org.research.api.recipe;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

import javax.annotation.Nullable;

public record RecipeWrapper(@Nullable RecipeType<?> type,@Nullable ResourceLocation recipe) {

    public static RecipeWrapper Craft(ResourceLocation recipe) {
        return new RecipeWrapper(RecipeType.CRAFTING,recipe);
    }
}
