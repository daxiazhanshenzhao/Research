package org.research.gui.minecraft.component;


import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.research.api.gui.ClientScreenManager;
import org.research.api.util.InsideContext;

public class SearchEditBox extends EditBox implements IOpenRenderable{

    private ClientScreenManager screenManager;
    public static final int SEARCH_BOX_WIDTH = 65;
    public static final int SEARCH_BOX_HEIGHT = 17;

    // 棕色字体颜色
    private static final int BROWN_TEXT_COLOR = 0x8B4513;

    public SearchEditBox(int x, int y, ClientScreenManager manager) {
        super(Minecraft.getInstance().font, x, y,SEARCH_BOX_WIDTH, SEARCH_BOX_HEIGHT, Component.empty());
        this.screenManager = manager;
        // 设置文本颜色为棕色
        this.setTextColor(BROWN_TEXT_COLOR);
    }



    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        screenManager.handleSearchEditBox(getValue());

        var context = InsideContext.RECIPE_SEARCH_BUTTON;
        guiGraphics.blit(context.texture(),getX()-3,getY()-3,
                context.u(),context.v(),
                context.width(),context.height(),
                context.textureWidth(),context.textureHeight());
    }

    @Override
    public int getZLevel() {
        return 1600;
    }


}
