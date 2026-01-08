package org.research.tech.stage4;

import net.minecraft.resources.ResourceLocation;
import org.research.Research;
import org.research.api.init.TechInit;
import org.research.api.recipe.RecipeWrapper;
import org.research.api.tech.AbstractTech;
import org.research.api.tech.TechBuilder;

public class ITech extends AbstractTech {
    public ITech() {
        super(Research.asResource("i_tech"));
    }

    @Override
    public TechBuilder getTechBuilder() {
        return TechBuilder.Builder()
                .addStage(4)
                .addParent(TechInit.E_TECH.getId())
                .addRecipe(RecipeWrapper.Craft(ResourceLocation.withDefaultNamespace("bucket")))
                .build();
    }
}
