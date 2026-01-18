package org.research.api.recipe.category;

import java.util.ArrayList;
import java.util.List;

public class CatalystsRegistration {

    private List<RecipeCategory> recipeCategories = new ArrayList<>();

    public void addRecipeCategory(RecipeCategory<?> category) {
        this.recipeCategories.add(category);
    }

    public List<RecipeCategory> getRecipeCategories() {
        return recipeCategories;
    }


}
