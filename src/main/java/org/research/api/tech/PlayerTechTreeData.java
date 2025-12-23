package org.research.api.tech;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.research.api.init.TechInit;
import org.research.api.tech.capability.ITechTreeCapability;
import org.research.api.tech.graphTree.GraphAdjList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerTechTreeData extends GraphAdjList<TechInstance> implements ITechTreeCapability {

    private ServerPlayer player;
    private Map<ResourceLocation, TechInstance> techMap;
    private TechInstance currentTech;   //只是玩家鼠标选取的tech

    public PlayerTechTreeData(ServerPlayer player) {
        super(TechInit.getAllTech().size());
        this.player = player;
        current = getCurrent();
        init();
    }


    /**
     * 将注册项初始化为图表
     */
    public void init() {
        List<TechInstance> techs = initInstance(player);
        
        // 创建ResourceLocation到TechInstance的映射，便于快速查找
        techMap = new HashMap<>();
        for (TechInstance tech : techs) {
            techMap.put(tech.getIdentifier(), tech);
        }
        
        // 先插入顶点
        for (TechInstance tech : techs) {
            insertVex(tech);
        }
        
        // 再插入边
        for (TechInstance tech : techs) {
            List<ResourceLocation> parents = tech.getParents();
            if (parents != null && !parents.isEmpty()) {
                for (ResourceLocation parentId : parents) {
                    TechInstance parentTech = techMap.get(parentId);
                    if (parentTech != null) {
                        // 插入从父节点到当前节点的边
                        insertEdge(parentTech, tech);
                    }
                }
            }
        }

    }

    @Override
    protected TechInstance getFirstTech() {
        return new TechInstance(TechInit.FIRST_TECH.get(),player);
    }

    private List<TechInstance> initInstance(ServerPlayer player) {
        List<TechInstance> techs = new ArrayList<>();
        for (AbstractTech tech : TechInit.getAllTech()) {
            TechInstance instance = new TechInstance(tech, player);
            techs.add(instance);
        }
        return techs;
    }

    /**
     * 重写nextNode方法，添加状态管理
     */
    @Override
    public void nextNode() {
        TechInstance current = getCurrent();
        if (current == null) {
            return;
        }
        
        // 将当前节点标记为完成
        current.setTechState(TechState.COMPLETED);
        
        // 调用父类的nextNode方法移动到下一个节点
        super.nextNode();
        
        // 获取新的当前节点并设置为可用状态
        TechInstance newCurrent = getCurrent();
        if (newCurrent != null) {
            newCurrent.setTechState(TechState.AVAILABLE);
        }
    }


}
