package org.research.gui.minecraft;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import org.research.api.client.ClientResearchData;
import org.research.api.gui.layer.ClientOverlayManager;
import org.research.api.util.OverlayContext;

import java.util.List;

/**
 * 研究覆盖层渲染器
 * 负责在游戏界面上渲染配方信息的覆盖层
 * 所有的计算逻辑都委托给 ClientOverlayManager 处理
 */
public class ResearchOverlay implements IGuiOverlay {

    /** 单例实例 */
    public static ResearchOverlay instance = new ResearchOverlay();

    /** 覆盖层管理器 */
    public ClientOverlayManager manager = ClientResearchData.getOverlayManager();




    /**
     * 主渲染方法
     * 在每一帧都会被调用
     */
    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        int width = gui.getMinecraft().getWindow().getGuiScaledWidth();
        int height = gui.getMinecraft().getWindow().getGuiScaledHeight();

        // 从 manager 获取计算好的坐标
        int guiLeft = manager.getOverlayLeft(width);
        int guiTop = manager.getOverlayTop(height);

        // 渲染框架和文本
        renderFrame(guiGraphics, guiLeft, guiTop);
        renderRecipeText(guiGraphics, guiLeft, guiTop);
    }

    /**
     * 渲染框架（三段式：起始、中心、结束）
     * 框架靠在屏幕右侧，使用动态高度
     *
     * @param context 绘图上下文
     * @param guiLeft 左侧 X 坐标
     * @param guiTop 顶部 Y 坐标
     */
    private void renderFrame(GuiGraphics context, int guiLeft, int guiTop) {
        var startContext = OverlayContext.OPEN_START;
        var centerContext = OverlayContext.OPEN_CENTER;
        var endContext = OverlayContext.OPEN_END;

        // 渲染起始部分（顶部）
        context.blit(startContext.texture(),
                guiLeft, guiTop,
                startContext.u(), startContext.v(),
                startContext.width(), startContext.height(),
                startContext.textureWidth(), startContext.textureHeight()
        );

        // 从 manager 获取中心部分的高度
        int centerHeight = manager.getCenterHeight();

        // 渲染中心部分（可拉伸）
        context.blit(centerContext.texture(),
                guiLeft, guiTop + startContext.height(),
                centerContext.u(), centerContext.v(),
                centerContext.width(), centerHeight,
                centerContext.textureWidth(), centerContext.textureHeight()
        );

        // 渲染结束部分（底部）
        context.blit(endContext.texture(),
                guiLeft, guiTop + startContext.height() + centerHeight,
                endContext.u(), endContext.v(),
                endContext.width(), endContext.height(),
                endContext.textureWidth(), endContext.textureHeight()
        );
    }

    /**
     * 渲染配方文本信息
     * 输出物品不带箭头，输入物品带箭头
     *
     * @param context 绘图上下文
     * @param guiLeft 左侧 X 坐标
     * @param guiTop 顶部 Y 坐标
     */
    private void renderRecipeText(GuiGraphics context, int guiLeft, int guiTop) {
        var arrowContext = OverlayContext.COLLAPSE;
        int arrowWidth = arrowContext.width();
        int textOffset = 2; // 箭头和文字之间的间距

        // 检查是否为 WAITING 状态
        if (manager.isWaitingState()) {
            // 渲染 WAITING 状态的提示信息
            context.drawString(
                    manager.getFont(),
                    manager.getWaitingMessage(),
                    manager.getOutputItemX(guiLeft, 0),
                    manager.getOutputItemY(guiTop, 0),
                    manager.getTextColor()
            );
            return; // 不再渲染其他内容
        }

        // 渲染输出物品列表（不带箭头）
        List<ClientOverlayManager.ItemDisplay> outputItems = manager.getOutputItems();
        for (int i = 0; i < outputItems.size(); i++) {
            ClientOverlayManager.ItemDisplay item = outputItems.get(i);

            // 直接绘制文字，不绘制箭头
            context.drawString(
                    manager.getFont(),
                    item.getDisplayText(),
                    manager.getOutputItemX(guiLeft, i),
                    manager.getOutputItemY(guiTop, i),
                    manager.getTextColor()
            );
        }

        // 渲染输入物品列表（带箭头）
        List<ClientOverlayManager.ItemDisplay> inputItems = manager.getInputItems();
        for (int i = 0; i < inputItems.size(); i++) {
            ClientOverlayManager.ItemDisplay item = inputItems.get(i);

            // 绘制箭头
            context.blit(arrowContext.texture(),
                    manager.getInputItemX(guiLeft, i)+1,
                    manager.getInputItemY(guiTop, i)+2,
                    arrowContext.u(), arrowContext.v(),
                    arrowContext.width(), arrowContext.height(),
                    arrowContext.textureWidth(), arrowContext.textureHeight()
            );

            // 绘制文字（在箭头右侧，加上偏移）
            context.drawString(
                    manager.getFont(),
                    item.getDisplayText(),
                    manager.getInputItemX(guiLeft, i) + arrowWidth + textOffset,
                    manager.getInputItemY(guiTop, i),
                    manager.getTextColor()
            );
        }
    }
}
