package org.research.gui.component;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.research.api.tech.AbstractTech;
import org.research.api.tech.TechInstance;
import org.research.api.tech.TechState;
import org.research.api.util.BlitContext;
import org.research.api.util.Texture;
import org.research.gui.ResearchContainerScreen;

public class TechSlot extends AbstractButton {

    public static final int Width = 26;
    public static final int Height = 26;

    private static final BlitContext LOCKED = BlitContext.of(Texture.TEXTURE,36,4,Width,Height);
    private static final BlitContext AVAILABLE = BlitContext.of(Texture.TEXTURE,5,4,Width,Height);
    private static final BlitContext FOCUS = BlitContext.of(Texture.TEXTURE,68,3,Width+2,Height+2);

    private TechInstance tech;
    private ResearchContainerScreen screen;

    private boolean clientFocus = false;
//    AbstractButton

    public TechSlot(int u, int v, TechInstance tech, ResearchContainerScreen screen) {
        super(u,v,Width,Height, Component.empty());
        this.tech = tech;
        this.screen = screen;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {



        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (this.isHoveredOrFocused()) {
            // FOCUS 状态：28x28，需要偏移-1使其居中
            BlitContext background = FOCUS;
            guiGraphics.blit(
                background.texture(),
                getX() - 1, getY() - 1,
                background.u(), background.v(),
                background.width(), background.height(),
                512, 512
            );
        } else {
            // AVAILABLE 状态：26x26
            BlitContext background = AVAILABLE;
            guiGraphics.blit(
                background.texture(),
                getX(), getY(),
                background.u(), background.v(),
                background.width(), background.height(),
                512, 512
            );
        }

        guiGraphics.blit(tech.getTech().getIconResource(),getX()+5,getY()+5,0,0,16,16,16,16);
        if (tech.getState().equals(TechState.LOCKED)){
            BlitContext background = LOCKED;
            guiGraphics.blit(
                background.texture(),
                getX(), getY(),
                background.u(), background.v(),
                background.width(), background.height(),
                512, 512
            );
        }
    }

    @Override
    public void onPress() {

    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

            screen.focus(tech.getTech());



        return super.mouseClicked(mouseX, mouseY, button);
    }
}
