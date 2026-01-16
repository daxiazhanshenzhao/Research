package org.research.api.event.custom;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class InventoryChangeEvent extends PlayerEvent {

    private final ItemStack item;
    private final int slot;

    public InventoryChangeEvent(Player player, ItemStack item, int slot) {
        super(player);
        this.item = item;
        if (slot >= 36 && slot <= 44) {
            this.slot = slot - 36;
        } else if (slot == 45) {
            this.slot = 40;
        } else if (slot == 5) {
            this.slot = 39;
        } else if (slot == 6) {
            this.slot = 38;
        } else if (slot == 7) {
            this.slot = 37;
        } else if (slot == 8) {
            this.slot = 36;
        } else {
            this.slot = slot;
        }
    }


    public ItemStack getItem() {
        return this.item;
    }

    public int getSlot() {
        return this.slot;
    }


}
