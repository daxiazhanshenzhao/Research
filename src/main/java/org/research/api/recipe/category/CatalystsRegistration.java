package org.research.api.recipe.category;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class CatalystsRegistration {

    private Map<RecipeType<?>, RecipeCategory<?>> recipeCategories = new HashMap<>();

    public void addRecipeCategory(RecipeType<?> recipeType, RecipeCategory<?> category) {
        this.recipeCategories.put(recipeType, category);
    }

    /**
     * 清除所有 RecipeCategory 的槽位缓存
     * 在窗口 resize 时调用，确保 RecipeTechSlot 根据新的窗口坐标重新创建
     */
    public void clearAllCache() {
        for (var category : recipeCategories.values()) {
            category.clearCache();
        }
    }


}
