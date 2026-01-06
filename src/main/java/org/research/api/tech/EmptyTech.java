package org.research.api.tech;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;
import org.research.Research;
import org.research.api.recipe.RecipeWrapper;

public class EmptyTech extends AbstractTech {


    public EmptyTech() {
        super(Research.asResource("empty_tech"));
    }

    @Override
    public TechBuilder getTechBuilder() {
        return TechBuilder.Builder()
                .addStage(0)
                .addRecipe(new RecipeWrapper(RecipeType.CRAFTING,
                        ResourceLocation.withDefaultNamespace("craft_table")))
                .build();
    }
}
