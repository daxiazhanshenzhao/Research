package org.research.api.gui.wrapper;

import org.research.api.util.BlitContextV2;
import org.research.api.util.UVContext;

/**
 *
 * @param minScale 最小缩放率
 * @param maxScale 最大缩放率
 * @param backGround 背景
 * @param window 窗口
 * @param insideUV 内部区域UV
 * @param movableAreaRatio 可移动区域比例 (0.0-1.0)
 */
public record ScreenConfigData(
        double minScale,
        double maxScale,
        BlitContextV2 backGround,
        BlitContextV2 window,
        UVContext insideUV,
        double movableAreaRatio



) {

}
