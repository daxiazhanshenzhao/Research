package org.research.api.tech.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector2i;
import org.research.api.tech.AbstractTech;
import org.research.api.tech.TechInstance;
import org.research.api.tech.TechState;
import org.research.api.tech.graphTree.Vec2i;

import java.util.HashMap;
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

    Map<ResourceLocation, TechInstance> getCacheds();
    Map<ResourceLocation, Vec2i> getVecMap();

    CompoundTag serializeNBT();
    void deserializeNBT(CompoundTag compoundTag);

    /**
     * inventory发生变动时触发，尝试遍历所有的，只在服务端进入游戏过后触发
     * @param itemStack 变动的物品
     */
    void tryComplete(ItemStack itemStack);


    /**
     *
     * @param tech
     */
    void focus(AbstractTech tech);

    ResourceLocation getFocus();

    void setPlayer(ServerPlayer player);

}
