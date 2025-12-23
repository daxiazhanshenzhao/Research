package org.research.api.tech;

import com.alessandro.astages.util.ARestrictionType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import org.research.api.event.custom.ChangeTechStageEvent;

import java.util.List;
import java.util.Objects;

public class TechInstance{

    private AbstractTech tech;
    private int stateValue;          //TechStage enum
    private ServerPlayer serverPlayer;

    private boolean active; //用于container，false就默认不会显示和参与初始化
    private boolean focused;

    public TechInstance(AbstractTech tech, ServerPlayer serverPlayer) {
        this.tech = tech;
        this.stateValue = TechState.LOCKED.getValue();
        this.serverPlayer = serverPlayer;
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

    //    public List<ResourceLocation> getChildren() {
//        return tech.getTechBuilder().child;
//    }


    public boolean isFocused() {
        return focused;
    }

    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    public boolean isActive() {
        return active;
    }

    public int getStateValue() {
        return stateValue;
    }

    public ARestrictionType getARestrictionType() {
        return tech.getTechBuilder().restriction;
    }
}
