package org.research.gui;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.research.api.tech.SyncData;
import org.research.api.util.BlitContext;
import org.research.api.util.Texture;
import org.research.gui.component.OpenRecipeWight;

@OnlyIn(value = Dist.CLIENT)
public class ResearchScreen extends ResearchContainerScreen {


    public ResearchScreen(SyncData syncData) {
        super(syncData);
    }

    private boolean openRecipeBook = false;

    private static final BlitContext RECIPE_BG_OPEN = BlitContext.of(Texture.TEXTURE,55,47,146,213);
    private static final BlitContext RECIPE_BG_OFF = BlitContext.of(Texture.TEXTURE,9,46,35,213);

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        super.init();

        int guiLeft = (this.width - 256) / 2;
        int guiTop = (this.height - 256) / 2;

        addRenderableWidget(new OpenRecipeWight(guiLeft+18, guiTop+104, this));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 先渲染父类的内容（背景、inside区域等）
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        var pose = guiGraphics.pose();
        pose.pushPose();
        guiGraphics.pose().translate(0, 0, 1500);

        renderRecipeBg(guiGraphics);

        pose.popPose();


    }

    private void renderRecipeBg(GuiGraphics guiGraphics) {
        var context = openRecipeBook ? RECIPE_BG_OPEN : RECIPE_BG_OFF;

        int x = getGuiLeft()+1;
        int y = getGuiTop()+18;
        guiGraphics.blit(context.texture(), x, y, context.u(), context.v(), context.width(), context.height(), 512, 512);
    }

    @Override
    protected Pair<Float, Float> getMaxOrMinScale() {
        return Pair.of(0.5f,2f);
    }

    @Override
    public BlitContext getBg() {
        return Texture.background;
    }

    @Override
    protected BlitContext getWindow() {
        return Texture.window;
    }

    @Override
    protected BlitContext getInside() {
        return Texture.inside;
    }


    @Override
    public void tick() {


        super.tick();
    }

    public boolean isOpenRecipeBook() {
        return openRecipeBook;
    }

    public void setOpenRecipeBook(boolean openRecipeBook) {
        this.openRecipeBook = openRecipeBook;
    }


}
