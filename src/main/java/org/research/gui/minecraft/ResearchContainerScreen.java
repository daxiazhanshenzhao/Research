package org.research.gui.minecraft;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.advancements.AdvancementWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;
import org.research.api.client.ClientResearchData;
import org.research.api.gui.MouseHandleBgData;
import org.research.api.init.PacketInit;
import org.research.api.tech.*;
import org.research.api.tech.graphTree.Vec2i;
import org.research.api.util.BlitContext;
import org.research.api.util.Texture;
import org.research.gui.minecraft.component.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.joml.Math;
import org.research.network.research.ClientClearFocusPacket;
import org.research.network.research.ClientSetFocusPacket;

//AdvancementsScreen
//AdvancementWidget

@OnlyIn(value = Dist.CLIENT)
public abstract class ResearchContainerScreen extends Screen {
    //data
    private SyncData data;

    @Getter
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
     * -- GETTER --
     *  获取当前客户端focus的槽位
     *
     * @return 当前focus的槽位，如果没有则返回null

     */
    @Setter
    @Getter
    private TechSlot focusSlot = TechSlot.EMPTY;
    //render

    private MouseHandleBgData mouseHandleBgData = new MouseHandleBgData();

    private boolean isDragging = false;

    private int guiLeft,guiTop;

    private int openTicks = 0;

    // 用于检测数据变化的哈希值
    private int lastDataHash = 0;

    /**
     * -- GETTER --
     *  返回配方书是否打开
     */
    // ResearchScreen 特有的字段
    @Setter
    @Getter
    private boolean openRecipeBook = false;
    private static final BlitContext RECIPE_BG_OPEN = BlitContext.of(Texture.TEXTURE,55,47,146,213);
    private static final BlitContext RECIPE_BG_OFF = BlitContext.of(Texture.TEXTURE,9,46,35,213);



    public ResearchContainerScreen(SyncData data) {
        super(Component.empty());
        this.data = data;
        this.lastDataHash = data.getDataHash(); // 初始化哈希值
    }

    private void addRecipePageWidgets(){

        // 添加 ResearchScreen 特有的组件
        int guiLeft = (this.width - 256) / 2;
        int guiTop = (this.height - 256) / 2;

        addRenderableWidget(new OpenRecipeWidget(guiLeft+18, guiTop+104, this));
        addRenderableWidget(new SearchEditBox(this.font, guiLeft+12,guiTop+98,71,23, this));

        // 生成 5列4排的 RecipeTechSlot，每个22x22，紧挨着排列
        int recipeStartX = guiLeft + 13;  // 配方书区域起始X
        int recipeStartY = guiTop + 126;  // 配方书区域起始Y（搜索框下方）
        int slotSize = 22;  // 每个槽位大小 22x22
        int columns = 5;  // 5列
        int rows = 4;     // 4排

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                int x = recipeStartX + col * slotSize;
                int y = recipeStartY + row * slotSize;

            }
        }
    }

    @Override
    protected void init() {
        // 首先调用父类的init()，清空之前的widgets

        super.init();

        // 计算窗口和内部坐标
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
        this.mouseHandleBgData.setBoundaryMarginRatio(0.1f);  // 10%的余量

        // 创建科技槽位
        var insList = data.getCacheds();
        var vecList = data.getVecMap();
        int insideX = centerX - insideContext.width() / 2;
        int insideY = centerY - insideContext.height() / 2;

        for (TechInstance instance : insList.values()) {
            var vec = vecList.getOrDefault(instance.getIdentifier(), Vec2i.EMPTY);
            var slot = new TechSlot(insideX + vec.x, insideY + vec.y, instance, this);
            slots.put(instance.getIdentifier(), slot);
            addWidget(slot);
        }

        addRecipePageWidgets();


        // 对 renderables 进行排序，让 IOpenRenderable 组件根据 Z 层级排序
        sortRenderablesByZLevel();

    }

    /**
     * 对可渲染组件进行排序，IOpenRenderable 组件根据 Z 层级排序（Z 层级低的先渲染）
     */
    private void sortRenderablesByZLevel() {
        // 将 renderables 转换为 List 以便排序
        var renderableList = new ArrayList<>(this.renderables);

        // 排序：IOpenRenderable 组件按 Z 层级排序，其他组件保持原有顺序
        renderableList.sort((r1, r2) -> {
            boolean isR1OpenRenderable = r1 instanceof IOpenRenderable;
            boolean isR2OpenRenderable = r2 instanceof IOpenRenderable;

            // 如果都是 IOpenRenderable，按 Z 层级排序
            if (isR1OpenRenderable && isR2OpenRenderable) {
                int z1 = ((IOpenRenderable) r1).getZLevel();
                int z2 = ((IOpenRenderable) r2).getZLevel();
                return Integer.compare(z1, z2); // Z 层级低的先渲染
            }

            // IOpenRenderable 组件优先渲染（排在前面）
            if (isR1OpenRenderable) return -1;
            if (isR2OpenRenderable) return 1;

            // 其他组件保持原有顺序
            return 0;
        });

        // 清空并重新添加排序后的组件
        this.renderables.clear();
        this.renderables.addAll(renderableList);
    }



    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.guiLeft = (this.width-256)/2;
        this.guiTop = (this.height-256)/2;

        updateSlotPositions();
        renderBg(guiGraphics, guiLeft, guiTop);
        renderInside(guiGraphics, mouseX, mouseY, partialTick);

        // 正确的 pose 操作顺序
        var pose = guiGraphics.pose();
        pose.pushPose();  // 先保存当前状态
        pose.translate(0, 0, 100);  // 然后进行变换
        renderRecipeBg(guiGraphics);
        renderFilteredChildren(guiGraphics, mouseX, mouseY, partialTick);
        renderRecipeSlots(guiGraphics);
        pose.popPose();  // 最后恢复状态
    }

    private void renderRecipeSlots(GuiGraphics guiGraphics) {


    }

    /**
     * 渲染配方书背景
     */
    private void renderRecipeBg(GuiGraphics guiGraphics) {
        var context = openRecipeBook ? RECIPE_BG_OPEN : RECIPE_BG_OFF;
        int x = guiLeft+1;
        int y = guiTop+18;
        guiGraphics.blit(context.texture(), x, y, context.u(), context.v(), context.width(), context.height(), 512, 512);
    }

    /**
     * 渲染子组件，根据isOpenRecipeBook()状态过滤IOpenRenderable组件
     */
    protected void renderFilteredChildren(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        for (var renderable : this.renderables) {
            // 如果是IOpenRenderable接口的实现
            if (renderable instanceof IOpenRenderable) {
                // OpenRecipeWidget 始终渲染（用于开关配方书）
                if (renderable instanceof OpenRecipeWidget) {
                    renderable.render(guiGraphics, mouseX, mouseY, partialTick);
                }
                // 其他 IOpenRenderable 组件只在配方书打开时渲染
                else if (isOpenRecipeBook()) {
                    renderable.render(guiGraphics, mouseX, mouseY, partialTick);
                }
            }

        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        openTicks++;
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
                var coords = calculateWindowCoords();
                var vec = data.getVecMap().getOrDefault(entry.getKey(), Vec2i.EMPTY);
                var slot = new TechSlot(coords.insideX + vec.x, coords.insideY + vec.y, entry.getValue(), this);
                slots.put(entry.getKey(), slot);
                addWidget(slot);
            }
        }
    }

    /**
     * 检查子组件是否应该被跳过（基于 IOpenRenderable 接口和配方书状态）
     */
    private boolean shouldSkipChild(Object child) {
        // OpenRecipeWidget 始终可以交互（用于开关配方书）
        if (child instanceof OpenRecipeWidget) {
            return false;
        }
        // 其他 IOpenRenderable 组件只在配方书打开时才能交互
        return child instanceof IOpenRenderable && !isOpenRecipeBook();
    }

    /**
     * 转换后的鼠标坐标辅助类
     */
    private static class TransformedMouseCoords {
        final int transformedX, transformedY;

        TransformedMouseCoords(int transformedX, int transformedY) {
            this.transformedX = transformedX;
            this.transformedY = transformedY;
        }

    }

    /**
     * 更新并获取转换后的鼠标坐标
     */
    private TransformedMouseCoords updateAndGetTransformedCoords(double mouseX, double mouseY) {
        mouseHandleBgData.updateTransformedMouseCoords(mouseX, mouseY);
        return new TransformedMouseCoords(
            mouseHandleBgData.getTransformedMouseX(),
            mouseHandleBgData.getTransformedMouseY()
        );
    }

    /**
     * 计算窗口坐标的辅助类
     */
    private static class WindowCoords {
        final int windowX, windowY, centerX, centerY, insideX, insideY;

        WindowCoords(int windowX, int windowY, int centerX, int centerY, int insideX, int insideY) {
            this.windowX = windowX;
            this.windowY = windowY;
            this.centerX = centerX;
            this.centerY = centerY;
            this.insideX = insideX;
            this.insideY = insideY;
        }
    }

    /**
     * 计算窗口和内部背景的坐标（提取重复代码）
     */
    private WindowCoords calculateWindowCoords() {
        var windowContext = getWindow();
        var insideContext = getInside();
        int windowX = guiLeft + windowContext.u();
        int windowY = guiTop + windowContext.v();
        int centerX = windowX + windowContext.width() / 2;
        int centerY = windowY + windowContext.height() / 2;
        int insideX = centerX - insideContext.width() / 2;
        int insideY = centerY - insideContext.height() / 2;
        return new WindowCoords(windowX, windowY, centerX, centerY, insideX, insideY);
    }

    /**
     * 检查鼠标是否在窗口区域内
     * @param mouseX 鼠标X坐标
     * @param mouseY 鼠标Y坐标
     * @return true 如果鼠标在窗口区域内，false 否则
     */
    private boolean isMouseInWindow(double mouseX, double mouseY) {
        var windowContext = getWindow();
        int windowX = guiLeft + windowContext.u();
        int windowY = guiTop + windowContext.v();
        int windowWidth = windowContext.width();
        int windowHeight = windowContext.height();

        return mouseX >= windowX && mouseX <= windowX + windowWidth &&
               mouseY >= windowY && mouseY <= windowY + windowHeight;
    }

    /**
     * 检查鼠标是否在配方书区域内
     * @param mouseX 鼠标X坐标
     * @param mouseY 鼠标Y坐标
     * @return true 如果配方书打开且鼠标在配方书区域内，false 否则
     */
    private boolean isMouseInRecipeBook(double mouseX, double mouseY) {
        if (!openRecipeBook) {
            return false;
        }

        // 配方书的渲染区域（根据 RECIPE_BG_OPEN 的尺寸）
        // RECIPE_BG_OPEN = BlitContext.of(Texture.TEXTURE,55,47,146,213);
        // 宽度: 146, 高度: 213
        int recipeX = guiLeft + 1;
        int recipeY = guiTop + 18;
        int recipeWidth = 146;
        int recipeHeight = 213;

        return mouseX >= recipeX && mouseX <= recipeX + recipeWidth &&
               mouseY >= recipeY && mouseY <= recipeY + recipeHeight;
    }

    /**
     * 更新所有槽位的位置，确保它们与内部背景对齐
     */
    private void updateSlotPositions() {
        var coords = calculateWindowCoords();
        var vecList = data.getVecMap();

        for (var entry : slots.entrySet()) {
            var vec = vecList.getOrDefault(entry.getKey(), Vec2i.EMPTY);
            var slot = entry.getValue();
            slot.setX(coords.insideX + vec.x);
            slot.setY(coords.insideY + vec.y);
        }
    }


    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        // 检查鼠标是否在窗口区域内
        if (!isMouseInWindow(mouseX, mouseY)) {
            return false;  // 鼠标不在窗口区域内，不处理缩放
        }
        
        // 如果鼠标在配方书区域内，不处理背景缩放
        if (isMouseInRecipeBook(mouseX, mouseY)) {
            return false;
        }

        // 使用MouseHandleBgData处理滚轮缩放
        return mouseHandleBgData.handleMouseScroll(mouseX, mouseY, delta);
    }


    private int dragTotal = 0;
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        // 检查鼠标是否在窗口区域内
        if (!isMouseInWindow(mouseX, mouseY)) {
            return false;  // 鼠标不在窗口区域内，不处理拖拽
        }

        // 如果鼠标在配方书区域内，不处理背景拖拽，但允许配方书内部组件处理
        if (isMouseInRecipeBook(mouseX, mouseY)) {
            // 只让配方书内部的 IOpenRenderable 组件处理事件
            for (var child : List.copyOf(children())) {
                if (child instanceof IOpenRenderable) {
                    if (child.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
                        return true;
                    }
                }
            }
            return false;  // 配方书区域内不处理背景拖拽和槽位交互
        }

        if (button == GLFW.GLFW_MOUSE_BUTTON_1){
            dragTotal += Math.abs(dragX);
            dragTotal += Math.abs(dragY);

            if (dragTotal >= 1){
                isDragging = true;
                mouseHandleBgData.handleMouseDragged(dragX, dragY);
            }
        }

        var coords = updateAndGetTransformedCoords(mouseX, mouseY);

        for (var child : List.copyOf(children())) {
            if (shouldSkipChild(child)) {
                continue;
            }

            // 根据组件类型使用不同的坐标
            if (child instanceof TechSlot) {
                if (child.mouseDragged(coords.transformedX, coords.transformedY, button, dragX, dragY)) {
                    return true;
                }
            } else {
                if (child.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
                    return true;
                }
            }
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // 检查鼠标是否在窗口区域内
        if (!isMouseInWindow(mouseX, mouseY)) {
            return false;  // 鼠标不在窗口区域内，不处理点击
        }

        if (isDragging) {
            return false;
        }

        // 如果鼠标在配方书区域内，只处理配方书内部组件
        if (isMouseInRecipeBook(mouseX, mouseY)) {
            // 只让配方书内部的 IOpenRenderable 组件处理事件
            for (var child : List.copyOf(children())) {
                if (child instanceof IOpenRenderable) {
                    if (child.isMouseOver(mouseX, mouseY)) {
                        if (child.mouseClicked(mouseX, mouseY, button)) {
                            setFocused(child);
                            return true;
                        }
                    }
                }
            }
            return false;  // 配方书区域内不处理槽位点击
        }

        var coords = updateAndGetTransformedCoords(mouseX, mouseY);

        for (var child : List.copyOf(children())) {
            if (shouldSkipChild(child)) {
                continue;
            }

            // 根据组件类型使用不同的坐标和判断逻辑
            if (child instanceof TechSlot) {
                if (child.isMouseOver(coords.transformedX, coords.transformedY)) {
                    if (child.mouseClicked(coords.transformedX, coords.transformedY, button)) {
                        setFocused(child);
                        return true;
                    }
                }
            } else {
                if (child.isMouseOver(mouseX, mouseY)) {
                    if (child.mouseClicked(mouseX, mouseY, button)) {
                        setFocused(child);
                        return true;
                    }
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        // 检查鼠标是否在窗口区域内
        if (!isMouseInWindow(mouseX, mouseY)) {
            return false;  // 鼠标不在窗口区域内，不处理释放事件
        }

        // 重置拖拽状态
        if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
            isDragging = false;
            dragTotal = 0;
        }

        // 如果鼠标在配方书区域内，只处理配方书内部组件
        if (isMouseInRecipeBook(mouseX, mouseY)) {
            // 只让配方书内部的 IOpenRenderable 组件处理事件
            for (var child : children()) {
                if (child instanceof IOpenRenderable) {
                    if (child.mouseReleased(mouseX, mouseY, button)) {
                        return true;
                    }
                }
            }
            return false;  // 配方书区域内不处理槽位释放事件
        }

        var coords = updateAndGetTransformedCoords(mouseX, mouseY);

        for (var child : children()) {
            if (shouldSkipChild(child)) {
                continue;
            }

            // 根据组件类型使用不同的坐标
            if (child instanceof TechSlot) {
                if (child.mouseReleased(coords.transformedX, coords.transformedY, button)) {
                    return true;
                }
            } else {
                if (child.mouseReleased(mouseX, mouseY, button)) {
                    return true;
                }
            }
        }

        return super.mouseReleased(mouseX, mouseY, button);
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

    /**
     * 获取配方书区域
     * @return
     */
    protected abstract BlitContext getRecipe();

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
