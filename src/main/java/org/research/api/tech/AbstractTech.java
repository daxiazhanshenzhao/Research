package org.research.api.tech;

import com.alessandro.astages.core.stage.AStage;
import com.alessandro.astages.store.server.ARestriction;
import com.alessandro.astages.util.ARestrictionType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.Event;
import org.research.Research;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public abstract class AbstractTech {


    private @Nullable ResourceLocation identifier;

    //config
//    private @Nullable AStage stage;
//    private @Nullable ARestrictionType restriction;
//
//    private List<ResourceLocation> parent;
//    private List<ResourceLocation> child;





    public AbstractTech(ResourceLocation identifier) {
        this.identifier = identifier;
    }

    public final String getId(){
        var resourceLocation = Objects.requireNonNull(getIdentifier());
        return resourceLocation.getPath().intern();
    }




    @Nullable
    public final ResourceLocation getIdentifier() {
        return identifier;
    }

    public final ResourceLocation getIconResource(){
        return ResourceLocation.fromNamespaceAndPath(getIdentifier().getNamespace(), "textures/gui/tech_icons/" + getId() + ".png");
    }

    public final ResourceLocation getARestrictionTypeResource(){
        return ResourceLocation.fromNamespaceAndPath(getIdentifier().getNamespace(),"textures/gui/restriction/" + getTechBuilder().restriction + ".png");
    }

    public abstract TechBuilder getTechBuilder();

    public abstract void eventHandle(Event event);



}
