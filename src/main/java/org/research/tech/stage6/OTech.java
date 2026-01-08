package org.research.tech.stage6;

import net.minecraft.resources.ResourceLocation;
import org.research.Research;
import org.research.api.recipe.RecipeWrapper;
import org.research.api.tech.AbstractTech;
import org.research.api.tech.TechBuilder;

public class OTech extends AbstractTech {
    public OTech() {
        super(Research.asResource("o_tech"));
    }

    @Override
    public TechBuilder getTechBuilder() {
        return TechBuilder.Builder()
                .addStage(6)
                .addRecipe(RecipeWrapper.Craft(ResourceLocation.withDefaultNamespace("brewing_stand")))
                .build();
    }
}
