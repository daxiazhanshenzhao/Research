package org.research.gui;

import com.mojang.datafixers.util.Pair;
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
}
