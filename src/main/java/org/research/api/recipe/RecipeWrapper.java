package org.research.api.recipe;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

public record RecipeWrapper(RecipeType<?> type, ResourceLocation recipe) {


}
