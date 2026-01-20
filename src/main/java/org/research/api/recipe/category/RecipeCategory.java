package org.research.api.recipe.category;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import org.research.Research;

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


    @Getter
    protected RecipeBuilder recipeBuilder;

    @Setter
    protected T currentRecipe;

    public void init(T recipe){
        this.recipeBuilder = new RecipeBuilder();
        this.currentRecipe = recipe;
        setRecipe(recipeBuilder,currentRecipe);
    }

    /**
     * 获取背景纹理资源位置
     * @return 背景纹理路径
     */
    protected abstract ResourceLocation getBackGround();

    /**
     * 每次打开都会调用
     * @param recipe
     */
    protected abstract void setRecipe(RecipeBuilder builder, T recipe);

    public void setRecipe(T recipe){
        this.currentRecipe = recipe;
        setRecipe(this.recipeBuilder, recipe);
    }

    public abstract RecipeType<?> getRecipeType();



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





}
