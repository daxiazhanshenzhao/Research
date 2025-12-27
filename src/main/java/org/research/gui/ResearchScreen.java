package org.research.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.research.Research;
import org.research.api.tech.SyncData;
import org.research.api.util.BlitContext;
import org.research.api.util.Texture;
import org.research.player.inventory.ResearchContainer;

@OnlyIn(value = Dist.CLIENT)
public class ResearchScreen extends ResearchContainerScreen {


    public ResearchScreen(SyncData syncData) {
        super(syncData);

    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public BlitContext getBg() {
        return Texture.background;
    }

    @Override
    public BlitContext getWindow() {
        return null;
    }

    @Override
    public BlitContext getInside() {
        return null;
    }


}
