package org.research.api.tech;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.research.api.init.TechInit;
import org.research.api.recipe.IRecipe;
import org.research.api.tech.capability.ITechTreeCapability;
import org.research.api.tech.graphTree.Vec2i;

import java.util.*;

import static org.research.api.tech.capability.TechTreeDataProvider.*;

//只在服务端控制
public class PlayerTechTreeData implements ITechTreeCapability<PlayerTechTreeData> {

    private ServerPlayer player;

    /**
     * only server
     */
    private Map<ResourceLocation,TechInstance> techMap = new HashMap<>();
    /**
     * client or server
     */
    private Map<ResourceLocation,TechInstance> cacheds = new HashMap<>();
    private Map<ResourceLocation,Vec2i> vecMap = new HashMap<>();

    private int stage = 0;

    public PlayerTechTreeData(ServerPlayer player) {
//        super(TechInit.getAllTech().size());
        this.player = player;
//        current = getCurrent();
        initTechSlot();

    }

    // 用于反序列化的构造函数，不调用 initTechSlot()
    public PlayerTechTreeData(Map<ResourceLocation,TechInstance> techMap,Map<ResourceLocation,Vec2i> vecMap,int stage) {
        this.player = null;
        this.techMap = techMap;
        this.cacheds = new HashMap<>(techMap); // 复制 techMap 到 cacheds
        this.vecMap = vecMap;
        this.stage = stage;

    }

    @Override
    public TechInstance getFirstTech() {
        TechInstance techinstance = new TechInstance(TechInit.A_TECH.get(),player);
        techinstance.setTechState(TechState.COMPLETED);
        return techinstance;
    }



    /**
     * 重写nextNode方法，添加状态管理
     */

    @Override
    public void initTechSlot() {
        //stage1
        addTech(TechInit.A_TECH.get(),239,471)
                //stage2
                .addTech(TechInit.B_TECH.get(),239,428)
                //stage3
                .addTech(TechInit.C_TECH.get(),239,387)
                .addTech(TechInit.D_TECH.get(),277,387)
                .addTech(TechInit.E_TECH.get(),199,387)
                .addTech(TechInit.F_TECH.get(),316,387)
                //stage4
                .addTech(TechInit.G_TECH.get(),277,353)
                .addTech(TechInit.H_TECH.get(),239,353)
                .addTech(TechInit.I_TECH.get(),199,353)
                .addTech(TechInit.J_TECH.get(),156,353)
                //stage5
                .addTech(TechInit.K_TECH.get(),156,319)
                .addTech(TechInit.L_TECH.get(),199,319)
                .addTech(TechInit.M_TECH.get(),239,319)
                //stage6
                .addTech(TechInit.N_TECH.get(),219,279)
                .addTech(TechInit.O_TECH.get(),262,279);
    }

    @Override
    public PlayerTechTreeData addTech(AbstractTech tech, int x, int y) {
        if (techMap.containsKey(tech.getIdentifier())) return this;

        var techInstance = new TechInstance(tech,player);
        var vec2i = new Vec2i(x,y);

        //对第一个tech设置特殊状态
        if (techInstance.equals(getFirstTech())){
            techInstance.setTechState(TechState.COMPLETED);
        }

        techMap.put(tech.getIdentifier(),techInstance);
        vecMap.put(tech.getIdentifier(),vec2i);

        return this;
    }




    @Override
    public PlayerTechTreeData removeTech(AbstractTech tech) {
        if (techMap.containsKey(tech.getIdentifier())) {
            techMap.remove(tech.getIdentifier());
            vecMap.remove(tech.getIdentifier());
        }
        return this;
    }

    @Override
    public void changeTech(AbstractTech target, TechState state) {
        var instance = techMap.getOrDefault(target.getIdentifier(), null);
        if (instance != null) {
            instance.setTechState(state);
        }
    }

    @Override
    public void tryComplete(ItemStack itemStack) {
        for (TechInstance tech : techMap.values()) {
            var output = tech.getRecipeOutput();
                if (!output.isEmpty() && ItemStack.isSameItemSameTags(output, itemStack)) {

                    tech.setTechState(TechState.COMPLETED);

                    syncToClient();
                    focus(tech.getTech(),true);
                    tryNext(tech.getTech());
                }

        }
    }

    @Override
    public ResourceLocation getFocus() {
        for (TechInstance tech : techMap.values()) {
            if (tech.isFocused()){
                return tech.getIdentifier();
            }
        }
        return TechInstance.EMPTY.getIdentifier();
    }


    @Override
    public void focus(AbstractTech tech, boolean isFocus) {
        // 检查科技是否存在
        if (!techMap.containsKey(tech.getIdentifier())) {
            return;
        }

        TechInstance instance = techMap.get(tech.getIdentifier());

        // 如果要设置focus，但科技是LOCKED状态，则不允许设置服务端focus
        if (isFocus && instance.getState().equals(TechState.LOCKED)) {
            return;
        }

        // 取消所有其他科技的focus状态
        for (var techInstance : techMap.values()) {
            if (techInstance.isFocused()) {
                techInstance.setFocused(false);
            }
        }

        // 设置当前科技的focus状态
        instance.setFocused(isFocus);
    }




    @Override
    public void tryNext(AbstractTech tech) {
        var instance = techMap.getOrDefault(tech.getIdentifier(), null);
        if (instance == null) {
            return; // 科技不存在，直接返回
        }

        // 获取当前科技的所有子节点
        List<ResourceLocation> children = getChildren(tech);
        
        // 1. 没有子节点：直接返回，保持当前状态
        if (children.isEmpty()) {
            return;
        }
        
        // 2. 只有一个子节点：当前科技保持完成状态，子节点变为可用（需检查所有前置是否完成）
        if (children.size() == 1) {
            // 当前科技应该已经是 COMPLETED 状态（由 tryComplete 设置）
            // 确保当前科技是 COMPLETED 状态
            if (instance.getState() != TechState.COMPLETED) {
                instance.setTechState(TechState.COMPLETED);
            }
            
            // 子节点变为可用状态（仅当所有前置科技都已完成时）
            ResourceLocation childId = children.get(0);
            TechInstance childInstance = techMap.get(childId);
            if (childInstance != null && childInstance.getState() == TechState.LOCKED) {
                // 检查子节点的所有前置科技是否都已完成
                if (areAllParentsCompleted(childInstance)) {
                    childInstance.setTechState(TechState.AVAILABLE);
                    syncStage(childInstance.getTech());
                }
            }
        }
        
        // 3. 有多个子节点：当前科技进入等待选择状态，所有子节点变为可用（需检查各自的前置）
        if (children.size() > 1) {
            // 当前科技进入等待选择状态
            instance.setTechState(TechState.WAITING);
            
            // 所有子节点变为可用状态（仅当它们各自的所有前置科技都已完成时）
            for (ResourceLocation childId : children) {
                TechInstance childInstance = techMap.get(childId);
                if (childInstance != null && childInstance.getState() == TechState.LOCKED) {
                    // 检查子节点的所有前置科技是否都已完成
                    if (areAllParentsCompleted(childInstance)) {
                        childInstance.setTechState(TechState.AVAILABLE);
                        syncStage(childInstance.getTech());
                    }
                }
            }
        }


    }

    /**
     * 检查一个科技的所有前置科技是否都已完成
     *
     * @param techInstance 要检查的科技实例
     * @return 如果所有前置科技都已完成则返回true，否则返回false
     */
    private boolean areAllParentsCompleted(TechInstance techInstance) {
        List<ResourceLocation> parents = techInstance.getParents();

        // 如果没有前置科技，返回true
        if (parents.isEmpty()) {
            return true;
        }

        // 检查每个前置科技的状态
        for (ResourceLocation parentId : parents) {
            TechInstance parentInstance = techMap.get(parentId);

            // 如果前置科技不存在或未完成，返回false
            if (parentInstance == null || parentInstance.getState() != TechState.COMPLETED) {
                return false;
            }
        }

        // 所有前置科技都已完成
        return true;
    }

    //
    @Override
    public void tick(ServerPlayer player, int tickCount) {
        autoSync();
    }

    /**
     * 同步同一阶段的科技，1.没有父节点，2，锁定状态
     * @param tech
     */
    private void syncStage(AbstractTech tech){

        //如果父节点没有科技就升级
        if (getParents(tech).isEmpty() && getStages(tech).size() >1) {
            for (TechInstance instance : techMap.values()) {
                if (getStages(tech).equals(instance.getIdentifier()) && instance.getState().equals(TechState.LOCKED)){
                    instance.setTechState(TechState.AVAILABLE);
                }
            }
        }
    }

    @Override
    public List<ResourceLocation> getStages(AbstractTech tech) {
        if (tech == null) {
            throw new IllegalArgumentException("科技不能为null");
        }
        
        TechBuilder builder = tech.getTechBuilder();
        if (builder == null || builder.stage == -1) {
            return List.of();
        }
        
        // 获取同一阶段的所有科技（包含自身）
        return getStages(builder.stage);
    }

    @Override
    public List<ResourceLocation> getStages(int stage) {
        List<ResourceLocation> result = new ArrayList<>();
        
        // 遍历所有科技实例，查找阶段匹配的科技
        for (TechInstance techInstance : techMap.values()) {
            AbstractTech tech = techInstance.getTech();
            TechBuilder builder = tech.getTechBuilder();
            
            // 检查阶段是否匹配（注意：stage为-1表示未设置阶段）
            if (builder != null && builder.stage != -1 && builder.stage == stage) {
                result.add(tech.getIdentifier());
            }
        }
        
        return result;
    }

    @Override
    public Pair<List<ResourceLocation>, List<ResourceLocation>> getDependencies(AbstractTech tech) {
        if (tech == null) {
            throw new IllegalArgumentException("科技不能为null");
        }

        List<ResourceLocation> parents = getParents(tech);
        List<ResourceLocation> children = getChildren(tech);

        return Pair.of(parents, children);
    }

    @Override
    public List<ResourceLocation> getChildren(AbstractTech tech) {
        if (tech == null) {
            throw new IllegalArgumentException("科技不能为null");
        }
        
        ResourceLocation targetId = tech.getIdentifier();
        List<ResourceLocation> children = new ArrayList<>();
        
        // 遍历所有科技实例，查找父节点包含目标科技的实例
        for (TechInstance techInstance : techMap.values()) {
            List<ResourceLocation> parents = techInstance.getParents();
            if (parents != null && !parents.isEmpty()) {
                // 检查父节点列表中是否包含目标科技
                if (parents.contains(targetId)) {
                    children.add(techInstance.getIdentifier());
                }
            }
        }
        
        return children;
    }

    @Override
    public List<ResourceLocation> getParents(AbstractTech tech) {
        if (tech == null) {
            throw new IllegalArgumentException("科技不能为null");
        }
        
        TechInstance instance = techMap.get(tech.getIdentifier());
        if (instance == null) {
            return List.of(); // 如果科技不存在，返回空列表
        }
        
        return instance.getParents(); // 直接从TechInstance获取父节点列表
    }

    /**
     * 比较差异来快速同步
     * 1.比较AbstractTech
     * 2.比较
     */
    private void autoSync(){
        boolean needsSync = false;
        
        //将tech同步到缓存
        for (TechInstance tech : techMap.values()) {
            var id = tech.getIdentifier();

            //如果使用了add方法
            if (!cacheds.containsKey(id)) {
                cacheds.put(id, tech);
                needsSync = true;
            }
            //如果改变了其他的
            else if (cacheds.containsKey(id)) {
                var cached = cacheds.get(id);
                int result = tech.compareTo(cached);
                if (result != 0){
                    cacheds.put(id, tech);
                    needsSync = true;
                }
            }
        }

        // 找出需要从缓存中移除的key（在cacheds中存在但在techMap中不存在）
        List<ResourceLocation> keysToRemove = new ArrayList<>();
        for (ResourceLocation cachedKey : cacheds.keySet()) {
            if (!techMap.containsKey(cachedKey)) {
                keysToRemove.add(cachedKey);
            }
        }
        
        // 批量移除
        if (!keysToRemove.isEmpty()) {
            for (ResourceLocation key : keysToRemove) {
                cacheds.remove(key);
            }
            needsSync = true;
        }
        
        // 如果需要同步，则同步到客户端
        if (needsSync) {
            syncToClient();
        }
    }
    @Override
    public void syncToClient() {
        getSyncData().syncToClient();
    }







    public SyncData getSyncData() {
        return new SyncData(this);
    }


    public static final Codec<PlayerTechTreeData> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
                Codec.unboundedMap(ResourceLocation.CODEC, TechInstance.CODEC).fieldOf(CACHEDs).forGetter(PlayerTechTreeData::getTechMap),
                Codec.unboundedMap(ResourceLocation.CODEC, Vec2i.CODEC).fieldOf(VEC).forGetter(PlayerTechTreeData::getVecMap),
                Codec.INT.fieldOf(STAGE).forGetter(PlayerTechTreeData::getStage)
        ).apply(instance,PlayerTechTreeData::new));



    @Override
    public Map<ResourceLocation, TechInstance> getCacheds() {
        return cacheds;
    }

    @Override
    public Map<ResourceLocation, Vec2i> getVecMap() {
        return vecMap;
    }

    public Map<ResourceLocation, TechInstance> getTechMap() {
        return techMap;
    }

    //将数据解析为nbt
    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        var dataResult = PlayerTechTreeData.CODEC.encodeStart(NbtOps.INSTANCE, this);

        dataResult.result().ifPresent(nbt->{
            tag.put(TREE_DATA, nbt);
        });

        return tag;
    }


    //将nbt解析回数据
    @Override
    public void deserializeNBT(CompoundTag compoundTag) {
        Tag dataResult = compoundTag.get(TREE_DATA);
        if (dataResult != null) {
            var a = PlayerTechTreeData.CODEC.parse(NbtOps.INSTANCE, dataResult);
            a.result().ifPresent(playerTechTreeData -> {
                Map<ResourceLocation, TechInstance> loadedTechMap = playerTechTreeData.getTechMap();

                // 如果加载的数据不为空，则使用加载的数据
                if (loadedTechMap != null && !loadedTechMap.isEmpty()) {
                    this.techMap = loadedTechMap;
                    this.cacheds = new HashMap<>(loadedTechMap);
                    this.vecMap = playerTechTreeData.getVecMap();
                    this.stage = playerTechTreeData.getStage();

                    // 重新关联 player 到所有 TechInstance
                    if (this.player != null) {
                        for (TechInstance instance : this.techMap.values()) {
                            instance.setServerPlayer(this.player);
                        }
                        for (TechInstance instance : this.cacheds.values()) {
                            instance.setServerPlayer(this.player);
                        }
                    }
                }
                // 如果加载的数据为空，保持构造函数中 initTechSlot() 初始化的数据
            });
        }
        // 如果 compoundTag 中没有 TREE_DATA，保持构造函数中 initTechSlot() 初始化的数据
    }

    public int getStage() {
        return stage;
    }

    public void setPlayer(ServerPlayer player) {
        this.player = player;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

}
