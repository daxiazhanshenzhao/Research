package org.research.api.event.custom;

import lombok.Getter;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;

/**
 * 背包变化事件
 * 双端事件
 * 当玩家背包中的物品发生变化时触发
 */
@Getter
public class InventoryChangeEvent extends PlayerEvent {

    /** 变化的物品 */
    private final ItemStack item;

    /** 变化的槽位索引 */
    private final int slot;

    /**
     * 构造函数
     * @param player 玩家
     * @param item 变化的物品
     * @param slot 变化的槽位索引
     */
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

}
