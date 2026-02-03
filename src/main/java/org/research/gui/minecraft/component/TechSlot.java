package org.research.gui.minecraft.component;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import org.research.api.recipe.IRecipe;
import org.research.api.tech.TechInstance;
import org.research.api.util.BlitContext;
import org.research.api.util.Texture;

import java.util.List;

public class TechSlot extends AbstractButton {

    public static final int Width = 20;
    public static final int Height = 20;

    private static final BlitContext BG_WHITE = BlitContext.of(Texture.TEXTURE,0,0,20,20);
    private static final BlitContext BG_BLACK = BlitContext.of(Texture.TEXTURE,21,0,20,20);

    private static final BlitContext WINDOW = BlitContext.of(Texture.TEXTURE,41,0,28,28);
    private static final BlitContext FOCUS_WINDOW = BlitContext.of(Texture.TEXTURE,70,0,28,28);

    private static final BlitContext LOCK = BlitContext.of(Texture.TEXTURE,0,24,10,15);

    public static final TechSlot EMPTY = new TechSlot(0,0,TechInstance.EMPTY);

    private TechInstance tech;
    private boolean focused = false;

    /**
     * 获取科技实例
     */
    public TechInstance getTechInstance() {
        return this.tech;
    }

    public TechSlot(int u, int v, TechInstance tech) {
        super(u,v,Width,Height, Component.empty());
        this.tech = tech;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (tech.isEmpty()) return;
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    public void renderTooltip(GuiGraphics guiGraphics, int screenMouseX, int screenMouseY) {
        if (!this.isHoveredOrFocused()) {
            return;
        }
        guiGraphics.renderComponentTooltip(Minecraft.getInstance().font,
                List.of(Component.literal("ID: " + tech.getIdentifier().toString()),
                        Component.literal("Stage: " + tech.getState().name())),
                screenMouseX, screenMouseY);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        boolean shouldShowFocus = tech.isFocused();

        // 1.渲染框
        BlitContext window = (shouldShowFocus || isHoveredOrFocused()) ? FOCUS_WINDOW : WINDOW;
        guiGraphics.blit(window.texture(), getX()-5, getY()-4, window.u(), window.v(), window.width(), window.height(), 512, 512);

        // 2.渲染背景
        BlitContext bg = (shouldShowFocus || isHoveredOrFocused() || tech.getState().isBlackBg()) ? BG_BLACK : BG_WHITE;
        guiGraphics.blit(bg.texture(), getX(), getY(), bg.u(), bg.v(), bg.width(), bg.height(), 512, 512);

        // 3.渲染内部图标
        if (tech != null && tech.getTech() != null) {
            ResourceLocation iconResource = tech.getTech().getIconResource();
            var minecraft = Minecraft.getInstance();
            AbstractTexture texture = minecraft.getTextureManager().getTexture(iconResource);

            if (texture != MissingTextureAtlasSprite.getTexture()) {
                guiGraphics.blit(iconResource, getX(), getY(), 0, 0, 16, 16, 16, 16);
            } else {
                // 使用默认图标（物品渲染）
                if (tech.getRecipe() != null && minecraft.getConnection() != null) {
                    var recipe = IRecipe.getClientRecipe(tech.getRecipe(), minecraft);
                    if (recipe != null) {
                        var item = recipe.getResultItem(minecraft.getConnection().registryAccess());
                        if (!item.isEmpty()) {
                            guiGraphics.pose().pushPose();
                            guiGraphics.pose().translate(0, 0, -100);
                            guiGraphics.renderItem(item, getX()+2, getY()+2);
                            guiGraphics.pose().popPose();
                        }
                    }
                }
            }
        }

        // 4.渲染锁（最上层）
        if (tech.getState().isLocked()){
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0, 0, +100);
            guiGraphics.blit(LOCK.texture(), getX()+5, getY()+3, LOCK.u(), LOCK.v(), LOCK.width(), LOCK.height(), 512, 512);
            guiGraphics.pose().popPose();
        }
    }

    @Override
    public void onPress() {
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }

    public Recipe<?> getRecipe(){
        return IRecipe.getClientRecipe(tech.getRecipe(), Minecraft.getInstance());
    }

    /**
     * 更新槽位的科技实例数据（避免重新创建槽位）
     */
    public void updateInstance(TechInstance newInstance) {
        this.tech = newInstance;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.playDownSound(Minecraft.getInstance().getSoundManager());
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return super.mouseReleased(mouseX, mouseY, button);
    }
}
