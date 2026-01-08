package org.research.tech.stage4;

import net.minecraft.resources.ResourceLocation;
import org.research.Research;
import org.research.api.init.TechInit;
import org.research.api.recipe.RecipeWrapper;
import org.research.api.tech.AbstractTech;
import org.research.api.tech.TechBuilder;

public class GTech extends AbstractTech {
    public GTech() {
        super(Research.asResource("g_tech"));
    }

    @Override
    public TechBuilder getTechBuilder() {
        return TechBuilder.Builder()
                .addStage(4)
                .addParent(TechInit.H_TECH.getId())
                .addRecipe(RecipeWrapper.Craft(ResourceLocation.withDefaultNamespace("stone_pickaxe")))
                .build();
    }
}
