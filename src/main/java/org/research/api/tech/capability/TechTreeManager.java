package org.research.api.tech.capability;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import org.research.api.event.custom.ChangeTechFocusedEvent;
import org.research.api.event.custom.ChangeTechStageEvent;
import org.research.api.event.custom.CompleteTechEvent;
import org.research.api.init.PacketInit;
import org.research.api.init.TechInit;
import org.research.api.tech.*;
import org.research.api.util.Vec2i;
import org.research.network.research.PlayTechSoundPacket;

import java.util.*;

import static org.research.api.tech.capability.TechTreeDataProvider.*;

//只在服务端控制
public class TechTreeManager implements ITechTreeManager<TechTreeManager> {

    private ServerPlayer player;

    /**
     * only server - 服务端科技数据
     */
    private Map<ResourceLocation, TechInstance> techMap = new HashMap<>();

    private Map<ResourceLocation,Vec2i> vecMap = new HashMap<>();
    private int stage = 1;

    /**
     * 上次同步时的 techMap 哈希值，用于判断数据是否变化
     */
    private int lastTechMapHash = 0;

    /**
     * 缓存的 SyncData 对象，避免每次 tick 都创建新对象
     */
    private SyncData cachedSyncData = null;

    public TechTreeManager(ServerPlayer player) {
//        super(TechInit.getAllTech().size());
        this.player = player;
//        current = getCurrent();
        initTechSlot();

    }

    // 用于反序列化的构造函数，不调用 initTechSlot()
    public TechTreeManager(Map<ResourceLocation,TechInstance> techMap, Map<ResourceLocation,Vec2i> vecMap, int stage) {
        this.player = null;
        this.techMap = techMap;
        this.vecMap = vecMap;
        this.stage = stage;
        // 初始化哈希值，使得下一次 tick 能正确判断数据是否变化
        this.lastTechMapHash = calculateTechMapHash();
    }

    @Override
    public TechInstance getFirstTech() {
        TechInstance techinstance = new TechInstance(TechInit.A_TECH.get(),player);
        techinstance.setTechState(TechState.AVAILABLE);
        return techinstance;
    }



    @Override
    public void initTechSlot() {
        //stage1
        addTech(TechInit.A_TECH.get(),239+294,471+157) //533,628
                //stage2
                .addTech(TechInit.B_TECH.get(),239+294,428+157)
                //stage3
                .addTech(TechInit.C_TECH.get(),239+294,387+157)
                .addTech(TechInit.D_TECH.get(),277+294,387+157)
                .addTech(TechInit.E_TECH.get(),199+294,387+157)
                .addTech(TechInit.F_TECH.get(),316+294,387+157)
                //stage4
                .addTech(TechInit.G_TECH.get(),277+294,353+157)
                .addTech(TechInit.H_TECH.get(),239+294,353+157)
                .addTech(TechInit.I_TECH.get(),199+294,353+157)
                .addTech(TechInit.J_TECH.get(),156+294,353+157)
                //stage5
                .addTech(TechInit.K_TECH.get(),156+294,319+157)
                .addTech(TechInit.L_TECH.get(),199+294,319+157)
                .addTech(TechInit.M_TECH.get(),239+294,319+157)
                //stage6
                .addTech(TechInit.N_TECH.get(),219+294,279+157)
                .addTech(TechInit.O_TECH.get(),262+294,279+157)

                //empty_tech
                .addTech(TechInit.EMPTY_TECH.get(),0,0);
    }

    @Override
    public TechTreeManager addTech(AbstractTech tech, int x, int y) {
        if (techMap.containsKey(tech.getIdentifier())) return this;

        var techInstance = new TechInstance(tech,player);
        var vec2i = new Vec2i(x,y);

        //对第一个tech设置特殊状态
        if (techInstance.equals(getFirstTech())){
            techInstance.setTechState(TechState.AVAILABLE);
        }

        techMap.put(tech.getIdentifier(),techInstance);
        vecMap.put(tech.getIdentifier(),vec2i);

        return this;
    }




    @Override
    public TechTreeManager removeTech(AbstractTech tech) {
        if (techMap.containsKey(tech.getIdentifier())) {
            techMap.remove(tech.getIdentifier());
            vecMap.remove(tech.getIdentifier());
        }
        return this;
    }

    // ==================== 带事件触发的状态修改方法 ====================

    /**
     * 修改科技状态（带事件触发）
     * 触发 ChangeTechStageEvent 事件，可以被取消或修改
     *
     * @param techInstance 科技实例
     * @param newState 新的科技状态
     * @return 是否成功修改（false 表示事件被取消）
     */
    public boolean changeTechStateWithEvent(TechInstance techInstance, TechState newState) {
        if (techInstance == null || player == null) {
            return false;
        }

        TechState oldState = techInstance.getState();

        // 触发事件
        ChangeTechStageEvent event = new ChangeTechStageEvent(oldState, newState, techInstance, player);
        if (MinecraftForge.EVENT_BUS.post(event)) {
            // 事件被取消
            return false;
        }

        // 应用事件中可能被修改的状态
        techInstance.setTechState(event.getNewState());
        return true;
    }

    /**
     * 修改科技焦点状态（带事件触发）
     * 触发 ChangeTechFocused 事件，可以被取消或修改
     *
     * @param techInstance 科技实例
     * @param newFocused 新的焦点状态
     * @return 是否成功修改（false 表示事件被取消）
     */
    public boolean changeTechFocusedWithEvent(TechInstance techInstance, boolean newFocused) {
        if (techInstance == null || player == null) {
            return false;
        }

        boolean oldFocused = techInstance.isFocused();

        // 触发事件
        ChangeTechFocusedEvent event = new ChangeTechFocusedEvent(oldFocused, newFocused, techInstance, player);
        if (MinecraftForge.EVENT_BUS.post(event)) {
            // 事件被取消
            return false;
        }

        // 应用事件中可能被修改的焦点状态（boolean 字段 Lombok 生成的是 isNewFocused()）
        techInstance.setFocused(event.isNewFocused());
        return true;
    }

    /**
     * 完成科技（带事件触发）
     * 触发 CompleteTechEvent 事件，可以被取消或修改完成后的状态
     *
     * @param techInstance 科技实例
     * @param completedState 完成后的状态（通常是 COMPLETED 或 WAITING）
     * @return 是否成功完成（false 表示事件被取消）
     */
    public boolean completeTechWithEvent(TechInstance techInstance, TechState completedState) {
        if (techInstance == null || player == null) {
            return false;
        }

        TechState oldState = techInstance.getState();

        // 触发事件
        CompleteTechEvent event = new CompleteTechEvent(oldState, completedState, techInstance, player);
        if (MinecraftForge.EVENT_BUS.post(event)) {
            // 事件被取消
            return false;
        }

        // 应用事件中可能被修改的状态
        techInstance.setTechState(event.getNewState());
        return true;
    }

    // ==================== 原有方法 ====================

    @Override
    public void changeTech(AbstractTech target, TechState state) {
        var instance = techMap.getOrDefault(target.getIdentifier(), null);
        if (instance != null) {
            // 使用带事件触发的方法
            changeTechStateWithEvent(instance, state);
        }
    }

    @Override
    public void tryComplete(ItemStack itemStack) {
        for (TechInstance tech : techMap.values()) {
            var output = tech.getRecipeOutput();
            // 只有当科技处于 AVAILABLE 状态时才能尝试完成
            if (!output.isEmpty()
                    && ItemStack.isSameItemSameTags(output, itemStack)
                    && tech.getState() == TechState.AVAILABLE) {

                // 检查所有前置科技是否都已完成
                var parents = getParents(tech.getTech());
                boolean allParentsCompleted = true;
                for (var parentId : parents) {
                    TechInstance parentInstance = techMap.get(parentId);
                    if (parentInstance == null || parentInstance.getState() != TechState.COMPLETED) {
                        allParentsCompleted = false;
                        break;
                    }
                }

                // 如果所有前置科技都已完成，则完成当前科技
                if (allParentsCompleted) {
                    // 根据子节点数量决定科技的最终状态
                    List<ResourceLocation> children = getChildren(tech.getTech());

                    boolean isWaiting = false;
                    if (children.size() > 1) {
                        // 有多个子节点：设置为 WAITING 状态，等待玩家选择（带事件触发）
                        completeTechWithEvent(tech, TechState.WAITING);
                        isWaiting = true;
                    } else {
                        // 只有一个或没有子节点：设置为 COMPLETED 状态（带事件触发）
                        completeTechWithEvent(tech, TechState.COMPLETED);
                    }
                    playerSound();
                    // 播放科技完成音效


                    syncStage(tech.getTech());

                    // 先执行 tryNext 解锁子节点
                    tryNext(tech.getTech());

                    // 只有当状态不是 WAITING 时，才自动聚焦到下一个科技
                    // 如果是 WAITING 状态，等待玩家手动选择
                    if (!isWaiting) {
                        autoFocusNextTech(tech.getTech());
                    } else {
                        // WAITING 状态：保持焦点在当前科技，等待玩家选择
                        focus(tech.getIdentifier());
                        player.sendSystemMessage(Component.literal("Multiple paths available. Please choose next tech manually."));
                    }
                }
            }
        }
    }

    /**
     * 播放科技完成音效
     * 发送网络包到客户端，在客户端播放声音
     */
    private void playerSound() {
        if (player != null) {
            PacketInit.sendToPlayer(new PlayTechSoundPacket(), player);
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
    public void focus(ResourceLocation techId) {
        // 检查科技是否存在
        if (!techMap.containsKey(techId)) {
            return;
        }

        TechInstance instance = techMap.get(techId);

        // 清除之前的焦点
        clearFocus();

        // 检查当前科技是否为 COMPLETED 状态且有多个子节点
        if (instance.getState().equals(TechState.COMPLETED)) {
            List<ResourceLocation> children = getChildren(instance.getTech());
            if (children.size() > 1) {
                // 有多个子节点，将科技变回 WAITING 状态，允许玩家重新选择分支（带事件触发）
                changeTechStateWithEvent(instance, TechState.WAITING);
                player.sendSystemMessage(Component.literal("Tech has multiple branches. Choose your path."));
            }
        }

        // 如果当前科技不是 WAITING 状态，则清除其他 WAITING 状态的科技
        // 这样可以允许手动选择 WAITING 状态的科技
        if (!instance.getState().equals(TechState.WAITING)) {
            clearWaiting();
        }

        // 设置焦点（带事件触发）
        changeTechFocusedWithEvent(instance, true);
        player.sendSystemMessage(Component.literal("Focused on tech: " + techId.toString()));

    }

    private void clearWaiting() {
        for (var techInstance : techMap.values()) {
            if (techInstance.getState().equals(TechState.WAITING)) {
                // 使用带事件触发的方法
                changeTechStateWithEvent(techInstance, TechState.COMPLETED);
            }
        }
    }

    /**
     * 自动将焦点移动到下一个可用的子节点
     * 当玩家完成一个科技后，自动聚焦到后续科技
     *
     * @param completedTech 刚完成的科技
     */
    private void autoFocusNextTech(AbstractTech completedTech) {
        // 获取已完成科技的所有子节点
        List<ResourceLocation> children = getChildren(completedTech);

        if (children.isEmpty()) {
            // 没有子节点，清除焦点
            clearFocus();
            return;
        }

        // 查找第一个 AVAILABLE 状态的子节点
        for (ResourceLocation childId : children) {
            TechInstance childInstance = techMap.get(childId);
            if (childInstance != null && childInstance.getState() == TechState.AVAILABLE) {
                // 找到可用的子节点，自动聚焦（带事件触发）
                clearFocus();
                changeTechFocusedWithEvent(childInstance, true);
                player.sendSystemMessage(Component.literal("Auto-focused on next tech: " + childId.toString()));
                return;
            }
        }

        // 如果没有找到 AVAILABLE 的子节点，检查是否有 WAITING 状态的
        for (ResourceLocation childId : children) {
            TechInstance childInstance = techMap.get(childId);
            if (childInstance != null && childInstance.getState() == TechState.WAITING) {
                // 找到等待中的子节点，自动聚焦（带事件触发）
                clearFocus();
                changeTechFocusedWithEvent(childInstance, true);
                player.sendSystemMessage(Component.literal("Auto-focused on waiting tech: " + childId.toString()));
                return;
            }
        }

        // 如果都没有找到，清除焦点
        clearFocus();
    }

    @Override
    public void clearFocus() {
        for (var techInstance : techMap.values()) {
            if (techInstance.isFocused()) {
                // 使用带事件触发的方法
                changeTechFocusedWithEvent(techInstance, false);
            }
        }

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
        
        // 2. 只有一个子节点：子节点变为可用（需检查所有前置是否完成），并自动聚焦
        if (children.size() == 1) {
            // 子节点变为可用状态（仅当所有前置科技都已完成时）
            ResourceLocation childId = children.get(0);
            TechInstance childInstance = techMap.get(childId);
            if (childInstance != null && childInstance.getState() == TechState.LOCKED) {
                // 检查子节点的所有前置科技是否都已完成
                if (areAllParentsCompleted(childInstance)) {
                    // 使用带事件触发的方法
                    changeTechStateWithEvent(childInstance, TechState.AVAILABLE);
                    syncStage(childInstance.getTech());
                    // 自动聚焦到新解锁的子节点
                    focus(childId);
                }
            }
        }
        
        // 3. 有多个子节点：所有子节点变为可用（需检查各自的前置），并聚焦到第一个解锁的子节点
        // 注意：当前科技的状态已在 tryComplete 中设置为 WAITING
        if (children.size() > 1) {
            boolean firstUnlocked = false;
            // 所有子节点变为可用状态（仅当它们各自的所有前置科技都已完成时）
            for (ResourceLocation childId : children) {
                TechInstance childInstance = techMap.get(childId);
                if (childInstance != null && childInstance.getState() == TechState.LOCKED) {
                    // 检查子节点的所有前置科技是否都已完成
                    if (areAllParentsCompleted(childInstance)) {
                        // 使用带事件触发的方法
                        changeTechStateWithEvent(childInstance, TechState.AVAILABLE);
                        syncStage(childInstance.getTech());
                        // 自动聚焦到第一个解锁的子节点
                        if (!firstUnlocked) {
                            focus(childId);
                            firstUnlocked = true;
                        }
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
            if (parentInstance == null || (parentInstance.getState().isLocked())) {
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
     * 重置所有科技数据，从头开始
     * 清空所有科技进度，重新初始化科技树
     */
    @Override
    public void resetAllTech() {
        // 清空所有数据（techMap 清空后就不需要再调用 clearFocus 了）
        techMap.clear();
        vecMap.clear();
        stage = 1;
        lastTechMapHash = 0;
        cachedSyncData = null;

        // 重新初始化科技树
        initTechSlot();

        // 同步到客户端
        syncToClient();

        // 发送消息通知玩家
        if (player != null) {
            player.sendSystemMessage(Component.literal("所有科技数据已重置！"));
        }
    }

    /**
     * 同步同一阶段的科技，1.没有父节点，2.锁定状态
     * 当完成一个科技时，解锁同一阶段中没有前置科技的其他科技
     * @param tech 刚完成的科技
     */
    private void syncStage(AbstractTech tech){
        // 获取当前科技所在阶段的所有科技ID
        List<ResourceLocation> sameStageTechs = getStages(tech);

        // 如果同一阶段只有一个科技（自己），则不需要同步
        if (sameStageTechs.size() <= 1) {
            return;
        }

        // 遍历同一阶段的所有科技
        for (ResourceLocation techId : sameStageTechs) {
            TechInstance instance = techMap.get(techId);

            // 检查：1. 科技存在  2. 处于锁定状态  3. 没有前置科技
            if (instance != null
                && instance.getState() == TechState.LOCKED
                && getParents(instance.getTech()).isEmpty()) {

                // 解锁这个孤立的科技
                instance.setTechState(TechState.AVAILABLE);
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

        List<ResourceLocation> parents = getParents(tech);
        List<ResourceLocation> children = getChildren(tech);

        return Pair.of(parents, children);
    }

    @Override
    public List<ResourceLocation> getChildren(AbstractTech tech) {

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
     * 自动同步数据到客户端
     * 通过计算 techMap 的哈希值来判断是否需要同步，避免不必要的网络传输和对象创建
     */
    private void autoSync(){
        // 如果 player 未设置，则无法同步数据，直接返回
        if (player == null) {
            return;
        }

        // 计算当前 techMap 的哈希值
        int currentHash = calculateTechMapHash();

        // 如果哈希值发生变化，说明 techMap 已更新，需要同步到客户端
        if (currentHash != lastTechMapHash) {
            // 标记缓存失效，需要重新创建 SyncData
            cachedSyncData = null;
            syncToClient();
            lastTechMapHash = currentHash;
        }
    }

    /**
     * 计算 techMap 的哈希值，用于检测数据变化
     * 包含所有 TechInstance 的状态信息
     */
    private int calculateTechMapHash() {
        int hash = 17;
        hash = 31 * hash + stage;

        // 遍历 techMap，计算每个 TechInstance 的哈希值
        for (var entry : techMap.entrySet()) {
            hash = 31 * hash + entry.getKey().hashCode();
            TechInstance instance = entry.getValue();
            hash = 31 * hash + instance.getStateValue();
            hash = 31 * hash + (instance.isFocused() ? 1 : 0);
        }

        return hash;
    }
    @Override
    public void syncToClient() {
        getSyncData().syncToClient();
    }


    /**
     * 获取同步数据对象
     * 使用缓存机制，只在数据变化时才创建新对象
     */
    public SyncData getSyncData() {
        // 如果缓存失效，重新创建 SyncData
        if (cachedSyncData == null) {
            cachedSyncData = new SyncData(this);
        }
        return cachedSyncData;
    }


    public static final Codec<TechTreeManager> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
                Codec.unboundedMap(ResourceLocation.CODEC, TechInstance.CODEC).fieldOf(CACHEDs).forGetter(TechTreeManager::getTechMap),
                Codec.unboundedMap(ResourceLocation.CODEC, Vec2i.CODEC).fieldOf(VEC).forGetter(TechTreeManager::getVecMap),
                Codec.INT.fieldOf(STAGE).forGetter(TechTreeManager::getStage)
        ).apply(instance, TechTreeManager::new));



    @Override
    public Map<ResourceLocation, Vec2i> getVecMap() {
        return vecMap;
    }

    @Override
    public Map<ResourceLocation, TechInstance> getTechMap() {
        return techMap;
    }

    //将数据解析为nbt
    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        var dataResult = TechTreeManager.CODEC.encodeStart(NbtOps.INSTANCE, this);

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
            var a = TechTreeManager.CODEC.parse(NbtOps.INSTANCE, dataResult);
            a.result().ifPresent(playerTechTreeManager -> {
                Map<ResourceLocation, TechInstance> loadedTechMap = playerTechTreeManager.getTechMap();

                // 如果加载的数据不为空，则使用加载的数据
                if (loadedTechMap != null && !loadedTechMap.isEmpty()) {
                    // 创建新的可变 HashMap，避免不可变集合导致的 UnsupportedOperationException
                    this.techMap = new HashMap<>(loadedTechMap);
                    this.vecMap = new HashMap<>(playerTechTreeManager.getVecMap());
                    this.stage = playerTechTreeManager.getStage();

                    // 重新关联 player 到所有 TechInstance
                    if (this.player != null) {
                        for (TechInstance instance : this.techMap.values()) {
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
        // 当设置 player 时，立即同步数据到客户端
        // 清除缓存，强制重新创建 SyncData
        cachedSyncData = null;
        syncToClient();
    }

    public ServerPlayer getPlayer() {
        return player;
    }

}
