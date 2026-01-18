package org.research.api.recipe.helper;

import net.minecraft.resources.ResourceLocation;
import org.research.Research;
import org.research.api.recipe.category.CatalystsRegistration;
import org.research.api.recipe.category.EmptyCategory;

public class EmptyResearchPlugin implements ResearchPlugin{

    @Override
    public ResourceLocation getPluginId() {
        return Research.empty();
    }

    @Override
    public void registerRecipeCategories(CatalystsRegistration registration) {
        registration.addRecipeCategory(new EmptyCategory());
    }
}
