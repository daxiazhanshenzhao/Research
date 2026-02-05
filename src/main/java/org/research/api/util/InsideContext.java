package org.research.api.util;

import net.minecraft.resources.ResourceLocation;
import org.research.Research;

public class InsideContext {


    public static final ResourceLocation INSIDE_CONTEXT = Research.asResource("textures/gui/inside_context.png");

    public static final BlitContextV2 RECIPE_PAGE_OPEN = BlitContextV2.of(INSIDE_CONTEXT,0,0,145,213,256,256);
    public static final BlitContextV2 RECIPE_PAGE_CLOSED = BlitContextV2.of(INSIDE_CONTEXT,153,0,35,213,256,256);

    public static final BlitContextV2 RECIPE_SEARCH_BUTTON = BlitContextV2.of(INSIDE_CONTEXT,142,218,71,23,256,256);

    public static final BlitContextV2 RECIPE_SEARCH_TECH_BUTTON = BlitContextV2.of(INSIDE_CONTEXT,98,218,20,20,256,256);
    public static final BlitContextV2 RECIPE_SEARCH_TECH_BUTTON_ACTIVE = BlitContextV2.of(INSIDE_CONTEXT,119,218,22,22,256,256);

    public static final BlitContextV2 RECIPE_CHANGE_BUTTON_LEFT = BlitContextV2.of(INSIDE_CONTEXT,212,29,14,8,256,256);
    public static final BlitContextV2 RECIPE_CHANGE_BUTTON_LEFT_ACTIVE = BlitContextV2.of(INSIDE_CONTEXT,193,29,14,8,256,256);

    public static final BlitContextV2 RECIPE_CHANGE_BUTTON_RIGHT = BlitContextV2.of(INSIDE_CONTEXT,192,16,14,8,256,256);
    public static final BlitContextV2 RECIPE_CHANGE_BUTTON_RIGHT_ACTIVE = BlitContextV2.of(INSIDE_CONTEXT,211,16,14,8,256,256);




}
