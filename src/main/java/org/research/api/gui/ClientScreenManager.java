package org.research.api.gui;

import lombok.Getter;
import lombok.Setter;
import org.research.Research;
import org.research.api.client.ClientResearchData;
import org.research.api.config.ClientConfig;
import org.research.api.gui.wrapper.*;
import org.research.api.init.PacketInit;
import org.research.api.tech.SyncData;
import org.research.api.tech.TechInstance;
import org.research.api.util.BlitContextV2;
import org.research.api.util.UVContext;
import org.research.gui.minecraft.component.TechSlot;
import org.research.network.research.ClientSetFocusPacket;

import java.util.Optional;

@Getter
@Setter
public class ClientScreenManager {

    private PoseStackData poseStackData = new PoseStackData();
    private ScreenConfigData screenConfigData = new ScreenConfigData(
            0.5d, 2.0d,
            new BlitContextV2(Research.asResource("textures/gui/background.png"), 0, 0, 1024, 1024, 1024, 1024),
            new BlitContextV2(Research.asResource("textures/gui/window.png"), 0, 0, 256, 256, 256, 256),
            new UVContext(15, 28, 226, 186), 0.8d
    );

    private ScreenData screenData = new ScreenData();
    private MouseData mouseData = new MouseData();
    private TechSlotData techSlotData;
    private SyncData lastSyncData;
    private Optional<RecipeTechData> optRecipeTechData = Optional.empty();


    // 拖拽距离阈值（像素）- 防止小的手抖被误认为拖拽
    private static final double DRAG_THRESHOLD = 2.0;

    public ClientScreenManager() {
        refreshConfigSafely();
    }

    // ==================== 基础工具方法 ====================

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private int getCenterX() {
        return screenData.getInsideX() + screenConfigData.insideUV().width() / 2;
    }

    private int getCenterY() {
        return screenData.getInsideY() + screenConfigData.insideUV().height() / 2;
    }

    private boolean isMouseInRect(double x, double y, int left, int top, int w, int h) {
        return x >= left && x < left + w && y >= top && y < top + h;
    }

    // ==================== 坐标转换核心 ====================

    /**
     * 执行逆变换：屏幕坐标 -> 世界坐标
     */
    private double[] inverseTransform(double screenX, double screenY) {
        int cx = getCenterX(), cy = getCenterY();
        double x = screenX - mouseData.getOffsetX() - cx;
        double y = screenY - mouseData.getOffsetY() - cy;

        float scale = mouseData.getScale();
        if (Math.abs(scale) > 0.001f) {
            x /= scale;
            y /= scale;
        }

        return new double[]{x + cx, y + cy};
    }

    /**
     * 执行正向变换：世界坐标 -> 屏幕坐标
     */
    private double[] forwardTransform(double worldX, double worldY) {
        int cx = getCenterX(), cy = getCenterY();
        float scale = mouseData.getScale();
        double x = (worldX - cx) * scale + cx + mouseData.getOffsetX();
        double y = (worldY - cy) * scale + cy + mouseData.getOffsetY();
        return new double[]{x, y};
    }

    /**
     * 更新转换后的鼠标坐标
     */
    private void updateTransformedMouseCoords(double screenX, double screenY) {
        double[] result = inverseTransform(screenX, screenY);
        mouseData.setTransformedMouseX(result[0]);
        mouseData.setTransformedMouseY(result[1]);
    }

    // ==================== 缩放和拖拽 ====================

    /**
     * 滚轮缩放界面（随鼠标中心缩放）
     */
    public boolean handleMouseScrolled(double mouseX, double mouseY, double delta) {
        if (!isMouseInSide(mouseX, mouseY)) {
            return false;
        }

        float oldScale = mouseData.getScale();
        float newScale = (float) clamp(oldScale + delta * 0.1, screenConfigData.minScale(), screenConfigData.maxScale());

        if (Math.abs(newScale - oldScale) < 0.001f) {
            return true;
        }

        int cx = getCenterX(), cy = getCenterY();
        double worldX = (mouseX - mouseData.getOffsetX() - cx) / oldScale + cx;
        double worldY = (mouseY - mouseData.getOffsetY() - cy) / oldScale + cy;

        mouseData.setScale(newScale);
        mouseData.setOffsetX(mouseX - (worldX - cx) * newScale - cx);
        mouseData.setOffsetY(mouseY - (worldY - cy) * newScale - cy);

        clampOffset();
        updateTransformedMouseCoords(mouseX, mouseY);
        return true;
    }

    /**
     * 限制偏移量在合理范围内
     */
    private void clampOffset() {
        var bgCtx = screenConfigData.backGround();
        var insideUV = screenConfigData.insideUV();
        int bgW = bgCtx.width(), bgH = bgCtx.height();
        int winW = insideUV.width(), winH = insideUV.height();

        if (bgW <= 0 || bgH <= 0 || winW <= 0 || winH <= 0) {
            return;
        }

        float scale = mouseData.getScale();
        int halfW = winW / 2, halfH = winH / 2;
        int guiL = screenData.getGuiLeft(), guiT = screenData.getGuiTop();
        int insideL = screenData.getInsideX(), insideT = screenData.getInsideY();

        int bgMinX = guiL - (insideL + halfW), bgMaxX = bgMinX + bgW;
        int bgMinY = guiT - (insideT + halfH), bgMaxY = bgMinY + bgH;

        double minOffsetX = Math.ceil(halfW - bgMaxX * scale);
        double maxOffsetX = Math.floor(-halfW - bgMinX * scale);
        double minOffsetY = Math.ceil(halfH - bgMaxY * scale);
        double maxOffsetY = Math.floor(-halfH - bgMinY * scale);

        if (bgW * scale <= winW) {
            minOffsetX = maxOffsetX = 0;
        }
        if (bgH * scale <= winH) {
            minOffsetY = maxOffsetY = 0;
        }

        mouseData.setOffsetX(clamp(mouseData.getOffsetX(), minOffsetX, maxOffsetX));
        mouseData.setOffsetY(clamp(mouseData.getOffsetY(), minOffsetY, maxOffsetY));
    }

    /**
     * 处理鼠标拖拽平移
     *
     * 防误触逻辑：
     * - 检查 canDrag 标志（仅在鼠标在内容区域时为 true）
     * - 累计拖拽距离，防止小的手抖被误认为拖拽
     * - 超过 DRAG_THRESHOLD 阈值后才真正移动内容
     * - 提高人体工学性能：给用户更多缓冲区间
     */
    public void handleMouseDrag(double mouseX, double mouseY, int button, double dragX, double dragY) {
        // 检查按下时是否允许拖拽
        if (!mouseData.isCanDrag()) {
            return;
        }

        // 累计拖拽距离（使用绝对值确保无论方向如何都累计）
        mouseData.setDragTotal(mouseData.getDragTotal() + Math.abs(dragX) + Math.abs(dragY));

        // 只有超过阈值才真正执行拖拽操作
        if (mouseData.getDragTotal() > DRAG_THRESHOLD) {
            float scale = mouseData.getScale();
            // 应用拖拽偏移
            mouseData.setOffsetX(mouseData.getOffsetX() + dragX / scale);
            mouseData.setOffsetY(mouseData.getOffsetY() + dragY / scale);

            // 限制偏移范围，防止拖出边界
            clampOffset();
        }

        // 始终更新转换后的鼠标坐标（用于 hover 检测等）
        updateTransformedMouseCoords(mouseX, mouseY);
    }

    // ==================== 鼠标区域检测 ====================

    /**
     * 检查鼠标是否在 GUI 窗口区域内
     */
    public boolean isMouseInGUI(double screenMouseX, double screenMouseY) {
        var window = screenConfigData.window();
        return isMouseInRect(screenMouseX, screenMouseY, screenData.getGuiLeft(), screenData.getGuiTop(),
                window.textureWidth(), window.textureHeight());
    }

    /**
     * 检查鼠标是否在内部区域内
     */
    public boolean isMouseInSide(double screenMouseX, double screenMouseY) {
        var insideUV = screenConfigData.insideUV();
        return isMouseInRect(screenMouseX, screenMouseY, screenData.getInsideX(), screenData.getInsideY(),
                insideUV.width(), insideUV.height());
    }

    /**
     * 查找鼠标悬停或点击的 TechSlot
     *
     * 使用转换后的世界坐标（内容坐标）进行命中检测
     * 从后向前遍历，确保获取最上层的槽位
     *
     * @param worldMouseX 转换后的世界坐标X（内容坐标）
     * @param worldMouseY 转换后的世界坐标Y（内容坐标）
     * @return 鼠标悬停的 TechSlot，如果没有则返回 TechSlot.EMPTY
     */
    public TechSlot findHoveredTechSlot(double worldMouseX, double worldMouseY) {
        if (techSlotData == null || techSlotData.isEmpty()) {
            return TechSlot.EMPTY;
        }

        var techSlots = techSlotData.getCachedTechSlots();
        // 从后向前遍历，确保点击最上层的槽位
        for (int i = techSlots.size() - 1; i >= 0; i--) {
            var slot = techSlots.get(i);
            if (slot.isMouseOver(worldMouseX, worldMouseY)) {
                return slot;
            }
        }
        return TechSlot.EMPTY;
    }

    /**
     * 查找鼠标悬停的 TechSlot（使用当前缓存的转换坐标）
     *
     * @return 鼠标悬停的 TechSlot，如果没有则返回 null
     */
    public TechSlot findHoveredTechSlot() {
        TechSlot slot = findHoveredTechSlot(mouseData.getTransformedMouseX(), mouseData.getTransformedMouseY());
        return slot == TechSlot.EMPTY ? null : slot;
    }

    // ==================== 公共接口 ====================

    public void handleMousePositon(double mouseX, double mouseY) {
        updateTransformedMouseCoords(mouseX, mouseY);
    }

    /**
     * 处理鼠标释放事件
     * - 若拖拽距离未超过阈值，认为是点击，发送焦点数据包到服务器
     * - 重置拖拽状态标志
     */
    public void handleMouseReleased(double mouseX, double mouseY, int button) {
        // 检查是否在内容区域内
        if (!isMouseInSide(mouseX, mouseY)) {
            mouseData.setDragTotal(0);
            mouseData.setCanDrag(false);
            return;
        }

        // 只有拖拽距离未超过阈值时，认为是点击操作，发送数据包
        if (mouseData.getDragTotal() <= DRAG_THRESHOLD) {
            double worldMouseX = mouseData.getTransformedMouseX();
            double worldMouseY = mouseData.getTransformedMouseY();

            // 查找被点击的 TechSlot
            TechSlot clickedSlot = findHoveredTechSlot(worldMouseX, worldMouseY);
            if (!clickedSlot.getTechInstance().isEmpty()) {
                // 发送焦点数据包到服务器
                sendFocusPacket(clickedSlot);
            }
        }

        // 重置拖拽状态
        mouseData.setDragTotal(0);
        mouseData.setCanDrag(false);
    }

    /**
     * 发送焦点数据包到服务器
     */
    private void sendFocusPacket(TechSlot slot) {
        PacketInit.sendToServer(new ClientSetFocusPacket(slot.getTechInstance().getIdentifier()));
    }

    /**
     * 处理鼠标点击事件
     * 初始化拖拽状态：记录起点、重置累计距离、标记是否允许拖拽
     * 同时检测是否点击了 TechSlot
     */
    public void handleMouseClick(double mouseX, double mouseY, int button) {
        // 检查鼠标是否在内部区域
        if (isMouseInSide(mouseX, mouseY)) {
            // 记录拖拽起点（屏幕坐标）
            mouseData.setDragStartX(mouseX);
            mouseData.setDragStartY(mouseY);

            // 重置累计拖拽距离
            mouseData.setDragTotal(0);

            // 标记允许拖拽
            mouseData.setCanDrag(true);

            // 检测点击的 TechSlot
            double worldMouseX = mouseData.getTransformedMouseX();
            double worldMouseY = mouseData.getTransformedMouseY();
            TechSlot clickedSlot = findHoveredTechSlot(worldMouseX, worldMouseY);
            if (!clickedSlot.getTechInstance().isEmpty()) {
                // 触发槽位的点击事件（会播放声音）
                clickedSlot.mouseClicked(worldMouseX, worldMouseY, button);
            }
        } else {
            // 不在内容区域，禁止拖拽
            mouseData.setCanDrag(false);
        }
    }

    public boolean translate(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return true;
    }

    /**
     * 应用变换到PoseStack
     */
    public void applyTransform() {
        if (poseStackData.getPoseStack() == null) {
            return;
        }

        int cx = getCenterX(), cy = getCenterY();
        poseStackData.translate((float) mouseData.getOffsetX(), (float) mouseData.getOffsetY(), 0);
        poseStackData.translate(cx, cy, 0);
        poseStackData.scale(mouseData.getScale(), mouseData.getScale(), 1.0f);
        poseStackData.translate(-cx, -cy, 0);
    }

    /**
     * 获取技能槽位数据，支持自动重新初始化和热更新
     *
     * 热更新逻辑：
     * - 每帧都强制更新所有 TechSlot 的 TechInstance，确保 focus 等状态实时同步
     * - 首次创建时初始化所有槽位
     */
    public TechSlotData getTechSlotData() {
        var currentSyncData = ClientResearchData.getSyncData();

        // 首次创建或需要完全重建
        if (techSlotData == null || techSlotData.isEmpty()) {
            techSlotData = new TechSlotData();
            lastSyncData = currentSyncData;

            var techs = currentSyncData.getCacheds();
            var vecs = currentSyncData.getVecMap();
            for (TechInstance tech : techs.values()) {
                var pos = vecs.get(tech.getIdentifier());
                if (pos != null) {
                    techSlotData.addTechSlot(new TechSlot(pos.x(), pos.y(), tech));
                }
            }
        } else {
            // 热更新：每帧强制更新所有 TechSlot 的 TechInstance
            // 这确保了 focus 等状态能实时反映到界面上
            var techs = currentSyncData.getCacheds();
            var existingSlots = new java.util.ArrayList<>(techSlotData.getCachedTechSlots());

            // 强制更新所有槽位的 TechInstance
            for (var slot : existingSlots) {
                var techId = slot.getTechInstance().getIdentifier();
                var updatedTech = techs.get(techId);
                if (updatedTech != null) {
                    // 直接更新，不做 equals 检查（因为 TechInstance.equals 只比较 tech 字段）
                    slot.updateInstance(updatedTech);
                }
            }

            lastSyncData = currentSyncData;
        }

        return techSlotData;
    }

    /**
     * 重置所有状态数据
     */
    public void reset() {
        mouseData.setTransformedMouseX(0d);
        mouseData.setTransformedMouseY(0d);
        mouseData.setDragStartX(0d);
        mouseData.setDragStartY(0d);
        mouseData.setDragTotal(0d);
        mouseData.setCanDrag(false);


        screenData.setOpenRecipe(false);
        screenData.setGuiLeft(0);
        screenData.setGuiTop(0);
    }

    /**
     * 验证坐标转换的正确性
     */
    public boolean validateCoordinateTransform(double worldX, double worldY) {
        double[] screenCoords = forwardTransform(worldX, worldY);
        double[] result = inverseTransform(screenCoords[0], screenCoords[1]);

        double tolerance = 0.01;
        return Math.abs(result[0] - worldX) < tolerance && Math.abs(result[1] - worldY) < tolerance;
    }

    private void refreshConfigSafely() {
        try {
            screenConfigData = new ScreenConfigData(
                    ClientConfig.MIN_SCALE.get(), ClientConfig.MAX_SCALE.get(),
                    ClientConfig.getBackgroundBlitContextV2(), ClientConfig.getWindowBlitContextV2(),
                    ClientConfig.getInsideUVContext(), ClientConfig.MOVABLE_AREA_RATIO.get()
            );
        } catch (IllegalStateException ignore) {
        }
    }


}
