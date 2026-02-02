package org.research.api.util;

import net.minecraft.resources.ResourceLocation;
import org.research.Research;

public class InsideContext {


    public static final ResourceLocation INSIDE_CONTEXT = Research.asResource("textures/gui/inside_context.png");

    public static final BlitContextV2 RECIPE_PAGE_OPEN = BlitContextV2.of(INSIDE_CONTEXT,0,0,145,213,256,256);
    public static final BlitContextV2 RECIPE_PAGE_CLOSED = BlitContextV2.of(INSIDE_CONTEXT,153,0,35,213,256,256);


}
