package org.research.gui.minecraft.component;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.research.api.gui.ClientScreenManager;
import org.research.api.util.BlitContextV2;
import org.research.api.util.InsideContext;

public class ChangePageButton extends AbstractButton {
    public static final int WIDTH = 14;
    public static final int HEIGHT = 8;

    private final boolean nextPage;
    private final ClientScreenManager clientScreenManager;

    public ChangePageButton(int x, int y, boolean isNextGroup, ClientScreenManager screenManager) {
        super(x, y, WIDTH, HEIGHT,Component.empty());
        this.nextPage = isNextGroup;
        this.clientScreenManager = screenManager;
    }


    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        var context_left = InsideContext.RECIPE_CHANGE_BUTTON_LEFT;
        var context_left_active = InsideContext.RECIPE_CHANGE_BUTTON_LEFT_ACTIVE;
        var context_right = InsideContext.RECIPE_CHANGE_BUTTON_RIGHT;
        var context_right_active = InsideContext.RECIPE_CHANGE_BUTTON_RIGHT_ACTIVE;

        // 根据按钮类型(左/右)和状态(hover/normal)选择合适的纹理
        BlitContextV2 renderContext;
        if (nextPage) {
            // 下一页按钮 (右箭头)
            renderContext = isHovered() ? context_right_active : context_right;
        } else {
            // 上一页按钮 (左箭头)
            renderContext = isHovered() ? context_left_active : context_left;
        }

        // 渲染按钮纹理
        guiGraphics.blit(
                renderContext.texture(),
                this.getX(),
                this.getY(),
                renderContext.u(),
                renderContext.v(),
                renderContext.width(),
                renderContext.height(),
                renderContext.textureWidth(),
                renderContext.textureHeight()
        );
    }

    @Override
    public void onPress() {
        clientScreenManager.handleChangePageButton(nextPage);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }
}
