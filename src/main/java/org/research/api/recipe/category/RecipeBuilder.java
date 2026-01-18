package org.research.api.recipe.category;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.research.api.recipe.RecipeIngredientRole;

import java.util.ArrayList;
import java.util.List;

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

    private final List<SlotBuilder> slots = new ArrayList<>();
    private boolean shapeless = false;

    /**
     * 添加一个槽位并返回 SlotBuilder 用于链式配置
     * @param x 槽位 X 坐标
     * @param y 槽位 Y 坐标
     * @param role 槽位角色（INPUT/OUTPUT/CATALYST）
     * @return SlotBuilder 用于配置槽位细节
     */
    public SlotBuilder addSlot(int x, int y, RecipeIngredientRole role){
        SlotBuilder slotBuilder = new SlotBuilder(this, x, y, role);
        slots.add(slotBuilder);
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

    /**
     * 获取所有槽位构建器
     * @return 槽位构建器列表
     */
    public List<SlotBuilder> getSlots() {
        return slots;
    }

    /**
     * 是否为无序配方
     */
    public boolean isShapeless() {
        return shapeless;
    }


}
