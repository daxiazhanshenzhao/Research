package org.research.api.tech;

/**
 * 科技状态枚举。
 * <p>
 * 表示科技树中科技的不同状态。
 * </p>
 */
public enum TechState {

    /**
     * 锁定状态 - 科技尚未解锁
     */
    LOCKED(0),

    /**
     * 可用状态 - 科技已解锁，可以开始研究但尚未完成。
     */
    AVAILABLE(1),

    /**
     * 等待状态 - 科技树已经解锁 - 存在两个分支需要玩家手动选择
     */
    WAITING(2),
    /**
     * 完成状态 - 科技研究已完成，相关效果已激活。
     */
    COMPLETED(3);

    private TechState(final int value) {
        this.value = value;
    }

    private final int value;

    public int getValue() {
        return value;
    }


    public boolean isBlackBg(){
        return this.equals(LOCKED);
    }
    public boolean isLocked(){
        return this.equals(LOCKED);
    }
}
