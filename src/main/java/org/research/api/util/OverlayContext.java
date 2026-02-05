package org.research.api.util;

import net.minecraft.resources.ResourceLocation;
import org.research.Research;

public class OverlayContext {

    public static final ResourceLocation OVERLAY_CONTEXT = Research.asResource("textures/gui/research_overlay.png");

    //收起状态
    @Deprecated
    public static final BlitContextV2 CLOSE_START = BlitContextV2.of(OVERLAY_CONTEXT,0,0,12,14,256,256);
    @Deprecated
    public static final BlitContextV2 CLOSE_CENTER = BlitContextV2.of(OVERLAY_CONTEXT,0,31,11,225,256,256);
    @Deprecated
    public static final BlitContextV2 CLOSE_END = BlitContextV2.of(OVERLAY_CONTEXT,0,15,12,14,256,256);

    //展开状态
    public static final BlitContextV2 OPEN_START = BlitContextV2.of(OVERLAY_CONTEXT,0,0,68,36,256,256);
    public static final BlitContextV2 OPEN_CENTER = BlitContextV2.of(OVERLAY_CONTEXT,0,56,68,200,256,256);
    public static final BlitContextV2 OPEN_END = BlitContextV2.of(OVERLAY_CONTEXT,0,37,68,14,256,256);


    //箭头
    public static final BlitContextV2 EXPAND = BlitContextV2.of(OVERLAY_CONTEXT,164,0,5,3,256,256);
    public static final BlitContextV2 COLLAPSE = BlitContextV2.of(OVERLAY_CONTEXT,160,0,3,5,256,256);
}

