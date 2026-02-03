package org.research.gui.minecraft.component;


import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.research.Research;
import org.research.api.gui.ClientScreenManager;
import org.research.gui.minecraft.ResearchContainerScreen;

public class SearchEditBox extends EditBox implements IOpenRenderable{

    private ClientScreenManager screenManager;
    public static final int SEARCH_BOX_WIDTH = 80;
    public static final int SEARCH_BOX_HEIGHT = 20;

    public SearchEditBox(int x, int y, ClientScreenManager manager) {
        super(Minecraft.getInstance().font, x, y,SEARCH_BOX_WIDTH, SEARCH_BOX_HEIGHT, Component.empty());
        this.screenManager = manager;
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
