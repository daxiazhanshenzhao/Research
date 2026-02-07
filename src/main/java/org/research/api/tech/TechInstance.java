package org.research.api.tech;


import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.research.api.init.TechInit;
import org.research.api.util.RecipeUtil;
import org.research.api.recipe.RecipeWrapper;
import org.research.api.tech.capability.ITechTreeManager;

import java.util.List;
import java.util.Objects;

public class TechInstance implements Comparable<TechInstance> {

    public static final String ID = "tech_id";
    public static final String STATE = "tech_state";
    public static final String FOCUS = "tech_focus";
    public static final TechInstance EMPTY = new TechInstance(TechInit.EMPTY_TECH.get(),null);

    private AbstractTech tech;
    private ServerPlayer serverPlayer = null;


    /**
     * 查看玩家的点击状态。
     */
    private boolean focused = false;
    private int stateValue = 0;          //TechStage enum


    public TechInstance(AbstractTech tech, ServerPlayer serverPlayer) {
        this.tech = tech;
        this.stateValue = TechState.LOCKED.getValue();
        this.serverPlayer = serverPlayer;
    }

    public TechInstance(ResourceLocation resourceLocation, Integer stageValue, Boolean focused) {
        this.tech = TechInit.getTech(resourceLocation);
        this.stateValue = stageValue;
        this.focused = focused;
    }

    /**
     * 设置科技状态（内部方法，不触发事件）
     * 事件触发在 TechTreeManager 中统一处理
     * @param state 新的科技状态
     */
    public void setTechState(TechState state) {
        this.stateValue = state.getValue();
    }

    /**
     * 设置科技焦点状态（内部方法，不触发事件）
     * 事件触发在 TechTreeManager 中统一处理
     * @param focused 新的焦点状态
     */
    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    /**
     * 获取当前科技状态
     * @return 当前科技状态
     */
    public TechState getState() {
        return getState(stateValue);
    }

    /**
     * 根据状态值获取科技状态枚举
     * @param stateValue 状态值
     * @return 对应的科技状态枚举
     */
    public TechState getState(int stateValue) {
        for (TechState type : TechState.values()) {
            if (type.getValue() == stateValue) {
                return type;
            }
        }
        return TechState.LOCKED;
    }

    public AbstractTech getTech() {
        return tech;
    }

    public ServerPlayer getServerPlayer() {
        return serverPlayer;
    }

    public void setServerPlayer(ServerPlayer serverPlayer) {
        this.serverPlayer = serverPlayer;
    }

    /**
     * 获取当前科技实例的所有父节点（前置科技）。
     * <p>
     * 父节点是解锁当前科技必须先完成的前置科技。该方法从科技的 {@link TechBuilder}
     * 中获取父节点列表。
     * </p>
     * <p>
     * <b>注意：</b>该方法返回的是当前科技实例的父节点，如果需要获取其他科技的父节点，
     * 请使用 {@link ITechTreeManager#getParents(AbstractTech)}。
     * </p>
     *
     * @return 包含所有父节点资源位置的列表。如果科技没有父节点，返回空列表。
     *         列表中的顺序与 {@link TechBuilder} 中添加的顺序一致。
     *
     * @see ITechTreeManager#getParents(AbstractTech)
     * @see TechBuilder#parent
     * @see ResourceLocation
     */
    public List<ResourceLocation> getParents() {
        return tech.getTechBuilder().parent;
    }

    public ResourceLocation getIdentifier() {
        return tech.getIdentifier();
    }

    public int getTechStage(){
        return tech.getTechBuilder().stage;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TechInstance instance = (TechInstance) o;
        return Objects.equals(tech, instance.tech);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(tech);
    }
    @Override
    public int compareTo(TechInstance other) {
        if (this == other) return 0;
        if (other == null) return 1;
        
        // 比较 tech 字段
        if (this.tech != other.tech) {
            if (this.tech == null) return -1;
            if (other.tech == null) return 1;

            ResourceLocation id1 = this.tech.getIdentifier();
            ResourceLocation id2 = other.tech.getIdentifier();
            if (id1 != id2) {
                if (id1 == null) return -1;
                if (id2 == null) return 1;
                int techCompare = id1.compareTo(id2);
                if (techCompare != 0) return techCompare;
            }
        }
        
        // 比较 serverPlayer 字段
        if (this.serverPlayer != other.serverPlayer) {
            if (this.serverPlayer == null) return -1;
            if (other.serverPlayer == null) return 1;
            int playerCompare = this.serverPlayer.getUUID().compareTo(other.serverPlayer.getUUID());
            if (playerCompare != 0) return playerCompare;
        }
        
        // 比较 stateValue 字段
        int stateCompare = Integer.compare(this.stateValue, other.stateValue);
        if (stateCompare != 0) return stateCompare;
        
        // 比较 focused 字段
        return Boolean.compare(this.focused, other.focused);
    }

    //    public List<ResourceLocation> getChildren() {
//        return tech.getTechBuilder().child;
//    }









    public ItemStack getRecipeOutput() {
        var recipeWrapperData = getRecipe();
        var registryAccess = serverPlayer.server.registryAccess();
        var recipe = RecipeUtil.getServerRecipe(recipeWrapperData, serverPlayer.server);
        if (recipe != null) {
            return recipe.getResultItem(registryAccess);
        }else {
            return ItemStack.EMPTY;
        }
    }



    public RecipeWrapper getRecipe() {
        return getTech().getTechBuilder().recipe;
    }


    public boolean isFocused() {
        return focused;
    }

    public int getStateValue() {
        return stateValue;
    }

//    public ARestrictionType getARestrictionType() {
//        return tech.getTechBuilder().restriction;
//    }

    public static final Codec<TechInstance> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            ResourceLocation.CODEC.fieldOf(ID).forGetter(TechInstance::getIdentifier),
            Codec.INT.fieldOf(STATE).forGetter(TechInstance::getStateValue),
                Codec.BOOL.fieldOf(FOCUS).forGetter(TechInstance::isFocused)
        ).apply(instance,TechInstance::new));


    public boolean isEmpty() {
        return this.tech.equals(TechInit.EMPTY_TECH.get());
    }
}
