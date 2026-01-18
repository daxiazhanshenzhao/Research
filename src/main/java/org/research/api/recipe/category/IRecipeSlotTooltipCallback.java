package org.research.api.recipe.category;

import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import net.minecraft.network.chat.Component;

import java.util.List;

@FunctionalInterface
public interface IRecipeSlotTooltipCallback {
    void onTooltip(IRecipeSlotView var1, List<Component> var2);
}