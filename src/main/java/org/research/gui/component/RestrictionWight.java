package org.research.gui.component;

import com.alessandro.astages.util.ARestrictionType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.research.api.tech.TechInstance;

public class RestrictionWight extends AbstractWidget {

    private TechInstance techInstance;

    public RestrictionWight(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int i, int i1, float v) {
        renderBg(guiGraphics);

    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

    public void renderBg(GuiGraphics context){
        var texture = techInstance.getTech().getARestrictionTypeResource();
        context.blit(texture,getX(),getY(),0,0,width,height);
    }
}
