package org.research.api.recipe.category;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import org.research.Research;
import org.research.api.recipe.IRecipe;
import org.research.api.recipe.RecipeIngredientRole;
import org.research.category.RecipeCategory;

import java.util.List;

/**
 * 工作台配方分类
 *
 * 使用示例：
 * <pre>
 * CraftingTableCategory category = new CraftingTableCategory();
 * category.init(craftingRecipe);
 * category.render(context, mouseX, mouseY, partialTick);
 * </pre>
 */
public class CraftingTableCategory extends RecipeCategory<CraftingRecipe> {

    // 工作台配方背景纹理
    private static final ResourceLocation CRAFTING_TABLE_BG =
        Research.asResource("textures/gui/crafting_table_bg.png");

    // 槽位布局：3x3 工作台
    private static final int GRID_START_X = 10;
    private static final int GRID_START_Y = 10;
    private static final int SLOT_SIZE = 18;

    private static final int OUTPUT_X = 90;
    private static final int OUTPUT_Y = 28;

    @Override
    protected ResourceLocation getBackGround() {
        return CRAFTING_TABLE_BG;
    }

    @Override
    protected void setRecipe(RecipeBuilder builder, CraftingRecipe recipe) {
        // 获取配方的原材料
        List<Ingredient> ingredients = recipe.getIngredients();

        // 判断是否为无序配方
        if (isShapeless(recipe)) {
            builder.setShapeless();
        }

        // 计算配方网格大小（1x1, 2x2, 或 3x3）
        int gridWidth = getRecipeWidth(recipe);
        int gridHeight = getRecipeHeight(recipe);

        // 添加输入槽位（最多 3x3）
        int index = 0;
        for (int row = 0; row < Math.min(3, gridHeight); row++) {
            for (int col = 0; col < Math.min(3, gridWidth); col++) {
                if (index < ingredients.size()) {
                    Ingredient ingredient = ingredients.get(index);

                    // 跳过空材料
                    if (!ingredient.isEmpty()) {
                        int x = GRID_START_X + col * SLOT_SIZE;
                        int y = GRID_START_Y + row * SLOT_SIZE;

                        builder.addSlot(x, y, RecipeIngredientRole.INPUT)
                               .addIngredients(ingredient)
                               .build();
                    }
                    index++;
                }
            }
        }

        // 添加输出槽位
        ItemStack output = IRecipe.getResultItem(recipe);
        if (!output.isEmpty()) {
            builder.addSlot(OUTPUT_X, OUTPUT_Y, RecipeIngredientRole.OUTPUT)
                   .addItems(List.of(output))
                   .build();
        }
    }

    /**
     * 判断是否为无序配方
     */
    private boolean isShapeless(CraftingRecipe recipe) {
        // 可以通过配方类名或其他方式判断
        return recipe.getClass().getSimpleName().contains("Shapeless");
    }

    /**
     * 获取配方宽度（列数）
     */
    private int getRecipeWidth(CraftingRecipe recipe) {
        // 如果是有序配方，尝试获取宽度
        // 默认返回 3（最大宽度）
        try {
            var field = recipe.getClass().getDeclaredField("width");
            field.setAccessible(true);
            return (int) field.get(recipe);
        } catch (Exception e) {
            // 无序配方或获取失败，按材料数量估算
            int size = recipe.getIngredients().size();
            if (size <= 1) return 1;
            if (size <= 4) return 2;
            return 3;
        }
    }

    /**
     * 获取配方高度（行数）
     */
    private int getRecipeHeight(CraftingRecipe recipe) {
        try {
            var field = recipe.getClass().getDeclaredField("height");
            field.setAccessible(true);
            return (int) field.get(recipe);
        } catch (Exception e) {
            // 按材料数量估算
            int size = recipe.getIngredients().size();
            int width = getRecipeWidth(recipe);
            return (size + width - 1) / width;
        }
    }
}
