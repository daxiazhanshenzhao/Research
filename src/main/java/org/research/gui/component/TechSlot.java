package org.research.gui.component;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.lwjgl.glfw.GLFW;
import org.research.api.tech.AbstractTech;
import org.research.api.tech.TechInstance;
import org.research.api.tech.TechState;
import org.research.api.util.BlitContext;
import org.research.api.util.Texture;
import org.research.gui.ResearchContainerScreen;

public class TechSlot extends AbstractButton {

    public static final int Width = 26;
    public static final int Height = 26;

    private static final BlitContext BG_WHITE = BlitContext.of(Texture.TEXTURE,0,0,20,20);
    private static final BlitContext BG_BLACK = BlitContext.of(Texture.TEXTURE,21,0,20,20);

    private static final BlitContext WINDOW = BlitContext.of(Texture.TEXTURE,41,0,28,28);
    private static final BlitContext FOCUS_WINDOW = BlitContext.of(Texture.TEXTURE,70,0,28,28);

    private static final BlitContext LOCK = BlitContext.of(Texture.TEXTURE,0,24,10,15);

    private TechInstance tech;
    private ResearchContainerScreen screen;

    private boolean clientFocus = false;  // 客户端focus状态（用于UI显示）

    // 双击检测（基于游戏tick）
    private int lastClickTick = 0;
    private static final int DOUBLE_CLICK_TICKS = 10; // 10 ticks (500ms) 内的两次点击视为双击

    /**
     * 设置客户端focus状态
     * @param clientFocus 是否为客户端focus
     */
    public void setClientFocus(boolean clientFocus) {
        this.clientFocus = clientFocus;
    }

    /**
     * 获取客户端focus状态
     * @return 是否为客户端focus
     */
    public boolean isClientFocus() {
        return this.clientFocus;
    }

    /**
     * 获取科技实例
     * @return 科技实例
     */
    public TechInstance getTechInstance() {
        return this.tech;
    }
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
        // 判断是否应该显示focus效果
        // clientFocus: 客户端focus（用于UI显示）
        // tech.isFocused(): 服务端focus（追踪状态）
        // 只要有一个focus状态为true，就显示focus效果
        boolean shouldShowFocus = this.clientFocus || tech.isFocused();

        //1.渲染框
        BlitContext window = (shouldShowFocus || isHoveredOrFocused()) ? FOCUS_WINDOW : WINDOW;
        guiGraphics.blit(window.texture(), getX()-5, getY()-4, window.u(), window.v(), window.width(), window.height(), 512, 512);

        //2.渲染背景
        BlitContext bg = (shouldShowFocus || isHoveredOrFocused() || tech.getState().isBlackBg()) ? BG_BLACK : BG_WHITE;
        guiGraphics.blit(bg.texture(),getX(),getY(),bg.u(),bg.v(),bg.width(),bg.height(),512,512);

        //3.渲染内部图标
        guiGraphics.blit(tech.getTech().getIconResource(),getX()+2,getY()+2,0,0,16,16,16,16);

        //4.渲染锁
        if (tech.getState().isLocked()){
            guiGraphics.blit(LOCK.texture(),getX()+5,getY()+3,LOCK.u(),LOCK.v(),LOCK.width(),LOCK.height(),512,512);
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
        // 先检查鼠标是否在槽位范围内
        if (!this.clicked(mouseX, mouseY)
                && (button != GLFW.GLFW_MOUSE_BUTTON_1)) {
            return false;
        }
            int currentTick = screen.getOpenTicks();
            int ticksSinceLastClick = currentTick - lastClickTick;

            // 双击
            if ((ticksSinceLastClick <= DOUBLE_CLICK_TICKS) && clientFocus) {
                screen.clearFocus(this.tech.getTech());
            } else {
                //单击
                screen.focus(tech.getTech(), true);
                lastClickTick = currentTick;
            }


        return super.mouseClicked(mouseX, mouseY, button);
    }
}
