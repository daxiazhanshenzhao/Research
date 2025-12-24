package org.research.api.tech;

import com.alessandro.astages.core.ARestrictionManager;
import com.alessandro.astages.core.server.restriction.recipe.ARecipeRestriction;
import com.alessandro.astages.core.stage.AStageManager;
import com.alessandro.astages.core.wrapper.RecipeWrapper;
import com.alessandro.astages.util.ARestrictionType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.Event;
import org.apache.maven.artifact.versioning.Restriction;
import org.research.Research;

public class EmptyTech extends AbstractTech{


    public EmptyTech() {
        super(Research.asResource("empty_tech"));
    }

    @Override
    public TechBuilder getTechBuilder() {
        return TechBuilder.Builder()
                .setRestriction(ARestrictionType.RECIPE)
                .addStage(0);
    }

    @Override
    public void eventHandle(Event event) {

    }

    @Override
    public void restriction() {

    }
}
