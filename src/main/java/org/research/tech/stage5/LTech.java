package org.research.tech.stage5;

import net.minecraft.resources.ResourceLocation;
import org.research.Research;
import org.research.api.init.TechInit;
import org.research.api.recipe.RecipeWrapper;
import org.research.api.tech.AbstractTech;
import org.research.api.tech.TechBuilder;

public class LTech extends AbstractTech {
    public LTech() {
        super(Research.asResource("l_tech"));
    }

    @Override
    public TechBuilder getTechBuilder() {
        return TechBuilder.Builder()
                .addStage(5)
                .addParent(TechInit.I_TECH.getId())
                .addRecipe(RecipeWrapper.Craft(ResourceLocation.withDefaultNamespace("golden_pickaxe")))
                .build();
    }
}
