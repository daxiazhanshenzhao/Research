package org.research.tech.stage3;

import net.minecraft.resources.ResourceLocation;
import org.research.Research;
import org.research.api.recipe.RecipeWrapper;
import org.research.api.tech.AbstractTech;
import org.research.api.tech.TechBuilder;

public class FTech extends AbstractTech {
    public FTech() {
        super(Research.asResource("f_tech"));
    }

    @Override
    public TechBuilder getTechBuilder() {
        return TechBuilder.Builder()
                .addStage(3)
                .addRecipe(RecipeWrapper.Craft(ResourceLocation.withDefaultNamespace("tnt")))
                .build();
    }
}
