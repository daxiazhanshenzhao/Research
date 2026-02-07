package org.research.api.event.custom;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Cancelable;
import org.research.api.tech.TechInstance;
import org.research.api.tech.TechState;

/**
 * 科技阶段变化事件
 * 当科技状态发生变化时触发
 * 可以通过取消事件来阻止状态变化
 */
@Getter
@Cancelable
public class ChangeTechStageEvent extends PlayerEvent {

    /** 关联的科技实例 */
    private final TechInstance techInstance;

    /** 旧的科技状态 */
    private final TechState oldState;

    /** 新的科技状态 */
    @Setter
    private TechState newState;

    /**
     * 构造函数
     * @param oldState 旧的科技状态
     * @param newState 新的科技状态
     * @param techInstance 科技实例
     * @param player 玩家
     */
    public ChangeTechStageEvent(TechState oldState, TechState newState, TechInstance techInstance, ServerPlayer player) {
        super(player);
        this.oldState = oldState;
        this.newState = newState;
        this.techInstance = techInstance;
    }

}
