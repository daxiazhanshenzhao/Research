package org.research.api.event.custom;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Event;
import org.research.api.tech.TechInstance;
import org.research.api.tech.TechState;



public class ChangeTechStageEvent extends PlayerEvent {

    public ChangeTechStageEvent(TechState oldState, TechState newState, TechInstance techInstance, ServerPlayer player) {
        super(player);
        this.oldState = oldState;
        this.newState = newState;
        this.techInstance = techInstance;
    }

    private final TechInstance techInstance;
    private final TechState oldState;
    private TechState newState;

    public TechInstance getTechInstance() {
        return techInstance;
    }

    public TechState getNewState() {
        return newState;
    }

    public TechState getOldState() {
        return oldState;
    }

    public void setNewState(TechState newState) {
        this.newState = newState;
    }


}
