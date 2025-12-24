package org.research.api.recipe;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;

public interface RecipeWrapper {

    void addRecipe(RecipeType<?> type, ResourceLocation recipe);
}
