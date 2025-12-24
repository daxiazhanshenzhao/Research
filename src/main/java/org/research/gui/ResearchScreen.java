package org.research.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.research.api.util.Texture;
import org.research.player.inventory.ResearchContainer;

@OnlyIn(value = Dist.CLIENT)
public class ResearchScreen extends ResearchContainerScreen<ResearchContainer> {

    private ResourceLocation texture;

    public ResearchScreen(ResearchContainer container) {
        super(container);

    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        super.init();


    }

    private void initWight(){
        addRenderableWidget();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }











    @Override
    public void renderBackground(GuiGraphics guiGraphics) {
        int guiLeft = (this.width-256) / 2;
        int guiTop = (this.height-256) / 2;

        Texture.renderBg(guiLeft,guiTop,guiGraphics);





    }


}
