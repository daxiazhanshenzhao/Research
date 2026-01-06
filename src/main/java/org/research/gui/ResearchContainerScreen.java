package org.research.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.advancements.AdvancementWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;
import org.research.api.client.ClientResearchData;
import org.research.api.tech.AbstractTech;
import org.research.api.tech.SyncData;
import org.research.api.tech.TechInstance;
import org.research.api.tech.graphTree.Vec2i;
import org.research.api.util.BlitContext;
import org.research.gui.component.TechSlot;

import java.util.HashMap;
import java.util.Map;
import org.joml.Math;
//AdvancementsScreen
//AdvancementWidget
public abstract class ResearchContainerScreen extends Screen {
    //data
    private SyncData data;

    @Deprecated
    private Map<ResourceLocation,TechSlot> slots = new HashMap<>();

    //render

    private float scrollOffs = 1f;  //缩放率，默认1f

    private boolean isDragging = false;

    private float offsetX = 0, offsetY = 0;  // 拖拽偏移量
    private int guiLeft,guiTop;

    // 缓存转换后的鼠标坐标，用于所有鼠标事件
    private int transformedMouseX = 0;
    private int transformedMouseY = 0;

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

            var slot = new TechSlot(vec.x,vec.y,instance,this);
            slots.put(instance.getIdentifier(),slot);
            addRenderableWidget(slot);
        }
        super.init();

    }



    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.guiLeft = (this.width-256)/2;
        this.guiTop = (this.height-256)/2;

        // 1. 渲染固定的边框背景（不缩放）
        renderBg(guiGraphics, guiLeft, guiTop);

        // 2. 渲染需要缩放的内容（内部背景和槽位）
        renderInside(guiGraphics, mouseX, mouseY, partialTick);

        // 注意：不调用 super.render()，避免重复渲染槽位
        // super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void tick() {

//        Research.LOGGER.info(String.valueOf(this.scrollOffs));
        handleCenter();
    }

    private void handleCenter() {


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
    private void updateTransformedMouseCoords(double screenX, double screenY) {
        // 获取窗口信息
        var windowContext = getWindow();
        int windowX = guiLeft + windowContext.u();
        int windowY = guiTop + windowContext.v();
        int windowWidth = windowContext.width();
        int windowHeight = windowContext.height();

        // 计算窗口中心点（缩放中心）
        int centerX = windowX + windowWidth / 2;
        int centerY = windowY + windowHeight / 2;

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


    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        // 获取窗口区域信息
        var windowContext = getWindow();
        int windowX = guiLeft + windowContext.u();
        int windowY = guiTop + windowContext.v();
        int windowWidth = windowContext.width();
        int windowHeight = windowContext.height();
        
        // 检查鼠标是否在窗口区域内
        if (mouseX < windowX || mouseX > windowX + windowWidth || 
            mouseY < windowY || mouseY > windowY + windowHeight) {
            return false;  // 鼠标不在窗口区域内，不处理缩放
        }
        
        // 计算新的缩放值
        float oldScale = this.scrollOffs;
        float newScale = oldScale + (float)(delta * 0.1);
        
        // 限制缩放范围
        newScale = Math.clamp(getMaxOrMinScale().getFirst(), getMaxOrMinScale().getSecond(), newScale);
        
        if (newScale == oldScale) {
            return true;  // 缩放值没有变化，直接返回
        }
        
        // 计算窗口中心点
        int centerX = windowX + windowWidth / 2;
        int centerY = windowY + windowHeight / 2;
        
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

        return true;
    }


    private int dragTotal = 0;
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_1){
            dragTotal += Math.abs(dragX);
            dragTotal += Math.abs(dragY);

            if (dragTotal >= 2){
                isDragging = true;

                // 累加偏移量，方向取反（向右拖动需要减少偏移让图片右移）
                // 除以 scrollOffs 是为了在不同缩放级别下保持一致的拖拽速度
                this.offsetX += (float)dragX / scrollOffs;
                this.offsetY += (float)dragY / scrollOffs;
            }
        }

        // 更新转换后的鼠标坐标
        updateTransformedMouseCoords(mouseX, mouseY);

        // 将事件传递给子组件
        for (var child : children()) {
            if (child.mouseDragged(transformedMouseX, transformedMouseY, button, dragX, dragY)) {
                return true;
            }
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // 更新转换后的鼠标坐标
        updateTransformedMouseCoords(mouseX, mouseY);

        // 如果正在拖拽，不处理点击
        if (isDragging) {
            return false;
        }

        // 将事件传递给子组件
        for (var child : children()) {
            if (child.mouseClicked(transformedMouseX, transformedMouseY, button)) {
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        // 更新转换后的鼠标坐标
        updateTransformedMouseCoords(mouseX, mouseY);

        // 重置拖拽状态
        if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
            isDragging = false;
            dragTotal = 0;
        }

        // 将事件传递给子组件
        for (var child : children()) {
            if (child.mouseReleased(transformedMouseX, transformedMouseY, button)) {
                return true;
            }
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        // 更新转换后的鼠标坐标
        updateTransformedMouseCoords(mouseX, mouseY);

        super.mouseMoved(mouseX, mouseY);
    }

    /**
     * @return 边框图片和整个大小
     */
    protected abstract BlitContext getBg();

    /**
     * @return 边框厚度围成的内部区域,
     * {@link BlitContext#texture()} 为null
     */
    protected abstract BlitContext getWindow();

    /**
     * @return 内部图片和整张图片的大小
     */
    protected abstract BlitContext getInside();

    /**
     *
     * @return 获取最大的放大率和最小放大率
     */
    protected abstract Pair<Float,Float> getMaxOrMinScale();

    /**
     * 计算当前实际缩放比例
     */
    private void setScale(float scrollOffs) {
        this.scrollOffs = Math.clamp(getMaxOrMinScale().getFirst(),getMaxOrMinScale().getSecond(),scrollOffs);
    }

    private void renderBg(GuiGraphics context,int x,int y){
        var blitContext = getBg();
        context.blit(blitContext.texture(),x,y,blitContext.u(),blitContext.v(),blitContext.width(),blitContext.height());
    }

    private void renderInside(GuiGraphics context, int mouseX, int mouseY, float partialTick){
        // 获取内部背景信息
        var insideContext = getInside();
        if (insideContext == null || insideContext.texture() == null) {
            return;  // 如果没有内部背景，直接返回
        }

        // 获取窗口信息（边框厚度围成的内部区域）
        var windowContext = getWindow();

        // 计算窗口区域的实际位置和中心点
        int windowX = guiLeft + windowContext.u();
        int windowY = guiTop + windowContext.v();
        int windowWidth = windowContext.width();
        int windowHeight = windowContext.height();

        // 计算窗口中心点作为缩放中心
        int centerX = windowX + windowWidth / 2;
        int centerY = windowY + windowHeight / 2;

        // 计算实际缩放值
        float scale = this.scrollOffs;

        // 启用裁剪，只在 window 区域内渲染
        context.enableScissor(windowX, windowY, windowX + windowWidth, windowY + windowHeight);

        // 应用矩阵变换
        PoseStack pose = context.pose();
        pose.pushPose();

        // 先应用拖拽偏移
        pose.translate(offsetX, offsetY, 0);

        // 然后应用缩放（以窗口中心为缩放中心）
        pose.translate(centerX, centerY, 0);
        pose.scale(scale, scale, 1.0f);
        pose.translate(-centerX, -centerY, 0);

        // 计算内部背景的渲染位置（在窗口区域居中）
        int insideX = centerX - insideContext.width() / 2;
        int insideY = centerY - insideContext.height() / 2;

        // 渲染内部背景
        context.blit(
            insideContext.texture(),
            insideX, insideY,
            insideContext.u(), insideContext.v(),
            insideContext.width(), insideContext.height(),
            insideContext.width(), insideContext.height()
        );

        // 更新转换后的鼠标坐标（用于渲染时的hover效果）
        updateTransformedMouseCoords(mouseX, mouseY);

        // 在变换后的坐标系中渲染所有槽位，使用转换后的鼠标坐标
        for (var renderable : renderables) {
            renderable.render(context, transformedMouseX, transformedMouseY, partialTick);
        }


        pose.popPose();
        context.disableScissor();
    }

    public void getGuiContext(PoseStack context){

    }

    public void focus(AbstractTech tech){

    }

}
