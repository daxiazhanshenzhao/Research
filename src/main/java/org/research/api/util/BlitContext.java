package org.research.api.util;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public record BlitContext(ResourceLocation texture, int u, int v, int width, int height) {
    public static BlitContext of(ResourceLocation texture, int u, int v, int width, int height) {
        return new BlitContext(texture, u, v, width, height);
    }


}
