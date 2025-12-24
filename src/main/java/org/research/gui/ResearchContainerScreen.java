package org.research.gui;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector2i;
import org.research.api.tech.TechInstance;
import org.research.gui.component.TechSlot;
import org.research.player.inventory.AbstractResearchContainer;

import java.util.HashMap;

public abstract class ResearchContainerScreen<R extends AbstractResearchContainer> extends Screen {

    private R container;

    private HashMap<ResourceLocation, TechSlot> techSlotMap = new HashMap<>();

    protected ResearchContainerScreen(R container) {
        super(Component.empty());
    }


    public void initSlot(){
        var techMap = container.getTechMap();
        var vecMap = container.getVecMap();
        for(TechInstance tech : techMap.values()){
            var id = tech.getIdentifier();
            var vec = vecMap.getOrDefault(id,new Vector2i(-1,-1));
            var techSlot = new TechSlot(vec.x,vec.y,tech);
            techSlotMap.put(id,techSlot);
        }
    }




}
