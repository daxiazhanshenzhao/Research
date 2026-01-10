package org.research.gui.component;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.research.api.util.BlitContext;
import org.research.api.util.Texture;
import org.research.gui.ResearchScreen;

import java.awt.*;

public class OpenRecipeWight extends AbstractButton {


    private static final int offX = 110;
    private static final int offY = 0;

    private static final int width = 7;
    private static final int height = 13;

    private ResearchScreen screen;

    private static final BlitContext OPEN_UNFOCUSED = BlitContext.of(Texture.TEXTURE,263,7,7,13);
    private static final BlitContext OPEN_FOCUSED = BlitContext.of(Texture.TEXTURE, 253, 9, 7, 11);

    private static final BlitContext OFF_UNFOCUSED = BlitContext.of(Texture.TEXTURE,254,25,7,13);
    private static final BlitContext OFF_FOCUSED = BlitContext.of(Texture.TEXTURE, 264, 27, 7, 11);


    public OpenRecipeWight(int x,int y, ResearchScreen screen) {
        super(x, y, width, height, Component.empty());
        this.screen = screen;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        var pose = guiGraphics.pose();
        pose.pushPose();
        guiGraphics.pose().translate(0, 0, 1600);

        if (isHoveredOrFocused()){
            if (screen.isOpenRecipeBook()){
                guiGraphics.blit(OPEN_FOCUSED.texture(), this.getX(), this.getY()+2, OPEN_FOCUSED.u(), OPEN_FOCUSED.v(), width, height,512,512);
            }else{
                guiGraphics.blit(OFF_FOCUSED.texture(), this.getX(), this.getY()+2, OFF_FOCUSED.u(), OFF_FOCUSED.v(), width, height,512,512);
            }
        }else{
            if (screen.isOpenRecipeBook()){
                guiGraphics.blit(OPEN_UNFOCUSED.texture(), this.getX(), this.getY(), OPEN_UNFOCUSED.u(), OPEN_UNFOCUSED.v(), width, height,512,512);
            }else {
                guiGraphics.blit(OFF_UNFOCUSED.texture(), this.getX(), this.getY(), OFF_UNFOCUSED.u(), OFF_UNFOCUSED.v(), width, height,512,512);
            }
        }
        pose.popPose();


    }

    @Override
    public void onPress() {
        boolean wasOpen = screen.isOpenRecipeBook();
        screen.setOpenRecipeBook(!wasOpen);

        // 根据状态变化应用偏移
        // 如果从关闭变为打开，向右偏移；如果从打开变为关闭，向左偏移（负偏移）
        if (screen.isOpenRecipeBook()) {
            // 现在是打开状态，向右偏移
            this.setPosition(this.getX() + offX, this.getY() + offY);
        } else {
            // 现在是关闭状态，向左偏移（回到原位）
            this.setPosition(this.getX() - offX, this.getY() - offY);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }
}
