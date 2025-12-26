package org.research.api.event.custom;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class CompleteTechEvent extends PlayerEvent {
    public CompleteTechEvent(Player player) {
        super(player);
    }


}
