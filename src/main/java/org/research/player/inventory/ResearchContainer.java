package org.research.player.inventory;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.research.Research;
import org.research.api.init.TechInit;
import org.research.api.util.BlitContext;
import org.research.api.util.Texture;

public class ResearchContainer extends AbstractResearchContainer {


    public ResearchContainer(Player player) {
        super(player);
    }

    @Override
    public ResourceLocation getBackground() {
        return Research.asResource("textures/gui/research.png");
    }

    @Override
    public BlitContext getBgContext() {
        return new BlitContext(0,0,1000,1000);
    }

    @Override
    protected void initTechs() {
        addTech(TechInit.FIRST_TECH.get(),100,100);

    }



}
