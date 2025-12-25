package org.research.api.event.custom;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Cancelable;

@Cancelable
public class ChangeTechFocused extends PlayerEvent {
    public ChangeTechFocused(Player player) {
        super(player);
    }

    private boolean focused;

    public boolean isFocused() {
        return focused;
    }

    public void setFocused(boolean focused) {
        this.focused = focused;
    }
}
