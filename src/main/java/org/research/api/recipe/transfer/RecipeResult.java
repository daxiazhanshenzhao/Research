package org.research.api.recipe.transfer;

import lombok.Getter;
import net.minecraft.network.chat.Component;

/**
 * 配方转移结果枚举
 * 表示配方转移操作的各种可能结果
 */
@Getter
public enum RecipeResult {

    /**
     * 成功：配方转移成功
     */
    SUCCESS(0,"recipe.transfer.success"),

    /**
     * 缺少材料：玩家背包中没有足够的材料
     */
    MISSING_MATERIALS(1,"recipe.transfer.missing_materials"),

    /**
     * 容器不匹配：当前打开的容器与配方要求的容器不匹配
     */
    CONTAINER_MISMATCH(2,"recipe.transfer.container_mismatch"),

    /**
     * 槽位被占用：目标槽位已经有物品，无法放置
     */
    SLOTS_OCCUPIED(3,"recipe.transfer.slots_occupied"),

    /**
     * 未知错误：发生了未预期的错误
     */
    UNKNOWN_ERROR(4,"recipe.transfer.unknown_error");

    /** 本地化键 */
    private final String translationKey;
    private final int value;

    RecipeResult(int value, String translationKey) {
        this.value = value;
        this.translationKey = translationKey;
    }

    /**
     * 获取本地化的消息组件
     * @return 本地化消息
     */
    public Component getMessage() {
        return Component.translatable(translationKey);
    }

    public static boolean isSuccess(RecipeResult result) {
        return result == SUCCESS;
    }
}
