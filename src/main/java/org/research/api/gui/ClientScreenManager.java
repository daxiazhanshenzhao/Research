package org.research.api.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import lombok.Setter;
import org.lwjgl.glfw.GLFW;
import org.research.Research;
import org.research.api.config.ClientConfig;
import org.research.api.util.BlitContextV2;
import org.research.api.util.Texture;
import org.research.api.util.UVContext;

import java.util.Optional;

@Getter
@Setter
public class ClientScreenManager {


    private PoseStackData poseStackData = new PoseStackData();

    private ScreenConfigData screenConfigData = new ScreenConfigData(
            0.5d,
            2.0d,
            new BlitContextV2(Research.asResource("textures/gui/background.png"), 0, 0, 1024, 1024, 1024, 1024),
            new BlitContextV2(Research.asResource("textures/gui/window.png"), 0, 0, 256, 256, 256, 256),
            new UVContext(15, 28, 226, 186),
            0.8d
    );

    private ScreenData screenData = new ScreenData();
    private MouseData mouseData = new MouseData();

    private Optional<TechSlotData> optTechSlotData = Optional.empty();
    private Optional<RecipeTechData> optRecipeTechData = Optional.empty();

    public ClientScreenManager() {
        refreshConfigSafely();
    }






    /**
     * 鼠标拖动平移界面
     */
    public boolean translate(double mouseX, double mouseY, int button, double deltaX, double deltaY){
        //在界面操作界面内

        //是鼠标左键操作

        //

        return true;
    }

    /**
     * 滚轮缩放界面（随鼠标中心缩放）
     * @param mouseX 屏幕空间鼠标X坐标
     * @param mouseY 屏幕空间鼠标Y坐标
     * @param delta 滚轮增量
     * @return 是否处理了该事件
     */
    public boolean handleMouseScrolled(double mouseX, double mouseY, double delta) {
        // 检查鼠标是否在内部区域
        if (!isMouseInSide(mouseX, mouseY)) {
            return false;
        }

        // 计算新的缩放值
        float oldScale = mouseData.getScale();
        float newScale = oldScale + (float)(delta * 0.1);

        // 限制缩放范围
        newScale = (float) clamp(newScale, screenConfigData.minScale(), screenConfigData.maxScale());

        if (Math.abs(newScale - oldScale) < 0.001f) {
            return true;  // 缩放值没有变化
        }

        // 获取缩放中心（内部区域的中心）
        int centerX = screenData.getInsideX() + screenConfigData.insideUV().width() / 2;
        int centerY = screenData.getInsideY() + screenConfigData.insideUV().height() / 2;

        // 计算鼠标相对于当前变换后中心点的偏移（考虑当前的偏移量）
        // 这里需要计算在当前变换下，鼠标指向的"世界空间"位置
        double currentOffsetX = mouseData.getOffsetX();
        double currentOffsetY = mouseData.getOffsetY();

        double worldMouseX = (mouseX - currentOffsetX - centerX) / oldScale + centerX;
        double worldMouseY = (mouseY - currentOffsetY - centerY) / oldScale + centerY;

        // 调整偏移量，使得鼠标指向的世界空间位置在缩放后保持在屏幕上的相同位置
        // 新的屏幕位置 = offsetX + (worldPos - centerX) * newScale + centerX
        // 我们希望新的屏幕位置 = mouseX
        // 所以: offsetX_new = mouseX - (worldMouseX - centerX) * newScale - centerX
        double newOffsetX = mouseX - (worldMouseX - centerX) * newScale - centerX;
        double newOffsetY = mouseY - (worldMouseY - centerY) * newScale - centerY;

        // 更新缩放值和偏移量
        mouseData.setScale(newScale);
        mouseData.setOffsetX(newOffsetX);
        mouseData.setOffsetY(newOffsetY);

        // 缩放后应用边界限制
        clampOffset();

        // 更新当前鼠标的转换坐标
        updateTransformedMouseCoords(mouseX, mouseY);

        return true;
    }

    /**
     * 限制偏移量在合理范围内
     * 确保背景边框不会超出 insideX, insideY 定义的可视区域
     */
    private void clampOffset() {
        var insideUV = screenConfigData.insideUV();
        var bgContext = screenConfigData.backGround();

        int bgWidth = bgContext.width();
        int bgHeight = bgContext.height();
        int windowWidth = insideUV.width();
        int windowHeight = insideUV.height();

        if (bgWidth <= 0 || bgHeight <= 0 || windowWidth <= 0 || windowHeight <= 0) {
            return;  // 如果没有设置边界信息，不做限制
        }

        // 获取内部区域的中心点（相对于屏幕）
        int centerX = screenData.getInsideX() + windowWidth / 2;
        int centerY = screenData.getInsideY() + windowHeight / 2;

        // 获取当前缩放值
        float scale = mouseData.getScale();

        // 背景的边界（世界空间）
        double bgMinX = 0;
        double bgMinY = 0;
        double bgMaxX = bgWidth;
        double bgMaxY = bgHeight;

        // 内部区域的边界（相对于中心点）
        double halfWindowWidth = windowWidth / 2.0;
        double halfWindowHeight = windowHeight / 2.0;

        // 计算偏移量的限制范围
        // offsetX 的限制：确保背景的右边界不超过窗口的右边界
        // 公式：offsetX + (bgMaxX - centerX) * scale <= halfWindowWidth
        // 所以：offsetX <= halfWindowWidth - (bgMaxX - centerX) * scale
        double maxOffsetX = halfWindowWidth - (bgMaxX - centerX) * scale;

        // offsetX 的限制：确保背景的左边界不超过窗口的左边界
        // 公式：offsetX + (bgMinX - centerX) * scale >= -halfWindowWidth
        // 所以：offsetX >= -halfWindowWidth - (bgMinX - centerX) * scale
        double minOffsetX = -halfWindowWidth - (bgMinX - centerX) * scale;

        // 同理计算 Y 轴的限制
        double maxOffsetY = halfWindowHeight - (bgMaxY - centerY) * scale;
        double minOffsetY = -halfWindowHeight - (bgMinY - centerY) * scale;

        // 应用限制
        double currentOffsetX = mouseData.getOffsetX();
        double currentOffsetY = mouseData.getOffsetY();

        mouseData.setOffsetX(clamp(currentOffsetX, minOffsetX, maxOffsetX));
        mouseData.setOffsetY(clamp(currentOffsetY, minOffsetY, maxOffsetY));
    }

    /**
     * 将值限制在指定范围内
     */
    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * 更新转换后的鼠标坐标（屏幕空间 -> 世界空间）
     * @param screenX 屏幕空间的X坐标
     * @param screenY 屏幕空间的Y坐标
     */
    private void updateTransformedMouseCoords(double screenX, double screenY) {
        // 获取缩放中心
        var insideUV = screenConfigData.insideUV();
        int centerX = screenData.getInsideX() + insideUV.width() / 2;
        int centerY = screenData.getInsideY() + insideUV.height() / 2;

        // 开始逆变换（从外到内，逆序撤销）
        double tempMouseX = screenX;
        double tempMouseY = screenY;

        // 撤销平移偏移
        tempMouseX -= mouseData.getOffsetX();
        tempMouseY -= mouseData.getOffsetY();

        // 撤销移动到中心
        tempMouseX -= centerX;
        tempMouseY -= centerY;

        // 撤销缩放
        float scale = mouseData.getScale();
        tempMouseX /= scale;
        tempMouseY /= scale;

        // 撤销从中心移回
        tempMouseX += centerX;
        tempMouseY += centerY;

        // 保存转换后的坐标（这就是世界空间坐标）
        mouseData.setTransformedMouseX(tempMouseX);
        mouseData.setTransformedMouseY(tempMouseY);
    }

    /**
     * 应用变换到PoseStack
     * 在渲染时调用，应用所有变换（平移和缩放）
     */
    public void applyTransform() {
        if (poseStackData.getPoseStack() == null) {
            return;
        }

        // 获取缩放中心（内部区域的中心）
        var insideUV = screenConfigData.insideUV();
        int centerX = screenData.getInsideX() + insideUV.width() / 2;
        int centerY = screenData.getInsideY() + insideUV.height() / 2;

        // 先应用拖拽偏移
        poseStackData.translate((float)mouseData.getOffsetX(), (float)mouseData.getOffsetY(), 0);

        // 然后应用缩放（以内部区域中心为缩放中心）
        poseStackData.translate(centerX, centerY, 0);
        poseStackData.scale(mouseData.getScale(), mouseData.getScale(), 1.0f);
        poseStackData.translate(-centerX, -centerY, 0);
    }

    public void handleMousePositon(double mouseX, double mouseY){
        // 更新转换后的鼠标坐标（考虑缩放和平移）
        updateTransformedMouseCoords(mouseX, mouseY);
    }

    /**
     * 检查鼠标是否在 GUI 窗口区域内（基于屏幕坐标）
     * @param screenMouseX 屏幕鼠标 X 坐标
     * @param screenMouseY 屏幕鼠标 Y 坐标
     * @return true 如果鼠标在 GUI 窗口区域
     */
    public boolean isMouseInGUI(double screenMouseX, double screenMouseY){
        // 窗口区域的边界（使用整张图片的尺寸）
        int guiLeft = screenData.getGuiLeft();
        int guiTop = screenData.getGuiTop();
        int guiRight = guiLeft + screenConfigData.window().textureWidth();
        int guiBottom = guiTop + screenConfigData.window().textureHeight();

        return screenMouseX >= guiLeft && screenMouseX < guiRight
                && screenMouseY >= guiTop && screenMouseY < guiBottom;
    }

    /**
     * 检查鼠标是否在内部区域内（基于屏幕坐标）
     * @param screenMouseX 屏幕鼠标 X 坐标
     * @param screenMouseY 屏幕鼠标 Y 坐标
     * @return true 如果鼠标在内部区域
     */
    public boolean isMouseInSide(double screenMouseX, double screenMouseY){

        var insideUV = screenConfigData.insideUV();
        int insideLeft = screenData.getInsideX();
        int insideTop = screenData.getInsideY();
        int insideRight = insideLeft + insideUV.width();
        int insideBottom = insideTop + insideUV.height();

        return screenMouseX >= insideLeft && screenMouseX < insideRight
                && screenMouseY >= insideTop && screenMouseY < insideBottom;
    }








    /**
     * 处理鼠标拖拽平移
     * @param dragX X方向拖拽增量
     * @param dragY Y方向拖拽增量
     */
    public void handleMouseDrag(double mouseX, double mouseY, int button, double dragX, double dragY) {
        // 检查是否在内部区域
        if (!isMouseInSide(mouseX, mouseY)) {
            return;
        }

        // 累加偏移量
        // 除以 scale 是为了在不同缩放级别下保持一致的拖拽速度
        float scale = mouseData.getScale();
        double newOffsetX = mouseData.getOffsetX() + dragX / scale;
        double newOffsetY = mouseData.getOffsetY() + dragY / scale;

        mouseData.setOffsetX(newOffsetX);
        mouseData.setOffsetY(newOffsetY);

        // 应用移动限制
        clampOffset();

        // 更新转换后的鼠标坐标
        updateTransformedMouseCoords(mouseX, mouseY);
    }

    public void handleMouseClick(double mouseX, double mouseY, int button) {
        // TODO: 实现点击逻辑
    }

    private void refreshConfigSafely() {
        try {
            screenConfigData = new ScreenConfigData(
                    ClientConfig.MIN_SCALE.get(),
                    ClientConfig.MAX_SCALE.get(),
                    ClientConfig.getBackgroundBlitContextV2(),
                    ClientConfig.getWindowBlitContextV2(),
                    ClientConfig.getInsideUVContext(),
                    ClientConfig.MOVABLE_AREA_RATIO.get()
            );
        } catch (IllegalStateException ignore) {
            // Config not loaded yet; keep defaults.
        }
    }

    /**
     * 重置所有状态数据
     * 在关闭界面时调用，防止状态持久化导致的问题
     */
    public void reset() {
        // 重置鼠标数据
        mouseData.setTransformedMouseX(0d);
        mouseData.setTransformedMouseY(0d);
        mouseData.setOffsetX(0d);
        mouseData.setOffsetY(0d);
        mouseData.setScale(1.0f);
        mouseData.setDragStartX(0d);
        mouseData.setDragStartY(0d);
        mouseData.setDragTotal(0d);
        mouseData.setCanDrag(false);

        // 重置屏幕数据
        screenData.setScale(1.0f);
        screenData.setOpenRecipe(false);
        screenData.setGuiLeft(0);
        screenData.setGuiTop(0);
    }




//    public boolean isMouseInRecipePage(){
//
//    }


}
