package org.research.api.recipe.helper;

import net.minecraft.resources.ResourceLocation;
import org.research.api.recipe.category.CatalystsRegistration;

public interface ResearchPlugin {

    ResourceLocation getPluginId();

    void registerRecipeCategories(CatalystsRegistration registration);

    
}
