package org.research.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix4f;

/**
 * 处理鼠标坐标变换的数据类
 * 封装了所有与矩阵变换相关的计算逻辑，避免在Screen类中直接处理坐标转换
 */
public class MouseHandleBgData {

    public static final MouseHandleBgData EMPTY = new MouseHandleBgData();

    // 变换矩阵（用于高级场景，目前使用直接计算）
    private Matrix4f matrix = new Matrix4f();

    // 变换参数
    private float offsetX = 0;  // 拖拽偏移X
    private float offsetY = 0;  // 拖拽偏移Y
    private float scrollOffs = 1.0f;  // 缩放比例

    // 转换后的鼠标坐标（世界空间）
    private int transformedMouseX = 0;
    private int transformedMouseY = 0;

    // 窗口信息（用于计算缩放中心）
    private int centerX = 0;
    private int centerY = 0;

    // 缩放限制
    private float minScale = 0.5f;
    private float maxScale = 2.0f;

    /**
     * 设置窗口中心点（缩放中心）
     */
    public void setCenter(int centerX, int centerY) {
        this.centerX = centerX;
        this.centerY = centerY;
    }

    /**
     * 设置缩放范围限制
     */
    public void setScaleRange(float minScale, float maxScale) {
        this.minScale = minScale;
        this.maxScale = maxScale;
    }

    /**
     * 处理鼠标滚轮缩放
     *
     * @param mouseX 屏幕空间鼠标X
     * @param mouseY 屏幕空间鼠标Y
     * @param delta 滚轮增量
     * @return 是否处理了该事件
     */
    public boolean handleMouseScroll(double mouseX, double mouseY, double delta) {
        // 计算新的缩放值
        float oldScale = this.scrollOffs;
        float newScale = oldScale + (float)(delta * 0.1);

        // 限制缩放范围
        newScale = Math.max(minScale, Math.min(maxScale, newScale));

        if (newScale == oldScale) {
            return true;  // 缩放值没有变化
        }

        // 计算鼠标相对于当前变换后中心点的偏移（考虑当前的偏移量）
        // 这里需要计算在当前变换下，鼠标指向的"世界空间"位置
        float worldMouseX = (float)((mouseX - offsetX - centerX) / oldScale + centerX);
        float worldMouseY = (float)((mouseY - offsetY - centerY) / oldScale + centerY);

        // 调整偏移量，使得鼠标指向的世界空间位置在缩放后保持在屏幕上的相同位置
        // 新的屏幕位置 = offsetX + (worldPos - centerX) * newScale + centerX
        // 我们希望新的屏幕位置 = mouseX
        // 所以: offsetX_new = mouseX - (worldMouseX - centerX) * newScale - centerX
        this.offsetX = (float)(mouseX - (worldMouseX - centerX) * newScale - centerX);
        this.offsetY = (float)(mouseY - (worldMouseY - centerY) * newScale - centerY);

        // 更新缩放值
        this.scrollOffs = newScale;

        // 更新当前鼠标的转换坐标
        updateTransformedMouseCoords(mouseX, mouseY);

        return true;
    }

    /**
     * 处理鼠标拖拽
     *
     * @param dragX X方向拖拽增量
     * @param dragY Y方向拖拽增量
     */
    public void handleMouseDragged(double dragX, double dragY) {
        // 累加偏移量，方向取反（向右拖动需要减少偏移让图片右移）
        // 除以 scrollOffs 是为了在不同缩放级别下保持一致的拖拽速度
        this.offsetX += (float)dragX / scrollOffs;
        this.offsetY += (float)dragY / scrollOffs;
    }

    /**
     * 将屏幕空间的鼠标坐标转换为变换后的世界空间坐标
     *
     * 渲染时的正向变换：
     * point' = T1 * T2 * S * T3 * point
     * 其中：
     *   T1 = translate(offsetX, offsetY, 0)
     *   T2 = translate(centerX, centerY, 0)
     *   S  = scale(scrollOffs, scrollOffs, 1)
     *   T3 = translate(-centerX, -centerY, 0)
     *
     * 逆变换（从右到左应用逆矩阵）：
     * point = T3^-1 * S^-1 * T2^-1 * T1^-1 * point'
     *
     * @param screenX 屏幕空间的X坐标（point'）
     * @param screenY 屏幕空间的Y坐标（point'）
     */
    public void updateTransformedMouseCoords(double screenX, double screenY) {
        // 开始逆变换（从外到内，逆序撤销）
        float tempMouseX = (float)screenX;
        float tempMouseY = (float)screenY;

        // T1^-1: 撤销 translate(offsetX, offsetY, 0)
        tempMouseX -= offsetX;
        tempMouseY -= offsetY;

        // T2^-1: 撤销 translate(centerX, centerY, 0)
        tempMouseX -= centerX;
        tempMouseY -= centerY;

        // S^-1: 撤销 scale(scrollOffs, scrollOffs, 1)
        tempMouseX /= scrollOffs;
        tempMouseY /= scrollOffs;

        // T3^-1: 撤销 translate(-centerX, -centerY, 0)
        tempMouseX += centerX;
        tempMouseY += centerY;

        // 保存转换后的坐标（这就是世界空间坐标）
        this.transformedMouseX = (int) tempMouseX;
        this.transformedMouseY = (int) tempMouseY;
    }

    /**
     * 应用变换到PoseStack
     * 在渲染时调用，应用所有变换
     *
     * @param pose PoseStack对象
     */
    public void applyTransform(PoseStack pose) {
        // 先应用拖拽偏移
        pose.translate(offsetX, offsetY, 0);

        // 然后应用缩放（以窗口中心为缩放中心）
        pose.translate(centerX, centerY, 0);
        pose.scale(scrollOffs, scrollOffs, 1.0f);
        pose.translate(-centerX, -centerY, 0);
    }

    // Getters

    public int getTransformedMouseX() {
        return transformedMouseX;
    }

    public int getTransformedMouseY() {
        return transformedMouseY;
    }

    public float getOffsetX() {
        return offsetX;
    }

    public float getOffsetY() {
        return offsetY;
    }

    public float getScrollOffs() {
        return scrollOffs;
    }

    public Matrix4f getMatrix() {
        return matrix;
    }

    // Setters（用于初始化或重置）

    public void setOffset(float offsetX, float offsetY) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    public void setScrollOffs(float scrollOffs) {
        this.scrollOffs = Math.max(minScale, Math.min(maxScale, scrollOffs));
    }

    /**
     * 重置所有变换参数
     */
    public void reset() {
        this.offsetX = 0;
        this.offsetY = 0;
        this.scrollOffs = 1.0f;
        this.transformedMouseX = 0;
        this.transformedMouseY = 0;
    }
}
