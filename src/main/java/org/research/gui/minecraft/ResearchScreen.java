package org.research.gui.minecraft;

import com.mojang.datafixers.util.Pair;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.research.api.tech.SyncData;
import org.research.api.util.BlitContext;
import org.research.api.util.Texture;

@OnlyIn(value = Dist.CLIENT)
public class ResearchScreen extends ResearchContainerScreen {

    public ResearchScreen(SyncData syncData) {
        super(syncData);
    }

    @Override
    protected Pair<Float, Float> getMaxOrMinScale() {
        return Pair.of(0.5f, 2f);
    }

    @Override
    public BlitContext getBg() {
        return Texture.background;
    }

    @Override
    protected BlitContext getWindow() {
        return Texture.window;
    }

    @Override
    protected BlitContext getInside() {
        return Texture.inside;
    }

    @Override
    protected BlitContext getRecipe() {
        return BlitContext.of(null,8,21,124,221);
    }
}
