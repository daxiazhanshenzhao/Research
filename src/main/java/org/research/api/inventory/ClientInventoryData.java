package org.research.api.inventory;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * 客户端背包数据管理器
 * 用于跟踪玩家背包中的物品数量，以便在配方显示时显示 "物品名 n/m" 格式
 * 其中 n 是玩家拥有的数量，m 是配方需要的数量
 */
public class ClientInventoryData {

    private final Minecraft mc = Minecraft.getInstance();

    /** 物品名称到数量的映射（缓存玩家背包中的物品数量）*/
    private final Map<String, Integer> itemCounts = new HashMap<>();

    /**
     * 更新背包数据
     * 扫描玩家背包的所有槽位，统计每种物品的数量
     */
    public void updateInventoryData() {
        itemCounts.clear();

        if (mc.player == null) {
            return;
        }

        Inventory inventory = mc.player.getInventory();

        // 遍历玩家背包的所有槽位（包括主背包、快捷栏、装备栏、副手）
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty()) {
                String itemName = stack.getHoverName().getString();
                int count = stack.getCount();

                // 合并相同物品的数量
                itemCounts.merge(itemName, count, Integer::sum);
            }
        }
    }

    /**
     * 获取玩家背包中指定物品的数量
     * @param itemName 物品名称
     * @return 物品数量，如果没有则返回 0
     */
    public int getItemCount(String itemName) {
        return itemCounts.getOrDefault(itemName, 0);
    }

    /**
     * 检查玩家是否拥有足够的物品
     * @param itemName 物品名称
     * @param requiredCount 需要的数量
     * @return 如果拥有足够的数量返回 true，否则返回 false
     */
    public boolean hasEnoughItems(String itemName, int requiredCount) {
        return getItemCount(itemName) >= requiredCount;
    }

    /**
     * 清除所有缓存数据
     */
    public void clearCache() {
        itemCounts.clear();
    }
}
