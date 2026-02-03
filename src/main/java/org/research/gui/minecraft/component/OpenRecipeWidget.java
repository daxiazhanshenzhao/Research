package org.research.gui.minecraft.component;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.research.api.gui.ClientScreenManager;
import org.research.api.util.BlitContext;
import org.research.api.util.Texture;

public class OpenRecipeWidget extends AbstractButton implements IOpenRenderable{

    private static final int offX = 110;
    private static final int offY = 0;

    private static final int width = 7;
    private static final int height = 13;

    private static final BlitContext OPEN_UNFOCUSED = BlitContext.of(Texture.TEXTURE,263,7,7,13);
    private static final BlitContext OPEN_FOCUSED = BlitContext.of(Texture.TEXTURE, 253, 9, 7, 11);

    private static final BlitContext OFF_UNFOCUSED = BlitContext.of(Texture.TEXTURE,254,25,7,13);
    private static final BlitContext OFF_FOCUSED = BlitContext.of(Texture.TEXTURE, 264, 27, 7, 11);

    private final ClientScreenManager screenManager;

    // 坐标验证字段
    private final int initialX;
    private final int initialY;
    private boolean isPositionValidated = true;

    public OpenRecipeWidget(int x, int y, ClientScreenManager manager) {
        super(x, y, width, height, Component.empty());
        this.screenManager = manager;
        this.initialX = x;
        this.initialY = y;
    }

    @Override
    public void onPress() {

    }

    /**
     * 验证坐标是否在合理范围内
     * @param x 要验证的X坐标
     * @param y 要验证的Y坐标
     * @return true如果坐标合理，false如果坐标异常
     */
    private boolean validatePosition(int x, int y) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getWindow() == null) {
            return false;
        }

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        // 检查坐标是否在屏幕范围内（留出边界）
        if (x < -50 || x > screenWidth + 50) {
            return false;
        }
        if (y < -50 || y > screenHeight + 50) {
            return false;
        }

        // 检查坐标偏移是否合理（不应该偏离初始位置太远）
        int maxOffset = 500; // 最大允许偏移
        if (Math.abs(x - initialX) > maxOffset || Math.abs(y - initialY) > maxOffset) {
            return false;
        }

        return true;
    }

    /**
     * 获取期望的坐标（根据当前打开状态）
     * @param isOpen 是否打开状态
     * @return 期望的X坐标
     */
    private int getExpectedX(boolean isOpen) {
        return isOpen ? initialX + offX : initialX;
    }

    /**
     * 修复异常坐标，恢复到正确位置
     */
    private void fixPosition() {
        boolean isOpen = screenManager.getScreenData().isOpenRecipe();
        int expectedX = getExpectedX(isOpen);
        int expectedY = initialY + offY;

        if (!validatePosition(expectedX, expectedY)) {
            // 如果期望坐标也不合理，回退到初始位置并关闭状态
            screenManager.getScreenData().setOpenRecipe(false);
            this.setPosition(initialX, initialY);
            isPositionValidated = false;
        } else {
            this.setPosition(expectedX, expectedY);
            isPositionValidated = true;
        }
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 渲染前验证坐标
        if (!validatePosition(this.getX(), this.getY())) {
            fixPosition();
        }

        // 验证位置是否与状态同步
        boolean isOpen = screenManager.getScreenData().isOpenRecipe();
        int expectedX = getExpectedX(isOpen);
        if (Math.abs(this.getX() - expectedX) > 5) { // 允许5像素的误差
            fixPosition();
        }

        if (isHovered()){
            if (screenManager.getScreenData().isOpenRecipe()){
                guiGraphics.blit(OPEN_FOCUSED.texture(), this.getX(), this.getY()+2, OPEN_FOCUSED.u(), OPEN_FOCUSED.v(), width, height,512,512);
            }else{
                guiGraphics.blit(OFF_FOCUSED.texture(), this.getX(), this.getY()+2, OFF_FOCUSED.u(), OFF_FOCUSED.v(), width, height,512,512);
            }
        }else{
            if (screenManager.getScreenData().isOpenRecipe()){
                guiGraphics.blit(OPEN_UNFOCUSED.texture(), this.getX(), this.getY(), OPEN_UNFOCUSED.u(), OPEN_UNFOCUSED.v(), width, height,512,512);
            }else {
                guiGraphics.blit(OFF_UNFOCUSED.texture(), this.getX(), this.getY(), OFF_UNFOCUSED.u(), OFF_UNFOCUSED.v(), width, height,512,512);
            }
        }
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        // 点击前验证当前坐标
        if (!validatePosition(this.getX(), this.getY())) {
            fixPosition();
            return super.mouseReleased(mouseX, mouseY, button);
        }

        boolean wasOpen = screenManager.getScreenData().isOpenRecipe();
        screenManager.getScreenData().setOpenRecipe(!wasOpen);

        // 计算新位置
        int newX, newY;
        if (screenManager.getScreenData().isOpenRecipe()) {
            // 现在是打开状态，向右偏移
            newX = this.getX() + offX;
            newY = this.getY() + offY;
        } else {
            // 现在是关闭状态，向左偏移（回到原位）
            newX = this.getX() - offX;
            newY = this.getY() - offY;
        }

        // 验证新位置是否合理
        if (validatePosition(newX, newY)) {
            this.setPosition(newX, newY);
            isPositionValidated = true;
        } else {
            // 新位置不合理，回退状态并修复位置
            screenManager.getScreenData().setOpenRecipe(wasOpen);
            fixPosition();
            isPositionValidated = false;
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

    @Override
    public int getZLevel() {
        return 1700;
    }
}
