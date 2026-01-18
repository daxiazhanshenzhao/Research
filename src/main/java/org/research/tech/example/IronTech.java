package org.research.tech.example;

import net.minecraft.resources.ResourceLocation;
import org.research.Research;
import org.research.api.recipe.RecipeWrapper;
import org.research.api.tech.AbstractTech;
import org.research.api.tech.TechBuilder;

public class IronTech extends AbstractTech {
    public IronTech() {
        super(Research.asResource("iron_tech"));
    }

    @Override
    public TechBuilder getTechBuilder() {
        return TechBuilder.Builder()
                .addStage(3)
//                .addParent(TechInit.FIRST_TECH.getId())
                .addRecipe(RecipeWrapper.Craft(ResourceLocation.withDefaultNamespace("iron_ingot_from_nuggets")))
                .build();
    }
}
