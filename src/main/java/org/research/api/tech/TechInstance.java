package org.research.api.tech;

import com.alessandro.astages.util.ARestrictionType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import org.research.Research;
import org.research.api.event.custom.ChangeTechStageEvent;
import org.research.api.init.TechInit;
import org.research.api.recipe.RecipeWrapper;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

import static org.research.api.tech.capability.TechTreeDataProvider.ID;

public class TechInstance implements Comparable<TechInstance> {

    public static final String ID = "tech_id";
    public static final String STATE = "tech_state";
    public static final String FOCUS = "tech_focus";
    public static final TechInstance EMPTY = new TechInstance(TechInit.EMPTY,null);

    private AbstractTech tech;

    private ServerPlayer serverPlayer = null;


    /**
     * 查看玩家的点击状态。
     */
    private boolean focused = false;
    private int stateValue = 0;          //TechStage enum


    public TechInstance(AbstractTech tech, ServerPlayer serverPlayer) {
        this.tech = tech;
        this.stateValue = TechState.LOCKED.getValue();
        this.serverPlayer = serverPlayer;
    }

    public TechInstance(ResourceLocation resourceLocation, Integer stageValue, Boolean focused) {
        this.tech = TechInit.getTech(resourceLocation);
        this.stateValue = stageValue;
        this.focused = focused;
    }

    public void setTechState(TechState state) {

        ChangeTechStageEvent event = new ChangeTechStageEvent(getState(),state,this, serverPlayer);
        if (this.serverPlayer == null || !MinecraftForge.EVENT_BUS.post(event)) {
            this.stateValue = event.getNewState().getValue();
        }
        if (this.serverPlayer != null ){
            this.stateValue = event.getNewState().getValue();
        }

    }

    public TechState getState() {
        return getState(stateValue);
    }
    public TechState getState(int stateValue) {
        for (TechState type : TechState.values()) {
            if (type.getValue() == stateValue) {
                return type;
            }
        }
        return TechState.LOCKED;
    }

    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    public AbstractTech getTech() {
        return tech;
    }

    public ServerPlayer getServerPlayer() {
        return serverPlayer;
    }

    public List<ResourceLocation> getParents() {
        return tech.getTechBuilder().parent;
    }

    public ResourceLocation getIdentifier() {
        return tech.getIdentifier();
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TechInstance instance = (TechInstance) o;
        return Objects.equals(tech, instance.tech);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(tech);
    }
    @Override
    public int compareTo(TechInstance other) {
        if (this == other) return 0;
        if (other == null) return 1;
        
        // 比较 tech 字段
        if (this.tech != other.tech) {
            if (this.tech == null) return -1;
            if (other.tech == null) return 1;

            ResourceLocation id1 = this.tech.getIdentifier();
            ResourceLocation id2 = other.tech.getIdentifier();
            if (id1 != id2) {
                if (id1 == null) return -1;
                if (id2 == null) return 1;
                int techCompare = id1.compareTo(id2);
                if (techCompare != 0) return techCompare;
            }
        }
        
        // 比较 serverPlayer 字段
        if (this.serverPlayer != other.serverPlayer) {
            if (this.serverPlayer == null) return -1;
            if (other.serverPlayer == null) return 1;
            int playerCompare = this.serverPlayer.getUUID().compareTo(other.serverPlayer.getUUID());
            if (playerCompare != 0) return playerCompare;
        }
        
        // 比较 stateValue 字段
        int stateCompare = Integer.compare(this.stateValue, other.stateValue);
        if (stateCompare != 0) return stateCompare;
        
        // 比较 focused 字段
        return Boolean.compare(this.focused, other.focused);
    }

    //    public List<ResourceLocation> getChildren() {
//        return tech.getTechBuilder().child;
//    }













    public RecipeWrapper getRecipe() {
        return getTech().getTechBuilder().recipe;
    }


    public boolean isFocused() {
        return focused;
    }

    public int getStateValue() {
        return stateValue;
    }

//    public ARestrictionType getARestrictionType() {
//        return tech.getTechBuilder().restriction;
//    }

    public static final Codec<TechInstance> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            ResourceLocation.CODEC.fieldOf(ID).forGetter(TechInstance::getIdentifier),
            Codec.INT.fieldOf(STATE).forGetter(TechInstance::getStateValue),
                Codec.BOOL.fieldOf(FOCUS).forGetter(TechInstance::isFocused)
        ).apply(instance,TechInstance::new));


}
