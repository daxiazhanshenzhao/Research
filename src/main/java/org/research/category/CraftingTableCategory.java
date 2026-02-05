package org.research.category;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import org.research.Research;
import org.research.api.recipe.IRecipe;
import org.research.api.recipe.RecipeIngredientRole;
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



    @Override
    protected ResourceLocation getBackGround() {
        // 使用默认背景，或者指定自定义纹理
        return Research.asResource("textures/gui/recipe/crafting_table.png");
    }

    @Override
    public void setRecipe(CraftingRecipe recipe) {
        // 清空之前的槽位配置
        builder.getBuilderSlots().clear();

        // 槽位布局常量
        final int INPUT_START_X = 4 ;  // 输入槽位起始 X 坐标
        final int INPUT_START_Y = 4;  // 输入槽位起始 Y 坐标
        final int SLOT_SPACING = 21;   // 槽位间距
        final int OUTPUT_X = 92;       // 输出槽位 X 坐标
        final int OUTPUT_Y = 25;       // 输出槽位 Y 坐标（居中对齐 3x3 网格）

        // 获取配方的材料列表
        List<Ingredient> ingredients = recipe.getIngredients();

        // 处理有序配方（ShapedRecipe）的特殊布局
        if (recipe instanceof ShapedRecipe shapedRecipe) {
            int width = shapedRecipe.getWidth();
            int height = shapedRecipe.getHeight();

            // 配置输入槽位（按照配方的形状布局）
            int slotId = 0;
            for (int row = 0; row < height; row++) {
                for (int col = 0; col < width; col++) {
                    int ingredientIndex = row * width + col;
                    if (ingredientIndex < ingredients.size()) {
                        Ingredient ingredient = ingredients.get(ingredientIndex);

                        int x = INPUT_START_X + col * SLOT_SPACING;
                        int y = INPUT_START_Y + row * SLOT_SPACING;

                        builder.addSlot(slotId++, x, y, RecipeIngredientRole.INPUT)
                                .addIngredients(ingredient)
                                .setSlotBackGround(Default_Slot_Background);
                    }
                }
            }
        } else {
            // 无序配方（ShapelessRecipe）：按顺序填充 3x3 网格
            builder.setShapeless();

            int slotId = 0;
            for (int i = 0; i < Math.min(ingredients.size(), 9); i++) {
                Ingredient ingredient = ingredients.get(i);

                int row = i / 3;
                int col = i % 3;
                int x = INPUT_START_X + col * SLOT_SPACING;
                int y = INPUT_START_Y + row * SLOT_SPACING;

                builder.addSlot(slotId++, x, y, RecipeIngredientRole.INPUT)
                        .addIngredients(ingredient)
                        .setSlotBackGround(Default_Slot_Background);
            }
        }

        // 配置输出槽位
        ItemStack resultItem = IRecipe.getResultItem(recipe);
        builder.addSlot(100, OUTPUT_X, OUTPUT_Y, RecipeIngredientRole.OUTPUT)
                .addItems(List.of(resultItem))
                .setSlotBackGround(Default_Slot_Background);
    }



    @Override
    public RecipeType<?> getRecipeType() {
        return RecipeType.CRAFTING;
    }

}
