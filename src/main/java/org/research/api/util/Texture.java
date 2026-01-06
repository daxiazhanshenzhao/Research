package org.research.api.util;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.research.Research;

public class Texture {

    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Research.MODID, "textures/gui/research_all.png");

    private static final ResourceLocation BACKGROUND = Research.asResource( "textures/gui/background.png");
    private static final ResourceLocation INSIDE = Research.asResource( "textures/gui/inside.png");


    public static final BlitContext background = new BlitContext(BACKGROUND,0,0,256,256);

    public static final BlitContext window = new BlitContext(BACKGROUND,15,28,226,187);
    public static final BlitContext inside = new BlitContext(INSIDE,0,0,474,292);



}
