package org.research.api.recipe.category;


import lombok.Getter;
import lombok.Setter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.research.api.recipe.RecipeIngredientRole;
import org.research.api.util.BlitContext;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
public class SlotBuilder {

    private final int x;
    private final int y;
    private final RecipeIngredientRole role;
    private final List<List<ItemStack>> ingredients = new ArrayList<>();
    private ResourceLocation slotBackground;
    private ResourceLocation slotOverlay;
    private boolean renderable = true;

    public SlotBuilder(int x, int y, RecipeIngredientRole role) {
        this.x = x;
        this.y = y;
        this.role = role;
    }

    /**
     * 添加一个 Ingredient（可能包含多个可选的 ItemStack）
     * @param ingredient 配方材料
     * @return this，支持链式调用
     */
    public SlotBuilder addIngredients(Ingredient ingredient){
        this.ingredients.add(Arrays.asList(ingredient.getItems()));
        return this;
    }

    /**
     * 直接添加 ItemStack 列表
     * @param items 物品列表
     * @return this，支持链式调用
     */
    public SlotBuilder addItems(List<ItemStack> items){
        this.ingredients.add(items);
        return this;
    }

    /**
     * 设置槽位背景纹理
     * @param texture 背景纹理资源位置
     * @return this，支持链式调用
     */
    public SlotBuilder setSlotBackGround(ResourceLocation texture) {
        this.slotBackground = texture;
        return this;
    }

    /**
     * 设置槽位叠加层纹理（如箭头等）
     * @param texture 叠加层纹理资源位置
     * @return this，支持链式调用
     */
    public SlotBuilder setSlotOverlay(ResourceLocation texture) {
        this.slotOverlay = texture;
        return this;
    }

    public SlotBuilder setRenderable(boolean renderable) {
        this.renderable = renderable;
        return this;
    }




}
