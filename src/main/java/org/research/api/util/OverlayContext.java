package org.research.api.util;

import net.minecraft.resources.ResourceLocation;
import org.research.Research;

public class OverlayContext {

    public static final ResourceLocation OVERLAY_CONTEXT = Research.asResource("textures/gui/research_overlay.png");

    //收起状态
    public static final BlitContextV2 CLOSE_START = BlitContextV2.of(OVERLAY_CONTEXT,0,0,12,14,256,256);

    public static final BlitContextV2 CLOSE_CENTER = BlitContextV2.of(OVERLAY_CONTEXT,0,31,11,225,256,256);
    public static final BlitContextV2 CLOSE_END = BlitContextV2.of(OVERLAY_CONTEXT,0,15,12,14,256,256);
    //展开状态
    //边
    public static final BlitContextV2 OPEN_SIDE_START = BlitContextV2.of(OVERLAY_CONTEXT,16,0,12,14,256,256);
    public static final BlitContextV2 OPEN_SIDE_CENTER = BlitContextV2.of(OVERLAY_CONTEXT,16,31,11,225,256,256);
    public static final BlitContextV2 OPEN_SIDE_END = BlitContextV2.of(OVERLAY_CONTEXT,16,15,12,14,256,256);

    //框
    public static final BlitContextV2 FRAME_OUTPUT = BlitContextV2.of(OVERLAY_CONTEXT,100,0,56,25,256,256);

    public static final BlitContextV2 FRAME_INPUT_START = BlitContextV2.of(OVERLAY_CONTEXT,33,0,56,12,256,256);
    public static final BlitContextV2 FRAME_INPUT_CENTER = BlitContextV2.of(OVERLAY_CONTEXT,33,31,56,225,256,256);
    public static final BlitContextV2 FRAME_INPUT_END = BlitContextV2.of(OVERLAY_CONTEXT,33,13,56,14,256,256);


}

