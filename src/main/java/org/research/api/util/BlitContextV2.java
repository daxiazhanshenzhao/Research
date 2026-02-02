package org.research.api.util;

import net.minecraft.resources.ResourceLocation;

public record BlitContextV2(ResourceLocation texture,
                            int u,
                            int v,
                            int width,
                            int height,
                            int textureWidth,
                            int textureHeight) {

    public static BlitContextV2 of(ResourceLocation texture, int u, int v, int width, int height, int textureWidth, int textureHeight) {
        return new BlitContextV2(texture, u, v, width, height, textureWidth, textureHeight);
    }


}
