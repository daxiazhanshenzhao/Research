package org.research.api.recipe.category;

import lombok.Getter;
import lombok.Setter;
import org.research.api.recipe.RecipeIngredientRole;
import org.research.gui.minecraft.component.RecipeTechSlot;

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
@Getter
@Setter
public class RecipeBuilder {

    private Map<Integer, SlotBuilder> builderSlots = new HashMap<>();

    private boolean shapeless = false;





    public SlotBuilder addSlot(int id, int x, int y, RecipeIngredientRole role){
        this.builderSlots.put(id, new SlotBuilder(x, y, role));
        return this.builderSlots.get(id);
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
