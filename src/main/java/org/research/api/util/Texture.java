package org.research.api.util;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.research.Research;

public class Texture {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Research.MODID, "textures/gui/research.png");

    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(Research.MODID, "textures/gui/background.png");


    public static final BlitContext background = new BlitContext(0,0,256,256);





    public static void renderBlit(int x,int y,GuiGraphics context, BlitContext rect){
        context.blit(TEXTURE,x,y,rect.u(),rect.v(),rect.width(),rect.height());
    }

    public static void renderBg(int x,int y,GuiGraphics context){
        context.blit(BACKGROUND,x,y,background.u(),background.v(),background.width(),background.height());
    }
}
