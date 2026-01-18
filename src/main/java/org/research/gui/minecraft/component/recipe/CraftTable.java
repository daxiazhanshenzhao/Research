package org.research.gui.minecraft.component.recipe;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import org.research.api.recipe.IRecipe;
import org.research.api.recipe.RecipeIngredientRole;
import org.research.gui.minecraft.ResearchContainerScreen;
import org.research.gui.minecraft.component.RecipeTechSlot;

import java.util.List;

public class CraftTable extends RecipeGUIManager<CraftingRecipe> {

    /**
     * 默认构造器（用于旧代码兼容）
     */
    public CraftTable(ResearchContainerScreen screen) {
        super(screen);
    }

    /**
     * 工厂构造器（推荐使用，支持方法引用 CraftTable::new）
     */
    public CraftTable(ResearchContainerScreen screen, CraftingRecipe recipe) {
        super(screen, recipe);
    }

    @Override
    public void setRecipe() {
        List<List<ItemStack>> inputs = recipe.getIngredients().stream()
                .map(ingredient -> List.of(ingredient.getItems()))
                .toList();

        ItemStack output = IRecipe.getResultItem(recipe);

        // 3×3 网格配置
        int gridStartX = getGUILeft() + 30;  // 网格起始 X 坐标
        int gridStartY = getGUITop() + 20;   // 网格起始 Y 坐标
        int slotSize = 18;                    // 每个槽位大小（包含间距）

        // 创建输入槽位（最多 9 个，按 3×3 排列）
        for (int i = 0; i < Math.min(inputs.size(), 9); i++) {
            List<ItemStack> variants = inputs.get(i);

            // 如果该槽位为空，跳过
            if (variants.isEmpty()) {
                continue;
            }

            // 计算网格坐标（row, col）
            int row = i / 3;  // 行号（0-2）
            int col = i % 3;  // 列号（0-2）

            int x = gridStartX + col * slotSize;
            int y = gridStartY + row * slotSize;

            RecipeTechSlot slot = new RecipeTechSlot(x, y, variants, screen);
            this.addWidgets(slot, RecipeIngredientRole.INPUT);
        }

        // 创建输出槽位（放在网格右侧）
        int outputX = gridStartX + 4 * slotSize;  // 右侧留空隙
        int outputY = gridStartY + slotSize;       // 垂直居中（第二行位置）

        RecipeTechSlot outputSlot = new RecipeTechSlot(
            outputX,
            outputY,
            List.of(output),  // 输出只有一个物品
            screen
        );
        this.addWidgets(outputSlot, RecipeIngredientRole.OUTPUT);
    }

    @Override
    public ResourceLocation getBackgroundTexture() {
        return null;
    }


}
