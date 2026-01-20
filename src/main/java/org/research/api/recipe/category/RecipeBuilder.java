package org.research.api.recipe.category;

import lombok.Getter;
import org.research.api.recipe.RecipeIngredientRole;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 配方构建器，用于在 RecipeCategory 中配置配方的槽位布局
 *
 * 使用方式：
 * <pre>
 * builder.addSlot(x, y, RecipeIngredientRole.INPUT)
 *        .addIngredients(ingredient)
 *        .setSlotBackGround(texture);
 * </pre>
 */
public class RecipeBuilder {


    @Getter
    private final List<SlotBuilder> slots = new ArrayList<>();
    private final Map<SlotKey, SlotBuilder> slotCache = new HashMap<>();

    @Getter
    private boolean shapeless = false;


    private static class SlotKey {
        private final int x;
        private final int y;
        private final RecipeIngredientRole role;
        private final int hash; // 预计算的哈希值

        public SlotKey(int x, int y, RecipeIngredientRole role) {
            this.x = x;
            this.y = y;
            this.role = role;
            // 在构造时立即计算并缓存哈希值，只计算一次
            this.hash = Objects.hash(x, y, role);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SlotKey slotKey = (SlotKey) o;
            // 先比较哈希值（快速失败），再比较具体字段
            return hash == slotKey.hash
                && x == slotKey.x
                && y == slotKey.y
                && role == slotKey.role;
        }

        @Override
        public int hashCode() {
            // 直接返回预计算的哈希值，O(1) 时间复杂度
            return hash;
        }
    }

    /**
     * 添加一个槽位并返回 SlotBuilder 用于链式配置
     * 如果已存在相同位置和角色的槽位，则返回已存在的槽位而不是创建新实例
     * 使用哈希缓存实现 O(1) 时间复杂度的重复检测
     * @param x 槽位 X 坐标
     * @param y 槽位 Y 坐标
     * @param role 槽位角色（INPUT/OUTPUT/CATALYST）
     * @return SlotBuilder 用于配置槽位细节
     */
    public SlotBuilder addSlot(int x, int y, RecipeIngredientRole role){
        // 使用哈希键快速查找，O(1) 时间复杂度
        SlotKey key = new SlotKey(x, y, role);
        SlotBuilder existingSlot = slotCache.get(key);

        if (existingSlot != null) {
            return existingSlot;
        }

        // 创建新槽位并同时更新列表和缓存
        SlotBuilder slotBuilder = new SlotBuilder(this, x, y, role);
        slots.add(slotBuilder);
        slotCache.put(key, slotBuilder);
        return slotBuilder;
    }


    /**
     * 标记配方为无序配方（如合成台无序配方）
     * @return this，支持链式调用
     */
    public RecipeBuilder setShapeless(){
        this.shapeless = true;
        return this;
    }


}
