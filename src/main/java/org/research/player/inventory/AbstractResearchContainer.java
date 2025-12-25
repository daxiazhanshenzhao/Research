package org.research.player.inventory;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.joml.Vector2i;
import org.research.api.tech.AbstractTech;
import org.research.api.tech.PlayerTechTreeData;
import org.research.api.tech.TechInstance;
import org.research.api.util.BlitContext;
import org.research.gui.component.TechSlot;

import java.util.HashMap;


/**
 * container负责给PlayerTechTreeData初始化，
 * PlayerTechData负责做步进操作，
 */
@Deprecated
public abstract class AbstractResearchContainer implements IResearchContainer {


//    private HashMap<ResourceLocation, TechInstance> techMap = new HashMap<>();
//    private HashMap<ResourceLocation, Vector2i> vecMap = new HashMap<>();
//
//    private Player player;
//    private PlayerTechTreeData playerTechTreeData;
//
//
//
//    protected abstract void initTechs();
//
//
//
//    public AbstractResearchContainer(ServerPlayer player, PlayerTechTreeData data) {
//        this.player = player;
//        this.playerTechTreeData = data;
//        initTechs();
//    }
//
//    public AbstractResearchContainer(PlayerTechTreeData data) {
//        this.playerTechTreeData = data;
//        this.player = null;
//        initTechs();
//    }
//
//
//    public void addTech(AbstractTech tech, int x, int y) {
//
//
//
//    }
//
//
//
//    public HashMap<ResourceLocation, TechInstance> getTechMap() {
//        return techMap;
//    }
//
//    public HashMap<ResourceLocation, Vector2i> getVecMap() {
//        return vecMap;
//    }
}
