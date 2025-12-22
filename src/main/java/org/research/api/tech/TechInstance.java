package org.research.api.tech;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import org.research.api.event.custom.ChangeTechStageEvent;

import java.util.List;

public class TechInstance {

    private AbstractTech tech;
    private TechState state;
    private ServerPlayer serverPlayer;
    public TechInstance(AbstractTech tech, ServerPlayer serverPlayer) {
        this.tech = tech;
        this.state = TechState.LOCKED;
        this.serverPlayer = serverPlayer;
    }

    public void setTechState(TechState state) {

        ChangeTechStageEvent event = new ChangeTechStageEvent(this.state,state,this, serverPlayer);
        if (this.serverPlayer == null || !MinecraftForge.EVENT_BUS.post(event)) {
            this.state = event.getNewState();
        }
        if (this.serverPlayer != null ){
            this.state = event.getNewState();
        }

    }

    public TechState getState() {
        return state;
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

    public List<ResourceLocation> getChildren() {
        return tech.getTechBuilder().child;
    }



}
