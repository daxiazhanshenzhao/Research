package org.research.api.recipe.category;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import org.research.Research;
import org.research.api.recipe.RecipeIngredientRole;

import java.util.List;

public class EmptyCategory extends RecipeCategory<CraftingRecipe> {


    @Override
    protected ResourceLocation getBackGround() {
        return Research.empty();
    }

    @Override
    public void setRecipe(CraftingRecipe recipe) {
        this.builder.addSlot(0,0,0, RecipeIngredientRole.INPUT).addItems(List.of(ItemStack.EMPTY));
    }

    @Override
    public RecipeType<?> getRecipeType() {
        return RecipeType.CRAFTING;
    }


}
