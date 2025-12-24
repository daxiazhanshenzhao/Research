package org.research.player.inventory;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.joml.Vector2i;
import org.research.api.tech.AbstractTech;
import org.research.api.tech.TechInstance;
import org.research.api.util.BlitContext;
import org.research.gui.component.TechSlot;

import java.util.HashMap;

public abstract class AbstractResearchContainer {


    private HashMap<ResourceLocation, TechInstance> techMap = new HashMap<>();
    private HashMap<ResourceLocation, Vector2i> vecMap = new HashMap<>();

    private Player player;



    protected abstract void initTechs();



    public AbstractResearchContainer(Player player){
        this.player = player;

        initTechs();

    }


    public void addTech(AbstractTech tech, int x, int y) {

        var techInstance = new TechInstance(tech,(ServerPlayer) player);
        this.techMap.put(techInstance.getIdentifier(), techInstance);


    }
    public void addTech(TechSlot slot) {}

    public abstract ResourceLocation getBackground();
    public abstract BlitContext getBgContext();

    public HashMap<ResourceLocation, TechInstance> getTechMap() {
        return techMap;
    }

    public HashMap<ResourceLocation, Vector2i> getVecMap() {
        return vecMap;
    }
}
