package org.research.api.tech;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.common.util.LazyOptional;
import org.joml.Vector2i;
import org.research.Research;
import org.research.api.event.custom.CompleteTechEvent;
import org.research.api.init.TechInit;
import org.research.api.recipe.IRecipe;
import org.research.api.tech.capability.ITechTreeCapability;
import org.research.api.tech.graphTree.GraphAdjList;
import org.research.api.tech.graphTree.Vec2i;

import java.util.*;

import static org.apache.logging.log4j.core.async.ThreadNameCachingStrategy.CACHED;
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



//    /**
//     * 将注册项初始化为图表，
//     * 每次启动的时候都要重新初始化，不需要持久化存储
//     */
//    public void init() {
//        // 先插入顶点
//        for (TechInstance tech : techMap.values()) {
//            insertVex(tech);
//        }
//
//        // 再插入边
//        for (TechInstance tech : techMap.values()) {
//            List<ResourceLocation> parents = tech.getParents();
//            if (parents != null && !parents.isEmpty()) {
//                for (ResourceLocation parentId : parents) {
//                    TechInstance parentTech = techMap.get(parentId);
//                    if (parentTech != null) {
//                        // 插入从父节点到当前节点的边
//                        insertEdge(parentTech, tech);
//                    }
//                }
//            }
//        }
//    }

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

    }

    @Override
    public PlayerTechTreeData addTech(AbstractTech tech, int x, int y) {
        var techInstance = new TechInstance(tech,player);
        var vec2i = new Vec2i(x,y);

        techMap.put(tech.getIdentifier(),techInstance);
        vecMap.put(tech.getIdentifier(),vec2i);

        return this;
    }




    @Override
    public PlayerTechTreeData removeTech(AbstractTech tech) {
        techMap.remove(tech.getIdentifier());
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
        for (var instance: techMap.values()) {
            if (instance.isFocused()){
                instance.setFocused(false);
            }
        }
        techMap.get(tech.getIdentifier()).setFocused(true);
    }



    //TODO : 等AI额度恢复了来写最复杂的逻辑
    //TOD0 : 没想好怎么写关注切换的问题
    @Override
    public void tryNext(AbstractTech tech) {
        var instance = techMap.getOrDefault(tech.getIdentifier(),null);
        if (instance != null) {

            //遍历科技树的所有的父节点
            List<ResourceLocation> techChildList = new ArrayList<>();
            for (TechInstance techInstance : techMap.values()){
                List<ResourceLocation> parents = techInstance.getParents();
                if (parents != null
                        && !parents.isEmpty()
                        && parents.equals(tech.getIdentifier())){

                    techChildList.add(techInstance.getIdentifier());
                }
            };

            //1.没有任意parens节点
            if (techChildList.isEmpty()) return;
            //2.有一个
            if (techChildList.size() == 1) {
                instance.setTechState(TechState.COMPLETED);
                techMap.get(techChildList.get(0)).setTechState(TechState.AVAILABLE);

            }
            //3.多个节点
            if (techChildList.size()>1){
                instance.setTechState(TechState.WAITING);
                for (ResourceLocation techChild : techChildList) {
                    techMap.get(techChild).setTechState(TechState.AVAILABLE);
                }
            }

        }
    }

    //
    @Override
    public void tick(ServerPlayer player, int tickCount) {
        autoSync();
    }

    //TODO :自动升级同一阶段科技
    private void syncStage(){

    }

    /**
     * 比较差异来快速同步
     * 1.比较AbstractTech
     * 2.比较
     */
    private void autoSync(){
        //将tech同步到缓存
        for (TechInstance tech : techMap.values()) {
            var id = tech.getIdentifier();

            //如果使用了add方法
            if (!cacheds.containsKey(id)) {

                cacheds.put(id, tech);
                syncToClient();
            }

            //如果改变了其他的
            if (cacheds.containsKey(id)) {
                var cached = cacheds.get(id);
                int result = tech.compareTo(cached);
                if (result != 0){

                    cacheds.put(id, tech);
                    syncToClient();
                }
            }
        }

        //如果使用了move方法
        if (cacheds.size() != techMap.size()) {
            //将缓存多余部分移除
            for (var cached : cacheds.keySet()) {
                for (var tech : techMap.keySet()) {
                    if (!cached.equals(tech)){

                        this.cacheds.remove(cached);
                        syncToClient();
                    }
                }
            }
        }
    }
    @Override
    public void syncToClient() {

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
            dataResult.result().ifPresent(nbt->tag.put(TREE_DATA, nbt));
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
    }

    public int getStage() {
        return stage;
    }
}
