package org.research.api.tech.capability;

import com.mojang.datafixers.util.Pair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.research.api.tech.AbstractTech;
import org.research.api.tech.SyncData;
import org.research.api.tech.TechInstance;
import org.research.api.tech.TechState;
import org.research.api.tech.graphTree.Vec2i;

import java.util.List;
import java.util.Map;

public interface ITechTreeCapability<T> {


    /**
     * 对初始化addTech
     */
    void initTechSlot();

    /**
     * 初始化的时候用，或者动态使用，用于添加科技树
     * @param tech 科技
     * @param x 相对于背景的x坐标
     * @param y 相对于背景的y坐标
     */
    T addTech(AbstractTech tech, int x, int y);

    /**
     * 动态移除科技
     * @param tech
     */
    T removeTech(AbstractTech tech);

    /**
     * 转移科技状态专用方法
     * @param tech
     * @param state
     */
    void changeTech(AbstractTech tech, TechState state);

    /**
     * 尝试推进一个节点，如果有多个子节点，就设置{@link TechState#WAITING}
     */
    void tryNext(AbstractTech tech);

    /**
     * hook外部tick
     */
    void tick(ServerPlayer player,int tickCount);

    TechInstance getFirstTech();

    void syncToClient();

    Map<ResourceLocation, Vec2i> getVecMap();

    CompoundTag serializeNBT();
    void deserializeNBT(CompoundTag compoundTag);

    /**
     * inventory发生变动时触发，尝试遍历所有的，只在服务端进入游戏过后触发
     * @param itemStack 变动的物品
     */
    void tryComplete(ItemStack itemStack);

    Map<ResourceLocation, TechInstance> getTechMap();

    void focus(ResourceLocation techId);

    void clearFocus();

    ResourceLocation getFocus();

    void setPlayer(ServerPlayer player);

    /**
     * 获取指定科技的完整依赖关系。
     * @param tech 要查询依赖关系的科技实例
     * @return 包含父节点列表和子节点列表的 {@code Pair}。
     *         第一个列表是父节点（前置科技），第二个列表是子节点（后续科技）。
     *         如果科技不存在或没有依赖关系，返回包含两个空列表的 {@code Pair}。
     */
    Pair<List<ResourceLocation>,List<ResourceLocation>> getDependencies(AbstractTech tech);

    /**
     * 获取指定科技的所有父节点（前置科技）。
     * @param tech 要查询父节点的科技实例
     * @return 包含所有父节点资源位置的列表。如果科技没有父节点，返回空列表。
     *         列表中的顺序不保证任何特定顺序。

     */
    List<ResourceLocation> getParents(AbstractTech tech);

    /**
     * 获取指定科技的所有子节点（后续科技）。
     * @param tech 要查询子节点的科技实例
     * @return 包含所有子节点资源位置的列表。如果科技没有子节点，返回空列表。
     *         列表中的顺序不保证任何特定顺序。
     *

     */
    List<ResourceLocation> getChildren(AbstractTech tech);

    /**
     * 获取指定科技所在阶段的所有科技。
     * <p>
     * 返回的列表包含指定科技本身及其同一阶段的所有其他科技。
     * 如果科技没有设置阶段（stage为-1），返回空列表。
     * </p>
     *
     * @param tech 要查询的科技实例
     * @return 包含指定科技所在阶段所有科技资源位置的列表。
     *         如果科技不存在或没有设置阶段，返回空列表。
     */
    List<ResourceLocation> getStages(AbstractTech tech);

    /**
     * 获取指定阶段的所有科技。
     * <p>
     * 返回指定阶段编号的所有科技资源位置。
     * </p>
     *
     * @param stage 要查询的阶段编号
     * @return 包含指定阶段所有科技资源位置的列表。
     *         如果该阶段没有科技，返回空列表。
     */
    List<ResourceLocation> getStages(int stage);
    SyncData getSyncData();

    void resetAllTech();
}
