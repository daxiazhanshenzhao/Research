package org.research.gui.minecraft;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import org.research.api.client.ClientResearchData;
import org.research.api.gui.ClientScreenManager;
import org.research.api.gui.PoseStackData;
import org.research.api.gui.ScreenData;
import org.research.api.util.InsideContext;

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

            // 渲染底层（背景和技能槽位，带缩放和平移变换）
            poseStackData.pushPose();
            context.enableScissor(insideX, insideY, insideX + config.insideUV().width(), insideY + config.insideUV().height());

            // 应用缩放和平移变换
            manager.applyTransform();

            renderBackGround(context, guiLeft, guiTop, manager);
            renderTechSlot(context, manager, partialTick);

            context.disableScissor();
            poseStackData.popPose();

            // 渲染窗口边框（不受缩放影响）
            renderWindow(context, guiLeft, guiTop, manager);

            // 渲染配方页面（不受缩放影响）
            renderRecipeBackGround(context, guiLeft, guiTop, manager);
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
        // TODO: 处理技能选择逻辑
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

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        ClientResearchData.getManager().ifPresent(manager ->
            manager.handleMousePositon(mouseX, mouseY)
        );
        super.mouseMoved(mouseX, mouseY);
    }

    private void renderBackGround(GuiGraphics context, int guiLeft, int guiTop, ClientScreenManager manager) {
        var bgContext = manager.getScreenConfigData().backGround();
        context.blit(bgContext.texture(), guiLeft, guiTop,
                bgContext.u(), bgContext.v(),
                bgContext.width(), bgContext.height(),
                bgContext.textureWidth(), bgContext.textureHeight());
    }

    private void renderTechSlot(GuiGraphics context, ClientScreenManager manager, float partialTick) {
        manager.getOptTechSlotData().ifPresent(techSlotData -> {
            var poseStackData = manager.getPoseStackData();
            var mouseData = manager.getMouseData();
            var techSlots = techSlotData.getCachedTechSlots();

            // 获取转换后的鼠标坐标（屏幕坐标 -> GUI坐标）
            double[] guiCoords = poseStackData.inverseTransform(
                mouseData.getTransformedMouseX(),
                mouseData.getTransformedMouseY()
            );

            int guiMouseX = Math.round((float) guiCoords[0]);
            int guiMouseY = Math.round((float) guiCoords[1]);

            // 渲染所有技能槽位
            // for (TechSlot techSlot : techSlots) {
            //     techSlot.render(context, guiMouseX, guiMouseY, partialTick);
            // }
        });

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
}
