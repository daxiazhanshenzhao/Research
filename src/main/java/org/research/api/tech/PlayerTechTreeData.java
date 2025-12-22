package org.research.api.tech;

import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.core.jmx.Server;
import org.research.api.init.TechInit;
import org.research.api.tech.graphTree.GraphAdjList;

import java.util.ArrayList;
import java.util.List;

public class PlayerTechTreeData extends GraphAdjList<TechInstance> {

    private ServerPlayer player;


    public PlayerTechTreeData(ServerPlayer player) {
        super(TechInit.getAllTech().size());
    }

    /**
     * 将注册项初始化为图表
     */
    public void init(){
        List<TechInstance> techs = initInstance(player);


    }

    private List<TechInstance> initInstance(ServerPlayer player){
        List<TechInstance> techs = new ArrayList<>();
        for (AbstractTech tech : TechInit.getAllTech()){
            TechInstance instance = new TechInstance(tech,player);

            techs.add(instance);
        }
        return techs;
    }

}
