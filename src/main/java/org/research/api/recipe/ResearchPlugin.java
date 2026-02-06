package org.research.api.recipe;

import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.resources.ResourceLocation;
import org.research.api.recipe.category.CatalystsRegistration;
import org.research.api.recipe.transfer.RecipeTransferRegistration;

public interface ResearchPlugin {

    public abstract ResourceLocation getPluginId();

    public abstract void registerRecipeCategories(CatalystsRegistration registration);

    default void registerRecipeTransfer(RecipeTransferRegistration registration){

    }
}
