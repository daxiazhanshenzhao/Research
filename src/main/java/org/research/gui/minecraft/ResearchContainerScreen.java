package org.research.gui.minecraft;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;
import org.research.api.client.ClientResearchData;
import org.research.api.gui.MouseHandleBgData;
import org.research.api.init.PacketInit;
import org.research.api.tech.*;
import org.research.api.tech.graphTree.Vec2i;
import org.research.api.util.BlitContext;
import org.research.gui.minecraft.component.IOpenRenderable;
import org.research.gui.minecraft.component.TechSlot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.joml.Math;
import org.research.network.research.ClientClearFocusPacket;
import org.research.network.research.ClientSetFocusPacket;

//AdvancementsScreen
//AdvancementWidget

public abstract class ResearchContainerScreen extends Screen {
    //data
    private SyncData data;


    private Map<ResourceLocation,TechSlot> slots = new HashMap<>();

    /**
     * 双重focus机制说明：
     *
     * 1. 客户端focus（focusSlot + TechSlot.clientFocus）：
     *    - 用于UI显示和查看信息
     *    - 任何状态的科技都可以设置
     *    - 点击任何槽位都会设置客户端focus
     *    - 显示FOCUS边框和黑色背景
     *
     * 2. 服务端focus（TechInstance.isFocused）：
     *    - 用于追踪状态（游戏机制）
     *    - 只有非LOCKED状态的科技可以设置
     *    - 点击LOCKED槽位时不会设置服务端focus
     *    - 服务端会拒绝LOCKED状态的focus请求
     *
     * 3. 不同步的情况：
     *    - 点击LOCKED槽位时，客户端focus会设置，但服务端focus不会设置
     *    - 此时客户端可以查看LOCKED科技的信息，但不会追踪它
     *
     * 4. 双击取消：
     *    - 双击任何已focus的槽位会同时取消客户端和服务端的focus状态
     */
    private TechSlot focusSlot = TechSlot.EMPTY;
    //render

    private MouseHandleBgData mouseHandleBgData = new MouseHandleBgData();

    private boolean isDragging = false;

    private int guiLeft,guiTop;


    private int openTicks = 0;

    // 用于检测数据变化的哈希值
    private int lastDataHash = 0;



    protected ResearchContainerScreen(SyncData data) {
        super(Component.empty());
        this.data = data;
        this.lastDataHash = data.getDataHash(); // 初始化哈希值
        init();
    }

    @Override
    protected void init() {
        // 首先调用父类的init()，清空之前的widgets
        super.init();

        // 获取窗口信息，计算内容区域的中心点
        var windowContext = getWindow();
        var insideContext = getInside();
        int windowX = (this.width - 256) / 2 + windowContext.u();
        int windowY = (this.height - 256) / 2 + windowContext.v();
        int centerX = windowX + windowContext.width() / 2;
        int centerY = windowY + windowContext.height() / 2;

        // 初始化MouseHandleBgData
        this.mouseHandleBgData = new MouseHandleBgData();
        this.mouseHandleBgData.setCenter(centerX, centerY);
        var scaleRange = getMaxOrMinScale();
        this.mouseHandleBgData.setScaleRange(scaleRange.getFirst(), scaleRange.getSecond());

        // 设置移动边界信息
        this.mouseHandleBgData.setBoundaryDimensions(
            insideContext.width(),
            insideContext.height(),
            windowContext.width(),
            windowContext.height()
        );
        // 设置边界余量（可以调整这个值来改变允许移出的距离）
        this.mouseHandleBgData.setBoundaryMarginRatio(0.1f);  // 30%的余量

        //科技槽位
        var insList = data.getCacheds();
        var vecList = data.getVecMap();


        // 计算内部背景的起始位置
        int insideX = centerX - insideContext.width() / 2;
        int insideY = centerY - insideContext.height() / 2;

        for (TechInstance instance : insList.values()) {
            var vec = vecList.getOrDefault(instance.getIdentifier(), Vec2i.EMPTY);

            // 将槽位坐标相对于内部背景的左上角定位
            var slot = new TechSlot(insideX + vec.x, insideY + vec.y, instance, this);
            slots.put(instance.getIdentifier(),slot);
            // 使用 addWidget 而不是 addRenderableWidget，避免自动渲染
            // 槽位将在 renderInside() 中手动渲染
            addWidget(slot);
        }

    }



    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.guiLeft = (this.width-256)/2;
        this.guiTop = (this.height-256)/2;


        updateSlotPositions();
        renderBg(guiGraphics, guiLeft, guiTop);

        renderInside(guiGraphics, mouseX, mouseY, partialTick);

        // 3. 渲染子组件，但过滤掉IOpenRenderable组件（如果配方书未打开）
        renderFilteredChildren(guiGraphics, mouseX, mouseY, partialTick);
    }

    /**
     * 渲染子组件，根据isOpenRecipeBook()状态过滤IOpenRenderable组件
     */
    protected void renderFilteredChildren(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        for (var renderable : this.renderables) {
            // 如果是IOpenRenderable接口的实现，只在配方书打开时渲染
            if (renderable instanceof IOpenRenderable) {
                if (isOpenRecipeBook()) {
                    renderable.render(guiGraphics, mouseX, mouseY, partialTick);
                }
            } else {
                // 其他组件正常渲染
                renderable.render(guiGraphics, mouseX, mouseY, partialTick);
            }
        }
    }

    /**
     * 子类需要实现此方法，返回配方书是否打开
     */
    protected abstract boolean isOpenRecipeBook();

    @Override
    public void tick() {
        openTicks++;

        // 自动检测 SyncData 变化
        checkAndUpdateData();
    }



    /**
     * 更新所有槽位的数据（不重新创建槽位）
     */
    private void updateSlotsData() {
        var insList = data.getCacheds();

        // 更新现有槽位的数据
        for (var entry : slots.entrySet()) {
            ResourceLocation id = entry.getKey();
            TechSlot slot = entry.getValue();

            // 如果该槽位对应的实例还存在，更新其数据
            if (insList.containsKey(id)) {
                slot.updateInstance(insList.get(id));
            }
        }

        // 如果有新的科技实例，添加它们
        for (var entry : insList.entrySet()) {
            if (!slots.containsKey(entry.getKey())) {
                var windowContext = getWindow();
                var insideContext = getInside();
                int windowX = (this.width - 256) / 2 + windowContext.u();
                int windowY = (this.height - 256) / 2 + windowContext.v();
                int centerX = windowX + windowContext.width() / 2;
                int centerY = windowY + windowContext.height() / 2;
                int insideX = centerX - insideContext.width() / 2;
                int insideY = centerY - insideContext.height() / 2;

                var vec = data.getVecMap().getOrDefault(entry.getKey(), Vec2i.EMPTY);
                var slot = new TechSlot(insideX + vec.x, insideY + vec.y, entry.getValue(), this);
                slots.put(entry.getKey(), slot);
                addWidget(slot);

            }
        }
    }

    private void handleCenter() {


    }

    /**
     * 更新所有槽位的位置，确保它们与内部背景对齐
     */
    private void updateSlotPositions() {
        // 获取窗口信息，计算内容区域的中心点
        var windowContext = getWindow();
        var insideContext = getInside();
        int windowX = guiLeft + windowContext.u();
        int windowY = guiTop + windowContext.v();
        int centerX = windowX + windowContext.width() / 2;
        int centerY = windowY + windowContext.height() / 2;

        // 计算内部背景的起始位置
        int insideX = centerX - insideContext.width() / 2;
        int insideY = centerY - insideContext.height() / 2;

        // 更新每个槽位的位置
        var vecList = data.getVecMap();
        for (var entry : slots.entrySet()) {
            var vec = vecList.getOrDefault(entry.getKey(), Vec2i.EMPTY);
            var slot = entry.getValue();
            // 设置槽位相对于内部背景的位置
            slot.setX(insideX + vec.x);
            slot.setY(insideY + vec.y);
        }
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
        
        // 使用MouseHandleBgData处理滚轮缩放
        return mouseHandleBgData.handleMouseScroll(mouseX, mouseY, delta);
    }


    private int dragTotal = 0;
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_1){
            dragTotal += Math.abs(dragX);
            dragTotal += Math.abs(dragY);

            if (dragTotal >= 1){
                isDragging = true;

                // 使用MouseHandleBgData处理拖拽
                mouseHandleBgData.handleMouseDragged(dragX, dragY);
            }
        }

        // 更新转换后的鼠标坐标
        mouseHandleBgData.updateTransformedMouseCoords(mouseX, mouseY);

        // 获取转换后的鼠标坐标
        int transformedMouseX = mouseHandleBgData.getTransformedMouseX();
        int transformedMouseY = mouseHandleBgData.getTransformedMouseY();

        for (var child : List.copyOf(children())) {
            // 如果是IOpenRenderable组件，只在配方书打开时才处理事件
            if (child instanceof IOpenRenderable && !isOpenRecipeBook()) {
                continue;
            }

            // 根据组件类型使用不同的坐标
            if (child instanceof TechSlot) {
                // TechSlot 使用转换后的坐标（受缩放和平移影响）
                if (child.mouseDragged(transformedMouseX, transformedMouseY, button, dragX, dragY)) {
                    return true;
                }
            } else {
                // 其他 widget 使用原始坐标（不受变换影响）
                if (child.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
                    return true;
                }
            }
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // 如果正在拖拽，忽略点击事件
        if (isDragging) {
            return false;
        }

        // 更新转换后的鼠标坐标
        mouseHandleBgData.updateTransformedMouseCoords(mouseX, mouseY);

        // 获取转换后的鼠标坐标
        int transformedMouseX = mouseHandleBgData.getTransformedMouseX();
        int transformedMouseY = mouseHandleBgData.getTransformedMouseY();

        // 只对鼠标坐标范围内的子组件触发点击事件
        for (var child : List.copyOf(children())) {
            // 如果是IOpenRenderable组件，只在配方书打开时才处理事件
            if (child instanceof IOpenRenderable) {
                if (!isOpenRecipeBook()) {
                    System.out.println("Skipping IOpenRenderable (recipe book closed): " + child.getClass().getSimpleName());
                    continue;  // 跳过此组件
                } else {
                    System.out.println("Processing IOpenRenderable (recipe book open): " + child.getClass().getSimpleName() +
                                     " at (" + mouseX + ", " + mouseY + ")");
                }
            }

            // 根据组件类型使用不同的坐标和判断逻辑
            if (child instanceof TechSlot) {
                // TechSlot 使用转换后的坐标
                if (child.isMouseOver(transformedMouseX, transformedMouseY)) {
                    if (child.mouseClicked(transformedMouseX, transformedMouseY, button)) {
                        return true;
                    }
                }
            } else {
                // 其他 widget 使用原始坐标
                if (child.isMouseOver(mouseX, mouseY)) {
                    System.out.println("Widget " + child.getClass().getSimpleName() + " isMouseOver=true");
                    if (child.mouseClicked(mouseX, mouseY, button)) {
                        System.out.println("Widget " + child.getClass().getSimpleName() + " handled click");
                        return true;
                    }
                }
            }
        }

        // 如果没有子组件处理事件，调用父类方法以确保正常的事件处理流程
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        // 更新转换后的鼠标坐标
        mouseHandleBgData.updateTransformedMouseCoords(mouseX, mouseY);

        // 重置拖拽状态
        if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
            isDragging = false;
            dragTotal = 0;
        }

        // 获取转换后的鼠标坐标
        int transformedMouseX = mouseHandleBgData.getTransformedMouseX();
        int transformedMouseY = mouseHandleBgData.getTransformedMouseY();

        // 将事件传递给子组件
        for (var child : children()) {
            // 如果是IOpenRenderable组件，只在配方书打开时才处理事件
            if (child instanceof IOpenRenderable && !isOpenRecipeBook()) {
                continue;  // 跳过此组件
            }

            // 根据组件类型使用不同的坐标
            if (child instanceof TechSlot) {
                // TechSlot 使用转换后的坐标
                if (child.mouseReleased(transformedMouseX, transformedMouseY, button)) {
                    return true;
                }
            } else {
                // 其他 widget 使用原始坐标
                if (child.mouseReleased(mouseX, mouseY, button)) {
                    return true;
                }
            }
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // 先让子组件处理键盘事件
        for (var child : children()) {
            // 如果是IOpenRenderable组件，只在配方书打开时才处理事件
            if (child instanceof IOpenRenderable && !isOpenRecipeBook()) {
                continue;  // 跳过此组件
            }

            if (child.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        // 先让子组件处理字符输入事件
        for (var child : children()) {
            // 如果是IOpenRenderable组件，只在配方书打开时才处理事件
            if (child instanceof IOpenRenderable && !isOpenRecipeBook()) {
                continue;  // 跳过此组件
            }

            if (child.charTyped(codePoint, modifiers)) {
                return true;
            }
        }

        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        // 更新转换后的鼠标坐标
        mouseHandleBgData.updateTransformedMouseCoords(mouseX, mouseY);
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

        // 启用裁剪，只在 window 区域内渲染
        context.enableScissor(windowX, windowY, windowX + windowWidth, windowY + windowHeight);

        // 应用矩阵变换
        PoseStack pose = context.pose();
        pose.pushPose();

        // 使用MouseHandleBgData应用变换
        mouseHandleBgData.applyTransform(pose);

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
        mouseHandleBgData.updateTransformedMouseCoords(mouseX, mouseY);

        // 获取转换后的鼠标坐标
        int transformedMouseX = mouseHandleBgData.getTransformedMouseX();
        int transformedMouseY = mouseHandleBgData.getTransformedMouseY();

        // 在变换后的坐标系中渲染所有槽位，使用转换后的鼠标坐标
        for (var slot : slots.values()) {
            slot.render(context, transformedMouseX, transformedMouseY, partialTick);
        }
        pose.popPose();
        context.disableScissor();
    }

    /**
     * 获取当前客户端focus的槽位
     * @return 当前focus的槽位，如果没有则返回null
     */
    public TechSlot getFocusSlot() {
        return this.focusSlot;
    }

    public void setFocusSlot(TechSlot focusSlot) {
        this.focusSlot = focusSlot;
    }

    public Map<ResourceLocation, TechSlot> getSlots() {
        return slots;
    }

    /**
     * 清除双端focus状态（通过双击槽位触发）
     * 同时清除客户端focus和服务端focus
     */
    public void clearFocus(ResourceLocation techId) {
        if (this.focusSlot != TechSlot.EMPTY) {

        }
        this.focusSlot = TechSlot.EMPTY;
        PacketInit.sendToServer(new ClientClearFocusPacket());
    }


    public void focus(ResourceLocation techId, boolean toServer) {
        if (!slots.containsKey(techId)) {
            return;
        }

        TechSlot slot = slots.get(techId);
        this.focusSlot = slot;
        if (toServer){
            PacketInit.sendToServer(new ClientSetFocusPacket(techId));
        }
    }

    public int getOpenTicks() {
        return openTicks;
    }


    private boolean isRunning = false;
    /**
     * 强制同步数据（忽略哈希值检查）
     */
    public void syncData(){
        // 防止递归调用
        if (isRunning) {
            return;
        }
        isRunning = true;
        try {
            doSyncData(false);
        } finally {
            isRunning = false;
        }
    }

    /**
     * 检查客户端缓存的 SyncData 是否发生变化，如果变化则自动更新
     */
    private void checkAndUpdateData() {
        doSyncData(true);
    }

    /**
     * 执行数据同步的核心逻辑
     * @param checkHash 是否检查哈希值（true=仅在数据变化时更新，false=强制更新）
     */
    private void doSyncData(boolean checkHash) {
        // 从客户端缓存获取最新数据
        SyncData latestData = ClientResearchData.getSyncData();

        // 如果是空数据，跳过
        if (latestData.getPlayerId() == -999) {
            return;
        }

        // 计算最新数据的哈希值
        int newHash = latestData.getDataHash();

        // 如果需要检查哈希值且数据没有变化，则跳过更新
        if (checkHash && newHash == lastDataHash) {
            return;
        }

        // 更新数据和哈希值
        this.data = latestData;
        this.lastDataHash = newHash;

        // 更新槽位数据
        updateSlotsData();
    }

    public int getGuiLeft() {
        return guiLeft;
    }

    public int getGuiTop() {
        return guiTop;
    }
}
