package org.research.tech.stage1;

import net.minecraft.resources.ResourceLocation;
import org.research.Research;
import org.research.api.recipe.RecipeWrapper;
import org.research.api.tech.AbstractTech;
import org.research.api.tech.TechBuilder;


public class ATech extends AbstractTech {
    public ATech() {
        super(Research.asResource("a_tech"));
    }

    @Override
    public TechBuilder getTechBuilder() {
        return TechBuilder.Builder()
                .addStage(1)
                .addRecipe(RecipeWrapper.Craft(ResourceLocation.withDefaultNamespace("iron_ingot_from_nuggets")))
                .build();
    }
}
