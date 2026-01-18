package org.research.gui.minecraft.component;


import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.research.Research;
import org.research.gui.minecraft.ResearchContainerScreen;

public class SearchEditBox extends EditBox implements IOpenRenderable{

    private ResearchContainerScreen screen;

    public SearchEditBox(Font font, int x, int y, int width, int height, ResearchContainerScreen screen) {
        super(font, x, y, width, height, Component.empty());
        this.screen = screen;
    }


    @Override
    public int getZLevel() {
        return 1600;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        return super.mouseClicked(mouseX, mouseY, button);
    }
}
