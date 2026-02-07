package org.research.api.event.custom;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Cancelable;
import org.research.api.tech.TechInstance;

/**
 * 科技焦点变化事件
 * 当玩家改变科技焦点状态时触发
 * 可以通过取消事件来阻止焦点变化
 */
@Getter
@Cancelable
public class ChangeTechFocusedEvent extends PlayerEvent {

    /** 旧的焦点状态 */
    private final boolean oldFocused;

    /** 新的焦点状态 */
    @Setter
    private boolean newFocused;

    /** 关联的科技实例 */
    private final TechInstance techInstance;

    /**
     * 构造函数
     * @param oldFocused 旧的焦点状态
     * @param newFocused 新的焦点状态
     * @param techInstance 科技实例
     * @param player 玩家
     */
    public ChangeTechFocusedEvent(boolean oldFocused, boolean newFocused, TechInstance techInstance, Player player) {
        super(player);
        this.oldFocused = oldFocused;
        this.newFocused = newFocused;
        this.techInstance = techInstance;
    }


    /**
     * 检查焦点状态是否发生变化
     * @return 如果状态发生变化返回 true，否则返回 false
     */
    public boolean hasChanged() {
        return oldFocused != newFocused;
    }
}
