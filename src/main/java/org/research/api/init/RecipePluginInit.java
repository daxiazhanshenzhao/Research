package org.research.api.init;


import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.registration.IRecipeTransferRegistration;
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
        registration.addRecipeTransferHandler(RecipeTypes.CRAFTING,CraftingMenu.class, MenuType.CRAFTING, 1, 9, 10, 36);
        registration.addRecipeTransferHandler(RecipeTypes.SMELTING, FurnaceMenu.class, MenuType.FURNACE,0, 1, 3, 36);
        registration.addRecipeTransferHandler(RecipeTypes.FUELING, FurnaceMenu.class, MenuType.FURNACE,1, 1, 3, 36);
        registration.addRecipeTransferHandler(RecipeTypes.SMOKING, SmokerMenu.class, MenuType.SMOKER,0, 1, 3, 36);
        registration.addRecipeTransferHandler(RecipeTypes.FUELING,SmokerMenu.class, MenuType.SMOKER, 1, 1, 3, 36);
        registration.addRecipeTransferHandler(RecipeTypes.BLASTING, BlastFurnaceMenu.class, MenuType.BLAST_FURNACE,0, 1, 3, 36);
        registration.addRecipeTransferHandler(RecipeTypes.FUELING, BlastFurnaceMenu.class, MenuType.BLAST_FURNACE, 1, 1, 3, 36);
        registration.addRecipeTransferHandler(RecipeTypes.BREWING, BrewingStandMenu.class, MenuType.BREWING_STAND,0, 4, 5, 36);
        registration.addRecipeTransferHandler(RecipeTypes.ANVIL,AnvilMenu.class, MenuType.ANVIL, 0, 2, 3, 36);
        registration.addRecipeTransferHandler(RecipeTypes.SMITHING, SmithingMenu.class, MenuType.SMITHING,0, 3, 3, 36);

    }
}
