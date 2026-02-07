package org.research.api.event.custom;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Cancelable;
import org.research.api.tech.TechInstance;
import org.research.api.tech.TechState;

/**
 * 科技完成事件
 * 当玩家完成一个科技时触发
 * 可以通过取消事件来阻止科技完成
 */
@Getter
@Cancelable
public class CompleteTechEvent extends PlayerEvent {

    /** 完成前的科技状态 */
    private final TechState oldState;

    /** 完成后的科技状态（通常是 COMPLETED）*/
    @Setter
    private TechState newState;

    /** 关联的科技实例 */
    private final TechInstance techInstance;

    /**
     * 构造函数
     * @param oldState 旧的科技状态
     * @param newState 新的科技状态
     * @param techInstance 科技实例
     * @param player 玩家
     */
    public CompleteTechEvent(TechState oldState, TechState newState, TechInstance techInstance, Player player) {
        super(player);
        this.oldState = oldState;
        this.newState = newState;
        this.techInstance = techInstance;
    }


    /**
     * 检查科技状态是否发生变化
     * @return 如果状态发生变化返回 true，否则返回 false
     */
    public boolean hasChanged() {
        return oldState != newState;
    }

    /**
     * 检查是否是真正的完成事件（状态变为 COMPLETED）
     * @return 如果新状态是 COMPLETED 返回 true，否则返回 false
     */
    public boolean isCompleting() {
        return newState == TechState.COMPLETED;
    }
}
