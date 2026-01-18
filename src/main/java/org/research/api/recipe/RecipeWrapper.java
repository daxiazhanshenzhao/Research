package org.research.api.recipe;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;

public record RecipeWrapper(RecipeType<?> type, ResourceLocation recipe) {
    public static RecipeWrapper Craft(ResourceLocation ironIngotFromNuggets) {
        return new RecipeWrapper(RecipeType.CRAFTING, ironIngotFromNuggets);
    }
}
