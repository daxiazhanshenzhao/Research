package org.research.api.recipe.category;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import org.research.Research;

public class EmptyCategory extends RecipeCategory<CraftingRecipe> {


    @Override
    protected ResourceLocation getBackGround() {
        return Research.empty();
    }

    @Override
    protected void setRecipe(RecipeBuilder builder, CraftingRecipe recipe) {

    }

    @Override
    public RecipeType<?> getRecipeType() {
        return RecipeType.CRAFTING;
    }


}
