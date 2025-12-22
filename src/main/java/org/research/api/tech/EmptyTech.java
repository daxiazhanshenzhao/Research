package org.research.api.tech;

import com.alessandro.astages.util.ARestrictionType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.Event;
import org.research.Research;

public class EmptyTech extends AbstractTech{


    public EmptyTech() {
        super(Research.asResource("empty_tech"));
    }

    @Override
    public TechBuilder getTechBuilder() {
        return TechBuilder.Builder()
                .setRestriction(ARestrictionType.RECIPE)
                .addStage("empty_tech");
    }

    @Override
    public void eventHandle(Event event) {

    }
}
