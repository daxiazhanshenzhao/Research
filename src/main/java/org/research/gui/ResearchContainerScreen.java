package org.research.gui;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.checkerframework.checker.signature.qual.Identifier;
import org.joml.Vector2i;
import org.research.api.tech.SyncData;
import org.research.api.tech.TechInstance;
import org.research.api.tech.capability.ITechTreeCapability;
import org.research.api.util.BlitContext;
import org.research.api.util.Texture;
import org.research.gui.component.TechSlot;
import org.research.player.inventory.AbstractResearchContainer;

import java.util.HashMap;

public abstract class ResearchContainerScreen extends Screen {

    private SyncData data;


    private HashMap<ResourceLocation, TechSlot> techSlotMap = new HashMap<>();

    protected ResearchContainerScreen() {
        super(Component.empty());
    }


    public void setData(SyncData data) {
        this.data = data;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int guiLeft = (this.width-256)/2;
        int guiTop = (this.height-256)/2;

        renderBg(guiGraphics,guiLeft,guiTop);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void tick() {



    }

    /**
     * @return 边框图片和整个大小
     */
    public abstract BlitContext getBg();

    /**
     * @return 边框厚度围成的内部区域
     */
    public abstract BlitContext getWindow();

    /**
     * @return 内部图片和整张图片的大小
     */
    public abstract BlitContext getInside();



    private void renderBg(GuiGraphics context,int x,int y){
        var blitContext = getBg();
        context.blit(blitContext.texture(),x,y,blitContext.u(),blitContext.v(),blitContext.width(),blitContext.height());

    }

}
