package org.research.category;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import org.research.Research;
import org.research.api.recipe.category.RecipeBuilder;
import org.research.api.recipe.category.SlotBuilder;

import javax.annotation.Nullable;

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

    public static final int Width = 112;
    public static final int Height = 66;

    public static final ResourceLocation Default_Background = Research.asResource("textures/gui/default_background.png");
    public static final ResourceLocation Default_Slot_Background = Research.asResource("textures/gui/default_slot_background.png");


    @Nullable
    protected RecipeBuilder recipeBuilder;

    @Nullable
    protected T currentRecipe;

    /**
     * 获取背景纹理资源位置
     * @return 背景纹理路径
     */
    protected abstract ResourceLocation getBackGround();

    /**
     * 使用 RecipeBuilder 配置配方槽位
     *
     * 示例：
     * <pre>
     * // 添加输入槽位
     * for (int i = 0; i < recipe.getIngredients().size(); i++) {
     *     builder.addSlot(10 + i * 18, 10, RecipeIngredientRole.INPUT)
     *            .addIngredients(recipe.getIngredients().get(i))
     *            .build();
     * }
     * // 添加输出槽位
     * builder.addSlot(90, 10, RecipeIngredientRole.OUTPUT)
     *        .addItems(List.of(recipe.getResultItem()))
     *        .build();
     * </pre>
     *
     * @param builder RecipeBuilder 实例
     * @param recipe 配方实例
     */
    protected abstract void setRecipe(RecipeBuilder builder, T recipe);

    /**
     * 初始化配方，创建 RecipeBuilder 并调用 setRecipe()
     *
     * @param recipe 要显示的配方
     */
    public void init(T recipe) {
        this.currentRecipe = recipe;
        this.recipeBuilder = new RecipeBuilder();
        setRecipe(recipeBuilder, recipe);
    }

    /**
     * 渲染配方界面
     *
     * @param context GuiGraphics 上下文
     * @param mouseX 鼠标 X 坐标
     * @param mouseY 鼠标 Y 坐标
     * @param partialTick 部分刻度
     */
    public void render(GuiGraphics context, double mouseX, double mouseY, int partialTick){
        renderBg(context, mouseX, mouseY, partialTick);
        renderSlots(context, mouseX, mouseY, partialTick);
    }

    /**
     * 渲染背景
     */
    private void renderBg(GuiGraphics context, double mouseX, double mouseY, int partialTick) {
        var minecraft = Minecraft.getInstance();
        ResourceLocation backgroundLocation = getBackGround();
        AbstractTexture texture = minecraft.getTextureManager().getTexture(backgroundLocation);

        // 如果纹理缺失，使用默认背景
        if (texture == MissingTextureAtlasSprite.getTexture()) {
            backgroundLocation = Default_Background;
        }

        context.blit(backgroundLocation, 0, 0, 0, 0, Width, Height);
    }

    /**
     * 渲染所有槽位
     */
    private void renderSlots(GuiGraphics context, double mouseX, double mouseY, int partialTick) {
        if (recipeBuilder == null) return;

        for (SlotBuilder slot : recipeBuilder.getSlots()) {
            renderSlot(context, slot, partialTick);
        }
    }

    /**
     * 渲染单个槽位
     */
    private void renderSlot(GuiGraphics context, SlotBuilder slot, int partialTick) {

    }

    /**
     * 获取配方的注册名称（如果配方是 Recipe 实例）
     */
    public ResourceLocation getRegistryName(T recipe) {
        if (recipe instanceof Recipe<?> vanillaRecipe) {
            return vanillaRecipe.getId();
        } else {
            return null;
        }
    }

    /**
     * 获取当前配方
     */
    @Nullable
    public T getCurrentRecipe() {
        return currentRecipe;
    }

    /**
     * 获取配方构建器
     */
    @Nullable
    public RecipeBuilder getRecipeBuilder() {
        return recipeBuilder;
    }

}
