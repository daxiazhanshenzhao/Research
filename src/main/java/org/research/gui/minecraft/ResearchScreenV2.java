package org.research.gui.minecraft;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import org.research.api.client.ClientResearchData;
import org.research.api.gui.ClientScreenManager;
import org.research.api.gui.wrapper.PoseStackData;
import org.research.api.gui.wrapper.ScreenData;
import org.research.api.recipe.category.RecipeCategory;
import org.research.api.util.InsideContext;
import org.research.gui.minecraft.component.*;

public class ResearchScreenV2 extends Screen {

    public static final int OPEN_BUTTON_WIDTH = 18;
    public static final int OPEN_BUTTON_HEIGHT = 85;

    public static final int OPEN_RECIPE_PAGE_WIDTH = 128;
    public static final int OPEN_RECIPE_PAGE_HEIGHT = 213;

    public static final int SEARCH_BOX_U = 14;
    public static final int SEARCH_BOX_V = 80;

    public static final int RECIPE_START_U = 14;
    public static final int RECIPE_START_V = 106;

    public static final int CHANGE_BUTTON_LEFT_U = 12;
    public static final int CHANGE_BUTTON_LEFT_V = 196;

    public static final int CHANGE_BUTTON_RIGHT_U = 109;
    public static final int CHANGE_BUTTON_RIGHT_V = 196;

    public ResearchScreenV2() {
        super(Component.empty());
    }

    @Override
    protected void init() {

        ClientScreenManager manager = ClientResearchData.getScreenManager();
        var config = manager.getScreenConfigData();
        // 获取窗口配置信息
        var windowContext = config.window();

        // 使用整张图片的实际渲染尺寸（包含空白内容）
        int guiLeft = (this.width - windowContext.textureWidth()) / 2;
        int guiTop = (this.height - windowContext.textureHeight()) / 2;

        ScreenData screenData = manager.getScreenData();
        screenData.setGuiLeft(guiLeft);
        screenData.setGuiTop(guiTop);
        screenData.setScale(1.0f);

        //内部坐标
        var insideContext = config.insideUV();
        screenData.setInsideX(guiLeft + insideContext.u());
        screenData.setInsideY(guiTop + insideContext.v());

        screenData.setGuiTextureWidth(guiLeft + windowContext.u());
        screenData.setGuiTextureHeight(guiTop + windowContext.v());

        addRenderableWidget(new OpenRecipeWidget(
                screenData.getGuiTextureWidth() + OPEN_BUTTON_WIDTH,
                screenData.getGuiTextureHeight() + OPEN_BUTTON_HEIGHT,
                manager));

        addRenderableWidget(new SearchEditBox(
                screenData.getGuiTextureWidth() + SEARCH_BOX_U,
                screenData.getGuiTextureHeight() + SEARCH_BOX_V,
                manager));

        // 生成 5列4排的 SearchTechSlot，每个22x22，紧挨着排列
        int slotSize = 22;                  // 每个槽位大小 22x22
        int columns = 5;                    // 5列
        int rows = 4;                       // 4排
        int recipeStartU = RECIPE_START_U;  // 配方书区域起始相对X偏移
        int recipeStartV = RECIPE_START_V;  // 配方书区域起始相对Y偏移（搜索框下方）
        int id = 0;                         //0-19

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                int slotU = recipeStartU + col * slotSize;
                int slotV = recipeStartV + row * slotSize;
                addRenderableWidget(new SearchTechSlot(
                    screenData.getGuiTextureWidth() + slotU,
                    screenData.getGuiTextureHeight() + slotV,
                    manager,id
                ));
                id++; // 每创建一个槽位，id递增1
            }
        }

        addRenderableWidget(new ChangePageButton(
                screenData.getGuiTextureWidth()+ CHANGE_BUTTON_LEFT_U,
                screenData.getGuiTextureHeight()+ CHANGE_BUTTON_LEFT_V,
                false,manager
        ));

        addRenderableWidget(new ChangePageButton(
                screenData.getGuiTextureWidth() + CHANGE_BUTTON_RIGHT_U,
                screenData.getGuiTextureHeight() + CHANGE_BUTTON_RIGHT_V,
                true,manager
        ));

        // 初始化时触发一次空搜索，显示按首字母排序的前20个物品
        manager.handleSearchEditBox("");

        super.init();
        resize();
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        super.resize(minecraft, width, height);
        resize();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void resize(){

        ClientScreenManager manager = ClientResearchData.getScreenManager();
        var config = manager.getScreenConfigData();
        // 获取窗口配置信息
        var windowContext = config.window();

        // 使用整张图片的实际渲染尺寸（包含空白内容）
        int guiLeft = (this.width - windowContext.textureWidth()) / 2;
        int guiTop = (this.height - windowContext.textureHeight()) / 2;

        ScreenData screenData = manager.getScreenData();
        screenData.setGuiLeft(guiLeft);
        screenData.setGuiTop(guiTop);
        screenData.setScale(1.0f);

        //内部坐标
        var insideContext = config.insideUV();
        screenData.setInsideX(guiLeft + insideContext.u());
        screenData.setInsideY(guiTop + insideContext.v());

        screenData.setGuiTextureWidth(guiLeft + windowContext.u());
        screenData.setGuiTextureHeight(guiTop + windowContext.v());



        //初始化所有techslot的位置
        var techSlotData = manager.getTechSlotData();
        if (techSlotData != null && !techSlotData.isEmpty()) {
            var syncData = ClientResearchData.getSyncData();
            techSlotData.initializePositionsWithVecMap(syncData.getVecMap(), guiLeft, guiTop);
        }

        // 清除所有 RecipeCategory 的槽位缓存，使得下次渲染时根据新的窗口坐标重新创建 RecipeTechSlot
        ClientResearchData.recipeCategories.clearAllCache();

    }
    public void tick() {
        super.tick();


    }
    @Override
    public void onClose() {
        // 重置管理器状态，防止持久化数据影响下次打开
        ClientResearchData.getScreenManager().reset();
        super.onClose();
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float partialTick) {
        ClientScreenManager manager = ClientResearchData.getScreenManager();

        var screenData = manager.getScreenData();
        int guiLeft = screenData.getGuiLeft();
        int guiTop = screenData.getGuiTop();
        int insideX = screenData.getInsideX();
        int insideY = screenData.getInsideY();
        var config = manager.getScreenConfigData();

        PoseStackData poseStackData = manager.getPoseStackData();
        poseStackData.setPose(context);

        // 每帧刷新一次转换后的鼠标坐标，避免悬停/tooltip 使用旧值
        manager.handleMousePositon(mouseX, mouseY);

        // 渲染底层（背景和技能槽位，带缩放和平移变换）
        poseStackData.pushPose();
            context.enableScissor(insideX, insideY, insideX + config.insideUV().width(), insideY + config.insideUV().height());

        // 应用缩放和平移变换
        manager.applyTransform();

        renderBackGround(context, guiLeft, guiTop, manager);
        renderTechSlot(context, manager, partialTick);

            context.disableScissor();
        poseStackData.popPose();
        poseStackData.translate(0,0,5000);
        // 渲染窗口边框（不受缩放影响）
        renderWindow(context, guiLeft, guiTop, manager);

        // 渲染配方页面（不受缩放影响）
        renderRecipeBackGround(context, guiLeft, guiTop, manager);

        renderWidget(context, mouseX, mouseY, partialTick,manager);
        renderRecipe(context,manager,mouseX,mouseY,partialTick);
        // 渲染 Tooltips（在屏幕空间，不受变换影响）
        renderTooltips(context, manager, mouseX, mouseY);
    }






    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
            ClientScreenManager manager = ClientResearchData.getScreenManager();

            // 检查鼠标下的组件是否为 OpenRecipeWidget
            for (var child : this.children()) {
                if (child instanceof OpenRecipeWidget && child.isMouseOver(mouseX, mouseY)) {
                    // 始终触发 OpenRecipeWidget 的释放方法
                    return super.mouseReleased(mouseX, mouseY, button);
                }
            }

            // 如果配方界面打开（鼠标在配方页面上），调用 super 方法处理按钮
            if (manager.isMouseOnRecipePage(mouseX, mouseY)) {
                return super.mouseReleased(mouseX, mouseY, button);
            } else {
                // 配方界面关闭，只处理 TechSlot 的逻辑，不调用 super（按钮不生效）
                manager.handleMouseReleased(mouseX, mouseY, button);
                return true; // 消费事件
            }
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
            ClientScreenManager manager = ClientResearchData.getScreenManager();

            // 检查鼠标下的组件是否为 OpenRecipeWidget
            for (var child : this.children()) {
                if (child instanceof OpenRecipeWidget && child.isMouseOver(mouseX, mouseY)) {
                    // 始终触发 OpenRecipeWidget 的拖拽方法
                    return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
                }
            }

            // 如果配方界面打开（鼠标在配方页面上），调用 super 方法处理按钮拖拽
            if (manager.isMouseOnRecipePage(mouseX, mouseY)) {
                return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
            } else {
                // 配方界面关闭，只处理 TechSlot 的拖拽逻辑，不调用 super（按钮不生效）
                manager.handleMouseDrag(mouseX, mouseY, button, dragX, dragY);
                return true; // 消费事件
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
            ClientScreenManager manager = ClientResearchData.getScreenManager();

            // 检查鼠标下的组件是否为 OpenRecipeWidget
            for (var child : this.children()) {
                if (child instanceof OpenRecipeWidget && child.isMouseOver(mouseX, mouseY)) {
                    // 始终触发 OpenRecipeWidget 的点击方法
                    return super.mouseClicked(mouseX, mouseY, button);
                }
            }

            // 如果配方界面打开（鼠标在配方页面上），调用 super 方法处理按钮点击
            if (manager.isMouseOnRecipePage(mouseX, mouseY)) {
                return super.mouseClicked(mouseX, mouseY, button);
            } else {
                // 配方界面关闭，只处理 TechSlot 的点击逻辑，不调用 super（按钮不生效）
                manager.handleMouseClick(mouseX, mouseY, button);
                return true; // 消费事件
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        ClientScreenManager manager = ClientResearchData.getScreenManager();

        // 如果鼠标在配方页面上，不触发缩放逻辑
        if (!manager.isMouseOnRecipePage(mouseX, mouseY)) {
            manager.handleMouseScrolled(mouseX, mouseY, delta);
        }

        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    private void renderBackGround(GuiGraphics context, int guiLeft, int guiTop, ClientScreenManager manager) {
        var bgContext = manager.getScreenConfigData().backGround();
        context.blit(bgContext.texture(), guiLeft, guiTop,
                bgContext.u(), bgContext.v(),
                bgContext.width(), bgContext.height(),
                bgContext.textureWidth(), bgContext.textureHeight());
    }

    /**
     * 渲染所有 TechSlot
     *
     * @param context GuiGraphics 上下文
     * @param manager ClientScreenManager 管理器
     * @param partialTick 帧间插值
     */
    private void renderTechSlot(GuiGraphics context, ClientScreenManager manager, float partialTick) {
        var techSlotData = manager.getTechSlotData();
        if (techSlotData == null || techSlotData.isEmpty()) {
            return;
        }

        var mouseData = manager.getMouseData();

        // 使用 ClientScreenManager 的坐标转换方法获取转化后的鼠标坐标
        // 这确保使用统一、经过验证的坐标转换逻辑
        double guiMouseX = mouseData.getTransformedMouseX();
        double guiMouseY = mouseData.getTransformedMouseY();

        // 渲染所有技能槽位
        var techSlots = techSlotData.getCachedTechSlots();
        for (var techSlot : techSlots) {
            techSlot.render(context, (int) guiMouseX, (int) guiMouseY, partialTick);
        }
    }


    private void renderWindow(GuiGraphics context, int guiLeft, int guiTop, ClientScreenManager manager) {
        var windowContext = manager.getScreenConfigData().window();
        context.blit(windowContext.texture(), guiLeft, guiTop,
                0, 0,
                windowContext.textureWidth(), windowContext.textureHeight(),
                windowContext.textureWidth(), windowContext.textureHeight());
    }

    private void renderRecipeBackGround(GuiGraphics context, int guiLeft, int guiTop, ClientScreenManager manager) {
        var recipeOpenPage = InsideContext.RECIPE_PAGE_OPEN;
        var recipeClosedPage = InsideContext.RECIPE_PAGE_CLOSED;
        var renderContext = manager.getScreenData().isOpenRecipe() ? recipeOpenPage : recipeClosedPage;
        var screenData = manager.getScreenData();

        context.blit(renderContext.texture(), screenData.getGuiTextureWidth(), screenData.getGuiTextureHeight(),
                renderContext.u(), renderContext.v(),
                renderContext.width(), renderContext.height(),
                renderContext.textureWidth(), renderContext.textureHeight());
    }

    private void renderWidget(GuiGraphics context, int mouseX, int mouseY, float partialTick,ClientScreenManager manager) {
        for (var widget : this.renderables) {
            if (widget instanceof OpenRecipeWidget openRecipeWidget){
                openRecipeWidget.render(context, mouseX, mouseY, partialTick);
            }else {
                if (manager.getScreenData().isOpenRecipe()){
                    widget.render(context, mouseX, mouseY, partialTick);
                }
            }

        }
    }

    private void renderRecipe(GuiGraphics context, ClientScreenManager manager,int mouseX,int mouseY,float partialTick) {
        var techSlot = manager.getTechSlotData().getFocusTechSlot();
        var tWidth = manager.getScreenData().getGuiTextureWidth();
        var tHeight = manager.getScreenData().getGuiTextureHeight();
        if (!manager.getScreenData().isOpenRecipe()) return;

        if (techSlot.equals(TechSlot.EMPTY)) {
            context.drawString(Minecraft.getInstance().font, "no recipe selected",tWidth+10 ,tHeight+20,0xFFFFFF);
            return;
        }

        var recipeWrapper = techSlot.getTechInstance().getRecipe();
        RecipeCategory<?> categories = ClientResearchData.recipeCategories.getRecipeCategories().get(recipeWrapper.type());

        categories.render(context, tWidth, tHeight,techSlot.getRecipe(),mouseX,mouseY,partialTick);

    }

    private void renderTooltips(GuiGraphics context, ClientScreenManager manager,
                                int screenMouseX, int screenMouseY) {
        // 如果鼠标在配方页面上，渲染配方槽位的 tooltip
        if (manager.isMouseOnRecipePage(screenMouseX, screenMouseY)) {
            // 只有在配方页面打开时才渲染配方槽位的 tooltip
            if (manager.getScreenData().isOpenRecipe()) {
                var techSlot = manager.getTechSlotData().getFocusTechSlot();
                if (!techSlot.equals(TechSlot.EMPTY)) {
                    var recipeWrapper = techSlot.getTechInstance().getRecipe();
                    RecipeCategory<?> categories = ClientResearchData.recipeCategories.getRecipeCategories().get(recipeWrapper.type());
                    var tWidth = manager.getScreenData().getGuiTextureWidth();
                    var tHeight = manager.getScreenData().getGuiTextureHeight();
                    categories.renderTooltips(context, tWidth, tHeight, techSlot.getRecipe(), screenMouseX, screenMouseY);
                }

                // 渲染搜索槽位的 tooltip
                for (var widget : this.renderables) {
                    if (widget instanceof SearchTechSlot searchSlot) {
                        searchSlot.renderTooltip(context, screenMouseX, screenMouseY);
                    }
                }
            }
            return;
        }

        // 检查鼠标是否在内部区域
        if (!manager.isMouseInSide(screenMouseX, screenMouseY)) {
            return;
        }

        // 使用 manager 的方法查找当前悬停的 TechSlot
        TechSlot hoveredTechSlot = manager.findHoveredTechSlot();
        if (hoveredTechSlot == null) {
            return;
        }

        // 渲染 TechSlot 的 tooltip
        hoveredTechSlot.renderTooltip(context, screenMouseX, screenMouseY);
    }
}
