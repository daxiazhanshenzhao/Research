package org.research.api.tech;

import com.alessandro.astages.core.stage.AStage;
import com.alessandro.astages.core.stage.AStageManager;
import com.alessandro.astages.util.ARestrictionType;
import net.minecraft.resources.ResourceLocation;
import org.research.api.recipe.RecipeWrapper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class TechBuilder {

    private TechBuilder(){}


//    public @Nullable ResourceLocation identifier;

//    public @Nullable ARestrictionType restriction;

    public @Nullable int stage = -1;
    public List<ResourceLocation> parent = new ArrayList<>();
    public @Nullable RecipeWrapper recipe;

//    @Deprecated
//    public List<ResourceLocation> child = new ArrayList<>();


    public static TechBuilder Builder(){
        return new TechBuilder();
    }


//    public TechBuilder setIdentifier(ResourceLocation identifier) {
//        this.identifier = identifier;
//        return this;
//    }

    public TechBuilder addStage(int stage) {
        this.stage = stage;
        return this;
    }



//    public TechBuilder setRestriction(ARestrictionType restriction) {
//        this.restriction = restriction;
//
//        return this;
//    }

//    public void addChild(ResourceLocation child) {
//        this.child.add(child);
//    }
    public TechBuilder addRecipe(RecipeWrapper recipe) {
        this.recipe = recipe;
        return this;
    }

    public TechBuilder addParent(ResourceLocation parent) {
        this.parent.add(parent);
        return this;
    }


    public TechBuilder builder(){
        if (!validate()){
            throw new RuntimeException("You didn't define all config attributes!");
        }
        return this;
    }

    private boolean validate(){
        return stage != -1 && recipe != null;
    }
}
