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

    public PlayerTechTreeData(Map<ResourceLocation,TechInstance> techMap,Map<ResourceLocation,Vec2i> vecMap,int stage) {
        this(null);
        this.techMap = techMap;
        this.vecMap = vecMap;
        this.stage = stage;


    }

    @Override
    public TechInstance getFirstTech() {
        TechInstance techinstance = new TechInstance(TechInit.FIRST_TECH.get(),player);
        techinstance.setTechState(TechState.COMPLETED);
        return techinstance;
    }



    /**
     * 重写nextNode方法，添加状态管理
     */

    @Override
    public void initTechSlot() {
        addTech(TechInit.FIRST_TECH.get(),100,100);
        addTech(TechInit.IRON_TECH.get(),474,292);
        addTech(TechInit.APP_TECH.get(),0,0);
    }

    @Override
    public PlayerTechTreeData addTech(AbstractTech tech, int x, int y) {
        if (techMap.containsKey(tech.getIdentifier())) return this;

        var techInstance = new TechInstance(tech,player);
        var vec2i = new Vec2i(x,y);

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

            var recipeWrapperData = tech.getRecipe();
            var registryAccess = player.server.registryAccess();
            var recipe = IRecipe.getRecipeFromWrapper(recipeWrapperData, player.server);
            if (recipe != null) {
                ItemStack recipeOutput = recipe.getResultItem(registryAccess);
                if (ItemStack.isSameItemSameTags(recipeOutput, itemStack)) {

                    tech.setTechState(TechState.COMPLETED);

                    focus(tech.getTech());
                    tryNext(tech.getTech());


                }
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
    public void focus(AbstractTech tech) {
        //遍历所有将focus设置为false
        if (techMap.get(tech.getIdentifier()).getState().equals(TechState.LOCKED)) return;

        for (var instance: techMap.values()) {
            if (instance.isFocused()){
                instance.setFocused(false);
            }
        }
        techMap.get(tech.getIdentifier()).setFocused(true);
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
        
        // 2. 只有一个子节点：当前科技保持完成状态，子节点变为可用
        if (children.size() == 1) {
            // 当前科技应该已经是 COMPLETED 状态（由 tryComplete 设置）
            // 确保当前科技是 COMPLETED 状态
            if (instance.getState() != TechState.COMPLETED) {
                instance.setTechState(TechState.COMPLETED);
            }
            
            // 子节点变为可用状态
            ResourceLocation childId = children.get(0);
            TechInstance childInstance = techMap.get(childId);
            if (childInstance != null && childInstance.getState() == TechState.LOCKED) {
                childInstance.setTechState(TechState.AVAILABLE);

                syncStage(childInstance.getTech());
            }
        }
        
        // 3. 有多个子节点：当前科技进入等待选择状态，所有子节点变为可用
        if (children.size() > 1) {
            // 当前科技进入等待选择状态
            instance.setTechState(TechState.WAITING);
            
            // 所有子节点变为可用状态
            for (ResourceLocation childId : children) {
                TechInstance childInstance = techMap.get(childId);
                if (childInstance != null && childInstance.getState() == TechState.LOCKED) {
                    childInstance.setTechState(TechState.AVAILABLE);

                    syncStage(childInstance.getTech());
                }
            }
        }


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
                Codec.unboundedMap(ResourceLocation.CODEC, TechInstance.CODEC).fieldOf(CACHEDs).forGetter(PlayerTechTreeData::getCacheds),
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

    //将数据解析为nbt
    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
            var dataResult = PlayerTechTreeData.CODEC.encodeStart(NbtOps.INSTANCE, this);

            dataResult.result().ifPresent(nbt->{
                tag.put(STAGE, nbt);
                getSyncData().saveNbt(tag);
            });

        return tag;
    }


    //将nbt解析回数据
    @Override
    public void deserializeNBT(CompoundTag compoundTag) {
        Tag dataResult = compoundTag.get(TREE_DATA);
        var a = PlayerTechTreeData.CODEC.parse(NbtOps.INSTANCE,dataResult);
        a.result().ifPresent(playerTechTreeData ->{
                    this.cacheds = playerTechTreeData.getCacheds();
                    this.vecMap = playerTechTreeData.getVecMap();
                }
        );

        getSyncData().loadNbt(compoundTag);
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
