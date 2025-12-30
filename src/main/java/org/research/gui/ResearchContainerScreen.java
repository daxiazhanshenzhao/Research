package org.research.gui;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.checkerframework.checker.signature.qual.Identifier;
import org.joml.Vector2i;
import org.research.Research;
import org.research.api.client.ClientResearchData;
import org.research.api.tech.AbstractTech;
import org.research.api.tech.SyncData;
import org.research.api.tech.TechInstance;
import org.research.api.tech.capability.ITechTreeCapability;
import org.research.api.tech.graphTree.Vec2i;
import org.research.api.util.BlitContext;
import org.research.api.util.Texture;
import org.research.gui.component.TechSlot;
import org.research.player.inventory.AbstractResearchContainer;

import java.util.HashMap;
import java.util.Map;
import org.joml.Math;


public abstract class ResearchContainerScreen extends Screen {
    //data
    private SyncData data;
    private Map<ResourceLocation,TechSlot> slots = new HashMap<>();

    //render
//    private int u,v,width,height;
    private double scrollOffs = 0;
    private int centerX,centerY;
    private boolean isMoving = false;


    protected ResearchContainerScreen(SyncData data) {
        super(Component.empty());
        this.data = data;
        init();
    }

    @Override
    protected void init() {
        //同步
        ClientResearchData.syncFromServer();
        this.data = ClientResearchData.getSyncData();

        //科技槽位
        var insList = data.getCacheds();
        var vecList = data.getVecMap();
        for (TechInstance instance : insList.values()) {
            var vec = vecList.getOrDefault(instance.getIdentifier(), Vec2i.EMPTY);

            var slot = new TechSlot(vec.x,vec.y,instance);
            slots.put(instance.getIdentifier(),slot);

        }
        super.init();
    }



    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int guiLeft = (this.width-256)/2;
        int guiTop = (this.height-256)/2;


        renderSlots(guiGraphics);
        renderBg(guiGraphics,guiLeft,guiTop);




        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void tick() {

//        Research.LOGGER.info(String.valueOf(this.scrollOffs));
        
    }


    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {

        setScrollOffs(this.scrollOffs+delta*0.1);

        return super.mouseScrolled(mouseX, mouseY, delta);
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

    private void renderSlots(GuiGraphics context){
        for (TechSlot slot : slots.values()){

        }
    }

    public void focus(AbstractTech tech){

    }

    public void setScrollOffs(double scrollOffs) {
        this.scrollOffs = Math.clamp(0, 1, scrollOffs);
    }
}
