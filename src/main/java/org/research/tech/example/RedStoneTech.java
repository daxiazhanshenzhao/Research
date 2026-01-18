package org.research.tech.example;

import net.minecraft.resources.ResourceLocation;
import org.research.Research;
import org.research.api.recipe.RecipeWrapper;
import org.research.api.tech.AbstractTech;
import org.research.api.tech.TechBuilder;

public class RedStoneTech extends AbstractTech {

    public RedStoneTech() {
        super(Research.asResource("red_tone_tech"));
    }

    @Override
    public TechBuilder getTechBuilder() {
        return TechBuilder.Builder()
                .addStage(1)
//                .addParent(TechInit.FIRST_TECH.getId())
                .addRecipe(RecipeWrapper.Craft(ResourceLocation.withDefaultNamespace("redstone_block")))
                .build();
    }


}
