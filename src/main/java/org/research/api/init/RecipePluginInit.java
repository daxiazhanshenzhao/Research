package org.research.api.init;



import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.crafting.RecipeType;
import org.research.Research;
import org.research.api.recipe.AutoResearchPlugin;
import org.research.api.recipe.category.CatalystsRegistration;
import org.research.api.recipe.transfer.RecipeTransferRegistration;
import org.research.category.CraftingTableCategory;
import org.research.api.recipe.ResearchPlugin;



@AutoResearchPlugin
public class RecipePluginInit implements ResearchPlugin {
    @Override
    public ResourceLocation getPluginId() {
        return Research.asResource("research_plugin");
    }

    @Override
    public void registerRecipeCategories(CatalystsRegistration registration) {
        registration.addRecipeCategory(RecipeType.CRAFTING, new CraftingTableCategory());
    }

    @Override
    public void registerRecipeTransfer(RecipeTransferRegistration registration) {
//        IRecipeTransferRegistration
        registration.addRecipeTransferHandler(RecipeType.CRAFTING,CraftingMenu.class, MenuType.CRAFTING, 1, 9, 10, 36);
        registration.addRecipeTransferHandler(RecipeType.SMELTING, FurnaceMenu.class, MenuType.FURNACE,0, 1, 3, 36);
        registration.addRecipeTransferHandler(RecipeType.SMOKING, SmokerMenu.class, MenuType.SMOKER,0, 1, 3, 36);
        registration.addRecipeTransferHandler(RecipeType.BLASTING, BlastFurnaceMenu.class, MenuType.BLAST_FURNACE,0, 1, 3, 36);

        registration.addRecipeTransferHandler(RecipeType.SMITHING, SmithingMenu.class, MenuType.SMITHING,0, 3, 3, 36);

    }
}
