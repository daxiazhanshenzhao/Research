package org.research.api.gui.layer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.Recipe;
import org.research.api.client.ClientResearchData;
import org.research.api.config.ClientConfig;
import org.research.api.util.RecipeUtil;
import org.research.api.recipe.category.RecipeCategory;
import org.research.api.recipe.category.SlotBuilder;
import org.research.api.tech.TechInstance;
import org.research.api.tech.TechState;
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

    /** 字体缩放比例（1.0 = 正常大小，0.8 = 80%，1.2 = 120%）*/
    public static final float FONT_SCALE = 1f;

    /** Overlay 内容区域的最大宽度（用于自动缩放文本）*/
    private static final int MAX_TEXT_WIDTH = 50;

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
    private RecipeUtil.RecipeDisplayData cachedRecipeData;

    /** 缓存的中心高度 */
    private int cachedCenterHeight = -1;

    /** 缓存的科技是否为 WAITING 状态 */
    private Boolean cachedIsWaiting = null;

    // ==================== 缓存管理方法 ====================

    /**
     * 清除所有缓存数据
     * 当服务端数据更新时调用，确保 Overlay 显示最新数据
     */
    public void clearCache() {
        cachedRecipeData = null;
        cachedCenterHeight = -1;
        cachedIsWaiting = null;
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

        RecipeUtil.RecipeDisplayData data = getRecipeDisplayData();
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
     * 获取字体缩放比例
     * @return 字体缩放比例（1.0 = 正常大小）
     */
    public float getFontScale() {
        return FONT_SCALE;
    }

    /**
     * 计算文本的自动缩放比例
     * 根据文本宽度和最大允许宽度自动计算缩放比例
     *
     * @param text 要渲染的文本
     * @return 缩放比例（1.0 = 不缩放，<1.0 = 缩小）
     */
    public float calculateTextScale(Component text) {
        Font font = getFont();
        int textWidth = font.width(text);

        // 如果文本宽度超过最大宽度，计算缩放比例
        if (textWidth > MAX_TEXT_WIDTH) {
            return (float) MAX_TEXT_WIDTH / textWidth;
        }

        // 应用全局字体缩放
        return FONT_SCALE;
    }

    /**
     * 计算文本的自动缩放比例（字符串版本）
     *
     * @param text 要渲染的文本
     * @return 缩放比例（1.0 = 不缩放，<1.0 = 缩小）
     */
    public float calculateTextScale(String text) {
        Font font = getFont();
        int textWidth = font.width(text);

        // 如果文本宽度超过最大宽度，计算缩放比例
        if (textWidth > MAX_TEXT_WIDTH) {
            return (float) MAX_TEXT_WIDTH / textWidth;
        }

        // 应用全局字体缩放
        return FONT_SCALE;
    }

    /**
     * 获取输出物品列表
     * @return 输出物品显示列表，如果没有则返回空列表
     */
    public List<RecipeUtil.ItemDisplay> getOutputItems() {
        RecipeUtil.RecipeDisplayData data = getRecipeDisplayData();
        return data != null ? data.outputs : Collections.emptyList();
    }

    /**
     * 获取输入物品列表
     * @return 输入物品显示列表，如果没有则返回空列表
     */
    public List<RecipeUtil.ItemDisplay> getInputItems() {
        RecipeUtil.RecipeDisplayData data = getRecipeDisplayData();
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
        cachedIsWaiting = null;
    }

    /**
     * 检查当前聚焦科技是否为 WAITING 状态
     * WAITING 状态表示科技已完成但有多个分支需要玩家手动选择
     * @return 如果是 WAITING 状态返回 true，否则返回 false
     */
    public boolean isWaitingState() {
        if (cachedIsWaiting != null) {
            return cachedIsWaiting;
        }

        var syncData = ClientResearchData.getSyncData();
        if (syncData == null || syncData.getPlayerId() == -999) {
            cachedIsWaiting = false;
            return false;
        }

        var focusTechId = syncData.getFocusTech();
        if (focusTechId == null || focusTechId.equals(TechInstance.EMPTY.getIdentifier())) {
            cachedIsWaiting = false;
            return false;
        }

        TechInstance focusTechInstance = syncData.getCacheds().get(focusTechId);
        if (focusTechInstance == null || focusTechInstance.isEmpty()) {
            cachedIsWaiting = false;
            return false;
        }

        cachedIsWaiting = focusTechInstance.getState() == TechState.WAITING;
        return cachedIsWaiting;
    }

    /**
     * 检查当前聚焦科技是否为 AVAILABLE 状态
     * 只有 AVAILABLE 状态才显示配方，其他状态显示"选择新配方"
     * @return 如果是 AVAILABLE 状态返回 true，否则返回 false
     */
    public boolean isAvailableState() {
        var syncData = ClientResearchData.getSyncData();
        if (syncData == null || syncData.getPlayerId() == -999) {
            return false;
        }

        var focusTechId = syncData.getFocusTech();
        if (focusTechId == null || focusTechId.equals(TechInstance.EMPTY.getIdentifier())) {
            return false;
        }

        TechInstance focusTechInstance = syncData.getCacheds().get(focusTechId);
        if (focusTechInstance == null || focusTechInstance.isEmpty()) {
            return false;
        }

        return focusTechInstance.getState() == TechState.AVAILABLE;
    }

    /**
     * 检查当前焦点科技是否为空（EMPTY）
     * @return 如果焦点科技为空返回 true，否则返回 false
     */
    public boolean isFocusTechEmpty() {
        var syncData = ClientResearchData.getSyncData();
        if (syncData == null || syncData.getPlayerId() == -999) {
            return true;
        }

        var focusTechId = syncData.getFocusTech();
        if (focusTechId == null || focusTechId.equals(TechInstance.EMPTY.getIdentifier())) {
            return true;
        }

        TechInstance focusTechInstance = syncData.getCacheds().get(focusTechId);
        return focusTechInstance == null || focusTechInstance.isEmpty();
    }

    /**
     * 获取 WAITING 状态的提示消息
     * @return 本地化的提示消息
     */
    public Component getWaitingMessage() {
        return Component.translatable("research.overlay.waiting_message");
    }

    /**
     * 获取非 AVAILABLE 状态的提示消息（选择新配方）
     * @return 本地化的提示消息
     */
    public Component getSelectRecipeMessage() {
        return Component.translatable("research.overlay.select_recipe_message");
    }

    /**
     * 获取焦点科技为空时的提示消息
     * @return 本地化的提示消息
     */
    public Component getEmptyFocusMessage() {
        return Component.translatable("research.overlay.empty_focus_message");
    }


    // ==================== 配方数据获取方法 ====================

    /**
     * 获取当前聚焦科技的配方显示数据
     * 直接从 SyncData 中获取服务端同步的焦点科技信息，
     * 解析其配方信息并转换为可显示的格式
     *
     * ✅ 只有在 AVAILABLE 状态时才返回配方数据
     *
     * @return 配方显示数据，如果没有有效配方则返回 null
     */
    public RecipeUtil.RecipeDisplayData getRecipeDisplayData() {
        // 如果已缓存，直接返回
        if (cachedRecipeData != null) {
            return cachedRecipeData;
        }

        // ✅ 只有 AVAILABLE 状态才显示配方数据
        if (!isAvailableState()) {
            return null;
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
        Recipe<?> recipe = RecipeUtil.getClientRecipe(recipeWrapper, Minecraft.getInstance());
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

        // 处理配方数据并缓存（使用 RecipeUtil 中的方法）
        cachedRecipeData = RecipeUtil.processRecipeData(builderSlots);

        // 注入玩家背包数据到输入物品（显示 n/m 格式）
        var inventoryData = ClientResearchData.getClientInventoryData();
        for (RecipeUtil.ItemDisplay inputItem : cachedRecipeData.inputs) {
            int playerCount = inventoryData.getItemCount(inputItem.name);
            inputItem.setCurrentCount(playerCount);
        }

        return cachedRecipeData;
    }


}
