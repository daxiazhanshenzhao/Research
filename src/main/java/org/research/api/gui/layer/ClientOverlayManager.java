package org.research.api.gui.layer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import org.research.api.client.ClientResearchData;
import org.research.api.config.ClientConfig;
import org.research.api.recipe.IRecipe;
import org.research.api.recipe.RecipeIngredientRole;
import org.research.api.recipe.category.RecipeCategory;
import org.research.api.recipe.category.SlotBuilder;
import org.research.api.tech.TechInstance;
import org.research.api.util.BlitContextV2;
import org.research.api.util.OverlayContext;
import org.research.api.util.Vec2i;

import java.util.*;

/**
 * 客户端覆盖层管理器
 * 负责管理和处理科技配方的 GUI 渲染计算
 * 所有坐标计算、尺寸计算、文本准备都在此类中进行，便于复用
 */
public class ClientOverlayManager {

    // ==================== 常量定义 ====================

    /** 文本颜色 - 棕色 */
    private static final int TEXT_COLOR = 0x8B4513;


    /** 行高 */
    private static final int LINE_HEIGHT = 10;

    /** 起始区域高度（用于标题） */
    private static final int START_HEIGHT = 36;

    /** 结束区域高度 */
    private static final int END_HEIGHT = 14;

    /** 最小中心高度 */
    private static final int MIN_CENTER_HEIGHT = 0;

    /** 输出区域的最大空白面积（相对于 Overlay 的坐标）*/
    public static final BlitContextV2 outputUV = BlitContextV2.of(OverlayContext.OVERLAY_CONTEXT, 6, 10, 45, 14, 256, 256);

    /** 输入区域的左上角坐标（相对于 Overlay 的坐标）*/
    public static final Vec2i inputStart = new Vec2i(6, 31);

    /** 输出区域内的文本左边距 */
    private static final int OUTPUT_TEXT_PADDING_LEFT = 2;

    /** 输出区域内的文本上边距 */
    private static final int OUTPUT_TEXT_PADDING_TOP = 2;

    /** 输入区域内的文本左边距（箭头后的偏移）*/
    private static final int INPUT_TEXT_PADDING_LEFT = 2;
    // ==================== 缓存数据 ====================

    /** 缓存的配方显示数据 */
    private RecipeDisplayData cachedRecipeData;

    /** 缓存的中心高度 */
    private int cachedCenterHeight = -1;

    // ==================== 缓存管理方法 ====================

    /**
     * 清除所有缓存数据
     * 当服务端数据更新时调用，确保 Overlay 显示最新数据
     */
    public void clearCache() {
        cachedRecipeData = null;
        cachedCenterHeight = -1;
    }

    // ==================== 坐标计算方法 ====================

    /**
     * 计算 Overlay 左侧 X 坐标
     * @param width 屏幕宽度
     * @return 左侧 X 坐标
     */
    public int getOverlayLeft(int width) {
        return width - OverlayContext.OPEN_START.width() + ClientConfig.getOverlayXOffset();
    }

    /**
     * 计算 Overlay 顶部 Y 坐标
     * @param height 屏幕高度
     * @return 顶部 Y 坐标
     */
    public int getOverlayTop(int height) {
        return height / 2 + ClientConfig.getOverlayYOffset();
    }

    // ==================== 高度计算方法 ====================

    /**
     * 获取中心区域需要的高度
     * 根据输出和输入物品数量动态计算
     * 输出区域在 outputUV 定义的区域内，输入区域从 inputStart 开始
     * @return 中心区域高度（像素）
     */
    public int getCenterHeight() {
        if (cachedCenterHeight != -1) {
            return cachedCenterHeight;
        }

        RecipeDisplayData data = getRecipeDisplayData();
        if (data == null) {
            cachedCenterHeight = MIN_CENTER_HEIGHT;
            return cachedCenterHeight;
        }

        int outputLines = data.outputs.size();
        int inputLines = data.inputs.size();


        // 计算输入区域需要的高度（从 inputStart.y 开始）
        int inputHeight = inputLines * LINE_HEIGHT;

        // 输入区域的结束位置（相对于 Overlay 顶部）
        int inputEndY = inputStart.y + inputHeight;

        // 中心区域高度 = 输入区域结束位置 - START_HEIGHT（起始区域高度）
        // 因为中心区域是在起始区域和结束区域之间
        cachedCenterHeight = Math.max(MIN_CENTER_HEIGHT, inputEndY - START_HEIGHT);

        return cachedCenterHeight;
    }

    // ==================== 文本渲染相关方法 ====================

    /**
     * 获取字体对象
     * @return Minecraft 字体
     */
    public Font getFont() {
        return Minecraft.getInstance().font;
    }

    /**
     * 获取文本颜色
     * @return 文本颜色值
     */
    public int getTextColor() {
        return TEXT_COLOR;
    }

    /**
     * 获取输出物品列表
     * @return 输出物品显示列表，如果没有则返回空列表
     */
    public List<ItemDisplay> getOutputItems() {
        RecipeDisplayData data = getRecipeDisplayData();
        return data != null ? data.outputs : Collections.emptyList();
    }

    /**
     * 获取输入物品列表
     * @return 输入物品显示列表，如果没有则返回空列表
     */
    public List<ItemDisplay> getInputItems() {
        RecipeDisplayData data = getRecipeDisplayData();
        return data != null ? data.inputs : Collections.emptyList();
    }

    /**
     * 获取输出物品的 X 坐标
     * 使用 outputUV 定义的区域，加上内部文本边距
     * @param guiLeft Overlay 左侧坐标
     * @param index 物品索引（从 0 开始）
     * @return 物品 X 坐标
     */
    public int getOutputItemX(int guiLeft, int index) {
        return guiLeft + outputUV.u() + OUTPUT_TEXT_PADDING_LEFT;
    }

    /**
     * 获取输出物品的 Y 坐标
     * 使用 outputUV 定义的区域，从顶部开始排列
     * @param guiTop Overlay 顶部坐标
     * @param index 物品索引（从 0 开始）
     * @return 物品 Y 坐标
     */
    public int getOutputItemY(int guiTop, int index) {
        return guiTop + outputUV.v() + OUTPUT_TEXT_PADDING_TOP + index * LINE_HEIGHT;
    }

    /**
     * 获取输入物品的 X 坐标（箭头位置）
     * 使用 inputStart 定义的起始位置
     * @param guiLeft Overlay 左侧坐标
     * @param index 物品索引（从 0 开始）
     * @return 箭头 X 坐标
     */
    public int getInputItemX(int guiLeft, int index) {
        return guiLeft + inputStart.x;
    }

    /**
     * 获取输入物品的 Y 坐标
     * 使用 inputStart 定义的起始位置，从上往下排列
     * @param guiTop Overlay 顶部坐标
     * @param index 物品索引（从 0 开始）
     * @return 物品 Y 坐标
     */
    public int getInputItemY(int guiTop, int index) {
        return guiTop + inputStart.y + index * LINE_HEIGHT;
    }

    // ==================== 数据更新方法 ====================

    /**
     * 刷新缓存数据
     * 当配方数据发生变化时调用此方法
     */
    public void refreshCache() {
        cachedRecipeData = null;
        cachedCenterHeight = -1;
    }

    // ==================== 内部数据类 ====================

    /**
     * 配方显示数据
     * 包含输出和输入物品列表
     */
    public static class RecipeDisplayData {
        /** 输出物品列表 */
        public final List<ItemDisplay> outputs = new ArrayList<>();

        /** 输入物品列表 */
        public final List<ItemDisplay> inputs = new ArrayList<>();
    }

    /**
     * 物品显示数据
     * 包含物品名称和数量
     */
    public static class ItemDisplay {
        /** 物品名称 */
        public final String name;

        /** 物品数量 */
        public int count;

        /**
         * 构造函数
         * @param name 物品名称
         * @param count 物品数量
         */
        public ItemDisplay(String name, int count) {
            this.name = name;
            this.count = count;
        }

        /**
         * 获取显示文本（名称 x 数量）
         * @return 格式化的显示文本
         */
        public String getDisplayText() {
            if (count > 1) {
                return name + " x" + count;
            }
            return name;
        }
    }

    // ==================== 配方数据获取方法 ====================

    /**
     * 获取当前聚焦科技的配方显示数据
     * 直接从 SyncData 中获取服务端同步的焦点科技信息，
     * 解析其配方信息并转换为可显示的格式
     *
     * @return 配方显示数据，如果没有有效配方则返回 null
     */
    public RecipeDisplayData getRecipeDisplayData() {
        // 如果已缓存，直接返回
        if (cachedRecipeData != null) {
            return cachedRecipeData;
        }

        // 获取 SyncData（服务端同步的数据）
        var syncData = ClientResearchData.getSyncData();
        if (syncData == null || syncData.getPlayerId() == -999) {
            return null;
        }

        // 获取服务端焦点科技的 ID
        var focusTechId = syncData.getFocusTech();
        if (focusTechId == null || focusTechId.equals(TechInstance.EMPTY.getIdentifier())) {
            return null;
        }

        // 直接从 syncData 的 cacheds 中获取焦点科技的 TechInstance
        TechInstance focusTechInstance = syncData.getCacheds().get(focusTechId);
        if (focusTechInstance == null || focusTechInstance.isEmpty()) {
            return null;
        }

        // 获取配方信息
        var recipeWrapper = focusTechInstance.getRecipe();
        if (recipeWrapper == null) {
            return null;
        }

        // 获取配方实例
        Recipe<?> recipe = IRecipe.getClientRecipe(recipeWrapper, Minecraft.getInstance());
        if (recipe == null) {
            return null;
        }

        // 获取对应的 RecipeCategory
        var categoryMap = ClientResearchData.recipeCategories.getRecipeCategories();
        RecipeCategory<?> category = categoryMap.get(recipe.getType());
        if (category == null) {
            return null;
        }

        // 初始化配方并获取 builderSlots
        @SuppressWarnings("unchecked")
        RecipeCategory<Recipe> genericCategory = (RecipeCategory<Recipe>) category;
        genericCategory.setRecipe(recipe);
        Map<Integer, SlotBuilder> builderSlots = category.getBuilder().getBuilderSlots();

        // 处理配方数据并缓存
        cachedRecipeData = processRecipeData(builderSlots);
        return cachedRecipeData;
    }

    /**
     * 处理配方数据，合并相同物品并计算数量
     * 遍历所有槽位构建器，根据物品角色（输入/输出/催化剂）分类，
     * 并将相同名称的物品合并，累加其数量
     *
     * @param builderSlots 槽位构建器映射表，键为槽位索引，值为槽位构建器
     * @return 处理后的配方显示数据
     */
    private RecipeDisplayData processRecipeData(Map<Integer, SlotBuilder> builderSlots) {
        RecipeDisplayData data = new RecipeDisplayData();

        // 使用 LinkedHashMap 保持物品顺序，同时用于合并相同物品
        Map<String, ItemDisplay> outputMap = new LinkedHashMap<>();
        Map<String, ItemDisplay> inputMap = new LinkedHashMap<>();

        // 遍历所有槽位构建器
        for (SlotBuilder slotBuilder : builderSlots.values()) {
            RecipeIngredientRole role = slotBuilder.getRole();
            List<List<ItemStack>> ingredients = slotBuilder.getIngredients();

            // 跳过空的材料列表
            if (ingredients.isEmpty()) {
                continue;
            }

            // 获取第一个材料选项
            List<ItemStack> firstIngredient = ingredients.get(0);
            if (!firstIngredient.isEmpty()) {
                ItemStack itemStack = firstIngredient.get(0);
                if (!itemStack.isEmpty()) {
                    String itemName = itemStack.getHoverName().getString();
                    int count = itemStack.getCount();

                    // 根据物品角色分类
                    if (role == RecipeIngredientRole.OUTPUT) {
                        mergeItem(outputMap, itemName, count);
                    } else if (role == RecipeIngredientRole.INPUT || role == RecipeIngredientRole.CATALYST) {
                        mergeItem(inputMap, itemName, count);
                    }
                }
            }
        }

        // 将 Map 转换为 List
        data.outputs.addAll(outputMap.values());
        data.inputs.addAll(inputMap.values());

        return data;
    }

    /**
     * 合并相同物品，累加数量
     * 如果物品已存在于映射表中，则累加其数量；
     * 如果是新物品，则创建新的 ItemDisplay 对象并添加到映射表
     *
     * @param map 物品映射表，键为物品名称，值为物品显示对象
     * @param itemName 物品名称
     * @param count 物品数量
     */
    private void mergeItem(Map<String, ItemDisplay> map, String itemName, int count) {
        if (map.containsKey(itemName)) {
            // 物品已存在，累加数量
            map.get(itemName).count += count;
        } else {
            // 新物品，添加到映射表
            map.put(itemName, new ItemDisplay(itemName, count));
        }
    }
}
