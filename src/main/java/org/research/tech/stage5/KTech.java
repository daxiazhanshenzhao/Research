package org.research.tech.stage5;

import net.minecraft.resources.ResourceLocation;
import org.research.Research;
import org.research.api.init.TechInit;
import org.research.api.recipe.RecipeWrapper;
import org.research.api.tech.AbstractTech;
import org.research.api.tech.TechBuilder;

public class KTech extends AbstractTech {
    public KTech() {
        super(Research.asResource("k_tech"));
    }

    @Override
    public TechBuilder getTechBuilder() {
        return TechBuilder.Builder()
                .addStage(5)
                .addParent(TechInit.J_TECH.getId())
                .addRecipe(RecipeWrapper.Craft(ResourceLocation.withDefaultNamespace("diamond_pickaxe")))
                .build();
    }
}
