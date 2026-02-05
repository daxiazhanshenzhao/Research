package org.research.api.recipe.category;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import org.research.Research;
import org.research.api.util.UVContext;
import org.research.gui.minecraft.component.RecipeTechSlot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 配方分类基类，用于在自定义配方 GUI 中显示配方
 *
 * @param <T> 配方类型
 *
 * 使用流程：
 * 1. 实现 getBackGround() 返回背景纹理
 * 2. 实现 setRecipe() 使用 RecipeBuilder 配置槽位
 * 3. 调用 init(recipe) 初始化配方
 * 4. 调用 render() 渲染配方
 */
public abstract class RecipeCategory<T> {

    public static final UVContext RECIPE_BACKGROUND_UV = new UVContext(12,11,112,66);

    public static final ResourceLocation Default_Background = Research.asResource("textures/gui/default_background.png");
    public static final ResourceLocation Default_Slot_Background = Research.asResource("textures/gui/default_slot_background.png");
    public static final ResourceLocation Default_Focus_Background = Research.asResource("textures/gui/default_slot_focus_background.png");

    protected RecipeBuilder builder = new RecipeBuilder();
    private final Map<Integer, RecipeTechSlot> techSlots = new HashMap<>();
    private ResourceLocation currentRecipeId = null; // 缓存当前配方ID，用于判断是否需要重新创建槽位


    public RecipeCategory(){

    }

    /**
     * 获取背景纹理资源位置
     * @return 背景纹理路径
     */
    protected abstract ResourceLocation getBackGround();

    /**
     * 每次打开都会调用
     * @param recipe 配方实例
     */
    public abstract void setRecipe(T recipe);



    public abstract RecipeType<?> getRecipeType();




    public void render(GuiGraphics context, int textureLeft, int textureTop,Recipe<?> recipe, int mouseX,int mouseY,float partialTick) {
        renderBg(context, textureLeft, textureTop);
        renderSlots(context, textureLeft, textureTop, recipe, mouseX,mouseY,partialTick);
    }

    /**
     * 渲染配方槽位的 tooltip
     * @param context 绘制上下文
     * @param textureLeft 纹理左边距
     * @param textureTop 纹理上边距
     * @param recipe 配方实例
     * @param mouseX 鼠标X坐标
     * @param mouseY 鼠标Y坐标
     */
    public void renderTooltips(GuiGraphics context, int textureLeft, int textureTop, Recipe<?> recipe, int mouseX, int mouseY) {
        var recipeTechSlots = getTechSlots(recipe, textureLeft, textureTop);
        for (RecipeTechSlot slot : recipeTechSlots) {
            slot.renderTooltip(context, mouseX, mouseY);
        }
    }

    /**
     * 渲染背景
     */
    public void renderBg(GuiGraphics context, int textureLeft, int textureTop) {
        var minecraft = Minecraft.getInstance();
        ResourceLocation backgroundLocation = getBackGround();
        AbstractTexture texture = minecraft.getTextureManager().getTexture(backgroundLocation);

        // 如果纹理缺失，使用默认背景
        if (texture == MissingTextureAtlasSprite.getTexture()) {
            backgroundLocation = Default_Background;
        }

        context.blit(backgroundLocation,
                textureLeft + RECIPE_BACKGROUND_UV.u(), textureTop + RECIPE_BACKGROUND_UV.v(),
                0, 0, RECIPE_BACKGROUND_UV.width(), RECIPE_BACKGROUND_UV.height(),RECIPE_BACKGROUND_UV.width(),RECIPE_BACKGROUND_UV.height());
    }

    public void renderSlots(GuiGraphics context, int textureLeft, int textureTop,Recipe<?> recipe, int mouseX,int mouseY,float partialTick) {
        var recipeTechSlots = getTechSlots(recipe,textureLeft,textureTop);
        for (RecipeTechSlot slot : recipeTechSlots) {
            slot.render(context,mouseX,mouseY,partialTick);
        }
    }

    public void renderOverlay(GuiGraphics context, int textureLeft, int textureTop){

    }

    /**
     * 清除槽位缓存（在窗口 resize 时调用）
     */
    public void clearCache() {
        techSlots.clear();
        currentRecipeId = null;
    }

    /**
     * 获取 RecipeBuilder 用于外部访问 builderSlots
     */
    public RecipeBuilder getBuilder() {
        return builder;
    }

    /**
     * 获取配方槽位列表（带缓存机制，避免频繁创建实例）
     * @param recipe 当前配方
     * @return 槽位列表
     */
    @SuppressWarnings("unchecked")
    private List<RecipeTechSlot> getTechSlots(Recipe<?> recipe,int textureLeft, int textureTop) {
        // 检查配方是否改变，如果改变则清空缓存并重新创建
        ResourceLocation recipeId = recipe.getId();
        if (!recipeId.equals(currentRecipeId)) {
            // 配方已改变，需要重新创建槽位
            currentRecipeId = recipeId;
            techSlots.clear();

            // 调用 setRecipe 方法让子类配置 builder
            setRecipe((T) recipe);

            // 从 builder 中获取所有 SlotBuilder 并转换为 RecipeTechSlot
            Map<Integer, SlotBuilder> builderSlots = builder.getBuilderSlots();
            for (Map.Entry<Integer, SlotBuilder> entry : builderSlots.entrySet()) {
                int slotId = entry.getKey();
                SlotBuilder slotBuilder = entry.getValue();

                // 只创建可渲染的槽位
                if (!slotBuilder.isRenderable()) {
                    continue;
                }

                // 获取槽位的第一个物品列表（如果有多个 Ingredient，这里取第一个）
                List<ItemStack> items = slotBuilder.getIngredients().isEmpty()
                    ? new ArrayList<>()
                    : slotBuilder.getIngredients().get(0);

                // 创建 RecipeTechSlot 实例
                RecipeTechSlot techSlot = new RecipeTechSlot(
                    slotBuilder.getX() + textureLeft + RECIPE_BACKGROUND_UV.u(),
                    slotBuilder.getY() + textureTop + RECIPE_BACKGROUND_UV.v(),
                    items
                );

                techSlots.put(slotId, techSlot);
            }
        }

        // 返回缓存的槽位列表
        return new ArrayList<>(techSlots.values());
    }








}
