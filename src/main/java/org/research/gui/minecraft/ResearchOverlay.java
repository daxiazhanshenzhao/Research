package org.research.gui.minecraft;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import org.research.api.config.ClientConfig;
import org.research.api.util.OverlayContext;

public class ResearchOverlay implements IGuiOverlay {

    public static ResearchOverlay instance;


    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        // 从配置文件获取偏移量
        int xOffset = ClientConfig.getOverlayXOffset();
        int yOffset = ClientConfig.getOverlayYOffset();

        // 获取窗口尺寸
        int width = gui.getMinecraft().getWindow().getGuiScaledWidth();
        int height = gui.getMinecraft().getWindow().getGuiScaledHeight();

        // 获取关闭状态的纹理上下文
        var closeStart = OverlayContext.CLOSE_START;
        var closeCenter = OverlayContext.CLOSE_CENTER;
        var closeEnd = OverlayContext.CLOSE_END;

        // 计算渲染起始位置（靠近右边，留一些边距）
        int margin = 10; // 距离右边的边距
        int renderStartX = width - closeStart.width() - margin + xOffset;
        int renderStartY = height / 2 - closeStart.height() / 2 + yOffset; // 垂直居中

        // 渲染顶部
        guiGraphics.blit(
                closeStart.texture(),
                renderStartX,
                renderStartY,
                closeStart.u(),
                closeStart.v(),
                closeStart.width(),
                closeStart.height(),
                closeStart.textureWidth(),
                closeStart.textureHeight()
        );

        // 渲染中间部分（可以根据需要调整高度）
        int centerHeight = 100; // 中间部分的高度
        guiGraphics.blit(
                closeCenter.texture(),
                renderStartX,
                renderStartY + closeStart.height(),
                closeCenter.u(),
                closeCenter.v(),
                closeCenter.width(),
                centerHeight,
                closeCenter.textureWidth(),
                closeCenter.textureHeight()
        );

        // 渲染底部
        guiGraphics.blit(
                closeEnd.texture(),
                renderStartX,
                renderStartY + closeStart.height() + centerHeight,
                closeEnd.u(),
                closeEnd.v(),
                closeEnd.width(),
                closeEnd.height(),
                closeEnd.textureWidth(),
                closeEnd.textureHeight()
        );
    }
}
