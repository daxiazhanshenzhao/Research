package org.research.player.inventory;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.research.api.tech.AbstractTech;
import org.research.api.tech.TechInstance;
import org.research.api.util.BlitContext;
import org.research.gui.component.TechSlot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class AbstractResearchContainer {

    private HashMap<ResourceLocation,TechSlot> slots = new HashMap<>();
    private HashMap<ResourceLocation, TechInstance> techMap = new HashMap<>();
    private Player player;


    protected abstract void initTechs();



    public AbstractResearchContainer(Player player){
        this.player = player;

        initTechs();

    }


    public void addTech(AbstractTech tech, int x, int y) {

        var techInstance = new TechInstance(tech,(ServerPlayer) player);
        var slot = new TechSlot(x,y,techInstance);

        this.slots.put(tech.getIdentifier(),slot);
    }
    public void addTech(TechSlot slot) {}

    public abstract ResourceLocation getBackground();
    public abstract BlitContext getBgContext();

    public HashMap<ResourceLocation, TechSlot> getSlots() {
        return slots;
    }

}
