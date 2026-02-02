package org.research.api.recipe;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import org.research.Research;
import org.research.api.client.ClientResearchData;
import org.research.api.recipe.category.RecipeBuilder;
import org.research.api.recipe.category.RecipeCategory;
import org.research.api.recipe.category.SlotBuilder;
import org.research.api.util.BlitContext;
import org.research.gui.minecraft.ResearchContainerScreen;
import org.research.gui.minecraft.component.RecipeTechSlot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 配方槽位连接器，负责预先创建和管理所有配方的槽位组件
 *
 * 工作流程：
 * 1. 配方加载阶段：预先创建所有 RecipeTechSlot 实例（坐标为0,0）
 * 2. Screen 初始化时：调用 initializeScreen() 更新所有槽位的坐标
 * 3. 切换配方时：直接使用缓存的槽位，只需更新屏幕显示
 */
public class WightConnection {

    private ResearchContainerScreen screen;
    private RecipeCategory<?> currentCategory;

    /**
     * 缓存：RecipeType -> 预先创建的 RecipeTechSlot 列表
     * 在配方加载阶段填充，避免运行时重复创建实例
     */
    private final Map<RecipeType<?>, List<RecipeTechSlot>> slotCache = new HashMap<>();

    /**
     * 标记 Screen 是否已经初始化（坐标是否已经设置）
     */
    private boolean screenInitialized = false;




    /**
     * 设置 Screen 并触发槽位预加载
     * @param screen ResearchContainerScreen 实例
     */
    public void setScreen(ResearchContainerScreen screen) {
        if (this.screen != null) {
            // 如果已经设置过 Screen，需要清除旧的初始化状态
            this.screenInitialized = false;
        }
        this.screen = screen;
        // 清空缓存并重新预加载
        slotCache.clear();
        preloadAllSlots();
    }

    /**
     * 预加载所有配方类别的槽位
     * 在构造函数中调用，创建所有槽位实例但坐标为 0,0
     */
    private void preloadAllSlots() {
        if (screen == null) {
            return; // Screen 未设置，跳过预加载
        }

        var categories = ClientResearchData.getRecipeCategories().getRecipeCategories();
        for (RecipeCategory<?> category : categories) {
            // 检查 RecipeBuilder 是否已初始化
            if (category.getRecipeBuilder() == null) {
                // RecipeBuilder 未初始化，跳过这个类别
                // 槽位将在第一次调用 setRecipe() 时创建
                continue;
            }

            List<RecipeTechSlot> slots = createSlotsForCategory(category);
            slotCache.put(category.getRecipeType(), slots);
        }
    }

    /**
     * 为指定配方类别创建槽位列表
     * @param category 配方类别
     * @return 槽位列表（坐标为 0,0）
     */
    private List<RecipeTechSlot> createSlotsForCategory(RecipeCategory<?> category) {
        RecipeBuilder recipeBuilder = category.getRecipeBuilder();
        if (recipeBuilder == null) {
            // RecipeBuilder 未初始化，返回空列表
            return new ArrayList<>();
        }

        List<SlotBuilder> slotBuilders = recipeBuilder.getSlots();
        List<RecipeTechSlot> slots = new ArrayList<>();

        for (SlotBuilder slotBuilder : slotBuilders) {
            // 提取物品列表
            List<ItemStack> items = slotBuilder.getIngredients().isEmpty()
                ? new ArrayList<>()
                : slotBuilder.getIngredients().get(0);

            // 创建槽位（坐标暂时为 0,0，稍后由 initializeScreen 更新）
            RecipeTechSlot slot = new RecipeTechSlot(0, 0, items, screen);
            slots.add(slot);
        }

        return slots;
    }

    /**
     * 初始化 Screen 坐标
     * 必须在 Screen.init() 之后调用，用于更新所有预创建槽位的坐标
     */
    public void initializeScreen() {
        if (screen == null) {
            Research.LOGGER.warn("initializeScreen() 跳过：screen 为 null");
            return; // Screen 未设置，跳过初始化
        }

        if (screenInitialized) {
            Research.LOGGER.info("initializeScreen() 跳过：已经初始化过");
            return; // 避免重复初始化
        }

        Research.LOGGER.info("开始初始化 Screen 坐标...");

        // 计算配方书区域的屏幕坐标
        var recipeContext = BlitContext.of(null, 8, 21, 124, 221);
        int guiLeft = (screen.width - 256) / 2;
        int guiTop = (screen.height - 256) / 2;
        int recipeBookX = guiLeft + recipeContext.u();
        int recipeBookY = guiTop + recipeContext.v();

        Research.LOGGER.info("配方书区域坐标：guiLeft={}, guiTop={}, recipeBookX={}, recipeBookY={}",
            guiLeft, guiTop, recipeBookX, recipeBookY);

        // 更新所有缓存槽位的坐标
        var categories = ClientResearchData.getRecipeCategories().getRecipeCategories();
        int totalSlots = 0;
        for (RecipeCategory<?> category : categories) {
            List<RecipeTechSlot> slots = slotCache.get(category.getRecipeType());
            if (slots == null) {
                Research.LOGGER.debug("配方类型 {} 的槽位缓存为空", category.getRecipeType());
                continue;

            }

            RecipeBuilder recipeBuilder = category.getRecipeBuilder();
            if (recipeBuilder == null) {
                Research.LOGGER.warn("配方类型 {} 的 RecipeBuilder 为 null", category.getRecipeType());
                continue;
            }

            List<SlotBuilder> slotBuilders = recipeBuilder.getSlots();
            for (int i = 0; i < slots.size() && i < slotBuilders.size(); i++) {
                RecipeTechSlot slot = slots.get(i);
                SlotBuilder builder = slotBuilders.get(i);

                // 计算并设置槽位的屏幕坐标
                int screenX = recipeBookX + builder.getX();
                int screenY = recipeBookY + builder.getY();
                slot.setPosition(screenX, screenY);
                totalSlots++;
            }
        }

        Research.LOGGER.info("Screen 坐标初始化完成，共更新 {} 个槽位", totalSlots);
        screenInitialized = true;
    }

    /**
     * 刷新当前配方的槽位显示
     * 用于 Screen 重新打开时恢复上次的配方显示
     */
    public void refreshCurrentRecipe() {
        if (currentCategory != null) {
            Research.LOGGER.info("刷新当前配方显示，配方类型：{}", currentCategory.getRecipeType());
            updateWight();
        } else {
            Research.LOGGER.warn("无法刷新配方显示：currentCategory 为 null");
        }
    }

    /**
     * 切换显示的配方
     * 直接从缓存中获取预创建的槽位，无需重复创建
     * 如果缓存中不存在，则动态创建槽位
     */
    public void setRecipe(RecipeWrapper recipeWrapper) {
        var categories = ClientResearchData.getRecipeCategories().getRecipeCategories();
        for (RecipeCategory<?> category : categories) {
            if (category.getRecipeType() == recipeWrapper.type()) {
                this.currentCategory = category;

                // 检查缓存中是否已有该配方类型的槽位
                if (!slotCache.containsKey(category.getRecipeType()) || slotCache.get(category.getRecipeType()).isEmpty()) {
                    // 缓存中没有，需要动态创建
                    // 注意：这里需要先初始化 RecipeBuilder
                    if (category.getRecipeBuilder() == null && screen != null) {
                        // RecipeBuilder 未初始化，需要获取具体配方并初始化
                        // 从客户端获取配方实例
                        var recipe = IRecipe.getClientRecipe(recipeWrapper, net.minecraft.client.Minecraft.getInstance());
                        if (recipe != null) {
                            // 使用原始类型调用，因为我们无法在运行时确定泛型类型
                            initCategoryWithRecipe(category, recipe);

                            // 创建槽位
                            List<RecipeTechSlot> slots = createSlotsForCategory(category);
                            slotCache.put(category.getRecipeType(), slots);

                            // 如果 Screen 已初始化，立即更新坐标
                            if (screenInitialized) {
                                updateSlotPositions(category.getRecipeType());
                            }
                        }
                    }
                }

                updateWight();
                break;
            }
        }
    }

    /**
     * 更新指定配方类型的槽位坐标
     */
    private void updateSlotPositions(RecipeType<?> recipeType) {
        if (screen == null) return;

        List<RecipeTechSlot> slots = slotCache.get(recipeType);
        if (slots == null || slots.isEmpty()) return;

        var categories = ClientResearchData.getRecipeCategories().getRecipeCategories();
        for (RecipeCategory<?> category : categories) {
            if (category.getRecipeType() == recipeType && category.getRecipeBuilder() != null) {
                List<SlotBuilder> slotBuilders = category.getRecipeBuilder().getSlots();

                // 计算配方书区域的屏幕坐标
                var recipeContext = BlitContext.of(null, 8, 21, 124, 221);
                int guiLeft = (screen.width - 256) / 2;
                int guiTop = (screen.height - 256) / 2;
                int recipeBookX = guiLeft + recipeContext.u();
                int recipeBookY = guiTop + recipeContext.v();

                for (int i = 0; i < slots.size() && i < slotBuilders.size(); i++) {
                    RecipeTechSlot slot = slots.get(i);
                    SlotBuilder builder = slotBuilders.get(i);

                    int screenX = recipeBookX + builder.getX();
                    int screenY = recipeBookY + builder.getY();
                    slot.setPosition(screenX, screenY);
                }
                break;
            }
        }
    }

    /**
     * 更新屏幕上显示的槽位
     * 从缓存中获取槽位，避免重复创建
     */
    private void updateWight() {

    }

    public void render(GuiGraphics guiGraphics) {
        // 渲染逻辑（如果需要）
    }

    /**
     * 使用原始类型安全地初始化配方类别
     * 抑制未检查的警告，因为我们通过 RecipeType 确保了类型匹配
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void initCategoryWithRecipe(RecipeCategory category, net.minecraft.world.item.crafting.Recipe recipe) {
        category.init(recipe);
    }
}
