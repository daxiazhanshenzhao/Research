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
import org.research.api.util.InsideContext;
import org.research.gui.minecraft.component.TechSlot;

public class ResearchScreenV2 extends Screen {




    public ResearchScreenV2() {
        super(Component.empty());
    }

    @Override
    protected void init() {
        super.init();
        resize();
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        super.resize(minecraft, width, height);
        resize();
    }

    private void resize(){
        ClientResearchData.getManager().ifPresent(manager -> {
            // 获取窗口配置信息
            var windowContext = manager.getScreenConfigData().window();

            // 使用整张图片的实际渲染尺寸（包含空白内容）
            int guiLeft = (this.width - windowContext.textureWidth()) / 2;
            int guiTop = (this.height - windowContext.textureHeight()) / 2;

            ScreenData screenData = manager.getScreenData();
            screenData.setGuiLeft(guiLeft);
            screenData.setGuiTop(guiTop);
            screenData.setScale(1.0f);

            //内部坐标
            var insideContext = manager.getScreenConfigData().insideUV();
            screenData.setInsideX(guiLeft + insideContext.u());
            screenData.setInsideY(guiTop + insideContext.v());

            //初始化所有techslot的位置
            var techSlotData = manager.getTechSlotData();
            if (techSlotData != null && !techSlotData.isEmpty()) {
                var syncData = ClientResearchData.getSyncData();
                techSlotData.initializePositionsWithVecMap(syncData.getVecMap(), guiLeft, guiTop);
            }
        });
    }

    @Override
    public void tick() {
        super.tick();


    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float partialTick) {
        ClientResearchData.getManager().ifPresent(manager -> {
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
//            context.enableScissor(insideX, insideY, insideX + config.insideUV().width(), insideY + config.insideUV().height());

            // 应用缩放和平移变换
            manager.applyTransform();

            renderBackGround(context, guiLeft, guiTop, manager);
            renderTechSlot(context, manager, partialTick);

//            context.disableScissor();
            poseStackData.popPose();
            poseStackData.translate(0,0,5000);
            // 渲染窗口边框（不受缩放影响）
            renderWindow(context, guiLeft, guiTop, manager);

            // 渲染配方页面（不受缩放影响）
            renderRecipeBackGround(context, guiLeft, guiTop, manager);

            // 渲染 Tooltips（在屏幕空间，不受变换影响）
            renderTooltips(context, manager, mouseX, mouseY);
        });
    }



    @Override
    public void onClose() {
        // 重置管理器状态，防止持久化数据影响下次打开
        ClientResearchData.getManager().ifPresent(ClientScreenManager::reset);
        super.onClose();
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
            ClientResearchData.getManager().ifPresent(manager -> {
                manager.handleMouseReleased(mouseX, mouseY, button);
            });
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
            ClientResearchData.getManager().ifPresent(manager -> {
                manager.handleMouseDrag(mouseX, mouseY, button, dragX, dragY);
            });
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
            ClientResearchData.getManager().ifPresent(manager -> {
                manager.handleMouseClick(mouseX, mouseY, button);
            });
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        ClientResearchData.getManager().ifPresent(manager ->
            manager.handleMouseScrolled(mouseX, mouseY, delta)
        );
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
        var config = manager.getScreenConfigData();

        context.blit(renderContext.texture(), guiLeft + config.window().u(), guiTop + config.window().v(),
                renderContext.u(), renderContext.v(),
                renderContext.width(), renderContext.height(),
                renderContext.textureWidth(), renderContext.textureHeight());
    }


    private void renderTooltips(GuiGraphics context, ClientScreenManager manager,
                                int screenMouseX, int screenMouseY) {
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
