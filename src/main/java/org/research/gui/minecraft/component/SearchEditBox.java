package org.research.gui.minecraft.component;


import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

public class SearchEditBox extends EditBox implements IOpenRenderable{

    public SearchEditBox(Font font, int x, int y, int width, int height, Component message) {
        super(font, x, y, width, height, message);
        // 设置最大输入长度
        this.setMaxLength(50);
        // 设置边框
        this.setBordered(true);
        // 设置可见
        this.setVisible(true);
        // 设置可编辑
        this.setEditable(true);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 直接渲染，不做坐标变换
        // Z坐标提升应该由父容器统一处理
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        boolean result = super.isMouseOver(mouseX, mouseY);
        if (result) {
            System.out.println("SearchEditBox isMouseOver=true at (" + mouseX + ", " + mouseY +
                             "), bounds: [" + getX() + ", " + getY() + ", " + (getX() + width) + ", " + (getY() + height) + "]");
        }
        return result;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        System.out.println("SearchEditBox.mouseClicked called at (" + mouseX + ", " + mouseY + ")");
        boolean result = super.mouseClicked(mouseX, mouseY, button);
        // 调试信息
        System.out.println("SearchEditBox mouseClicked result: " + result + ", Focused: " + this.isFocused());
        return result;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean result = super.keyPressed(keyCode, scanCode, modifiers);
        // 调试信息
        if (result) {
            System.out.println("SearchEditBox keyPressed: " + keyCode + ", focused: " + this.isFocused());
        }
        return result;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        boolean result = super.charTyped(codePoint, modifiers);
        // 调试信息
        if (result) {
            System.out.println("SearchEditBox charTyped: " + codePoint + ", text: " + this.getValue());
        }
        return result;
    }

    @Override
    public void setZLevel(int level) {

    }
}
