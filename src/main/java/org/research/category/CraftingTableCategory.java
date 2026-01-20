package org.research.category;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import org.research.api.recipe.IRecipe;
import org.research.api.recipe.RecipeIngredientRole;
import org.research.api.recipe.category.RecipeBuilder;
import org.research.api.recipe.category.RecipeCategory;

import java.util.List;

/**
 * 工作台配方分类
 *
 * 布局说明：
 * - 3x3 输入槽位（左侧）
 * - 1x1 输出槽位（右侧）
 * - 支持有���配方（ShapedRecipe）和无序配方（ShapelessRecipe）
 *
 * 使用示例：
 * <pre>
 * CraftingTableCategory category = new CraftingTableCategory();
 * category.init(craftingRecipe);
 * category.render(context, mouseX, mouseY, partialTick);
 * </pre>
 */
public class CraftingTableCategory extends RecipeCategory<CraftingRecipe> {

    // 槽位尺寸和间距
    private static final int SLOT_SIZE = 18; // 每个槽位18x18像素

    // 输入槽位起始位置（3x3网格）
    private static final int INPUT_START_X = 10;
    private static final int INPUT_START_Y = 10;

    // 输出槽位位置
    private static final int OUTPUT_X = 80;
    private static final int OUTPUT_Y = 28;

    @Override
    protected ResourceLocation getBackGround() {
        // 使用默认背景，或者指定自定义纹理
        return Default_Background;
    }

    @Override
    protected void setRecipe(RecipeBuilder builder, CraftingRecipe recipe) {
        // 获取配方的所有材料
        List<Ingredient> ingredients = recipe.getIngredients();

        // 判断是否为有序配方
        boolean isShaped = recipe instanceof ShapedRecipe;

        if (isShaped) {
            // 有序配方：按照 ShapedRecipe 的宽高布局
            ShapedRecipe shapedRecipe = (ShapedRecipe) recipe;
            int width = shapedRecipe.getWidth();
            int height = shapedRecipe.getHeight();

            // 添加输入槽位（按网格布局）
            for (int row = 0; row < height; row++) {
                for (int col = 0; col < width; col++) {
                    int index = row * width + col;
                    if (index < ingredients.size()) {
                        Ingredient ingredient = ingredients.get(index);
                        if (!ingredient.isEmpty()) {
                            int x = INPUT_START_X + col * SLOT_SIZE;
                            int y = INPUT_START_Y + row * SLOT_SIZE;

                            builder.addSlot(x, y, RecipeIngredientRole.INPUT)
                                   .addIngredients(ingredient)
                                   .setSlotBackGround(Default_Slot_Background);
                        }
                    }
                }
            }
        } else {
            // 无序配方：按顺序填充到 3x3 网格
            builder.setShapeless(); // 标记为无序配方

            int index = 0;
            for (Ingredient ingredient : ingredients) {
                if (!ingredient.isEmpty() && index < 9) { // 最多9个槽位
                    int row = index / 3;
                    int col = index % 3;
                    int x = INPUT_START_X + col * SLOT_SIZE;
                    int y = INPUT_START_Y + row * SLOT_SIZE;

                    builder.addSlot(x, y, RecipeIngredientRole.INPUT)
                           .addIngredients(ingredient)
                           .setSlotBackGround(Default_Slot_Background);

                    index++;
                }
            }
        }

        // 添加输出槽位
        ItemStack output = IRecipe.getResultItem(recipe);
        if (!output.isEmpty()) {
            builder.addSlot(OUTPUT_X, OUTPUT_Y, RecipeIngredientRole.OUTPUT)
                   .addItems(List.of(output))
                   .setSlotBackGround(Default_Slot_Background);
        }
    }

    @Override
    public RecipeType<?> getRecipeType() {
        return RecipeType.CRAFTING;
    }

}
