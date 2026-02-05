package org.research.gui.minecraft.component;


import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.research.Research;
import org.research.api.gui.ClientScreenManager;
import org.research.api.util.InsideContext;
import org.research.gui.minecraft.ResearchContainerScreen;

public class SearchEditBox extends EditBox implements IOpenRenderable{

    private ClientScreenManager screenManager;
    public static final int SEARCH_BOX_WIDTH = 65;
    public static final int SEARCH_BOX_HEIGHT = 17;

    public SearchEditBox(int x, int y, ClientScreenManager manager) {
        super(Minecraft.getInstance().font, x, y,SEARCH_BOX_WIDTH, SEARCH_BOX_HEIGHT, Component.empty());
        this.screenManager = manager;
    }



    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        screenManager.handleSearchEditBox(getValue());

        var context = InsideContext.RECIPE_SEARCH_BUTTON;
        guiGraphics.blit(context.texture(),getX(),getY(),
                context.u(),context.v(),
                context.width(),context.height(),
                context.textureWidth(),context.textureHeight());
    }

    @Override
    public int getZLevel() {
        return 1600;
    }


}
