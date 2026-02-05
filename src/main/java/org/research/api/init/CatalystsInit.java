package org.research.api.init;


import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;
import org.research.Research;
import org.research.api.recipe.category.AutoResearchPlugin;
import org.research.api.recipe.category.CatalystsRegistration;
import org.research.category.CraftingTableCategory;
import org.research.api.recipe.helper.ResearchPlugin;



@AutoResearchPlugin
public class CatalystsInit implements ResearchPlugin {
    @Override
    public ResourceLocation getPluginId() {
        return Research.asResource("catalysts");
    }

    @Override
    public void registerRecipeCategories(CatalystsRegistration registration) {
        registration.addRecipeCategory(RecipeType.CRAFTING, new CraftingTableCategory());
    }
}
