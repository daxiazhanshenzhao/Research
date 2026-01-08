package org.research.tech.stage6;

import net.minecraft.resources.ResourceLocation;
import org.research.Research;
import org.research.api.init.TechInit;
import org.research.api.recipe.RecipeWrapper;
import org.research.api.tech.AbstractTech;
import org.research.api.tech.TechBuilder;

public class NTech extends AbstractTech {
    public NTech() {
        super(Research.asResource("n_tech"));
    }

    @Override
    public TechBuilder getTechBuilder() {
        return TechBuilder.Builder()
                .addStage(6)
                .addParent(TechInit.L_TECH.getId())
                .addParent(TechInit.M_TECH.getId())
                .addRecipe(RecipeWrapper.Craft(ResourceLocation.withDefaultNamespace("enchanting_table")))
                .build();
    }
}
