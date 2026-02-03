package org.research.api.gui.wrapper;

import net.minecraft.resources.ResourceLocation;
import org.research.api.tech.SyncData;
import org.research.api.util.Vec2i;
import org.research.gui.minecraft.component.TechSlot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 客户端缓存数据管理类
 * 使用写时复制（Copy-on-Write）模式，确保外界遍历时不会因数据改变而出错
 *
 * 性能优化：
 * - 复用 ArrayList 实例，减少 GC 压力
 * - 双重检查锁定，高频读取零开销
 */
public class TechSlotData {

    // 脏数据标记：true表示数据已改变，需要重新创建快照
    private volatile boolean dirty = false;

    // 内部可变列表：复用实例，避免频繁创建对象
    private final List<TechSlot> cache = new ArrayList<>();

    // 不可变快照：用于外界安全读取
    private List<TechSlot> snapshot = Collections.emptyList();
    private TechSlot focusTechSlot = TechSlot.EMPTY;
    // 缓存的哈希值：用于高频调用时快速判断数据是否变化
    private int cachedDataHash = 0;

    /**
     * 设置缓存的 TechSlot 列表（复用内部 list，性能最优）
     *
     * @param techSlots 新的 TechSlot 列表
     */
    public synchronized void setCachedTechSlots(List<TechSlot> techSlots) {
        cache.clear();
        if (techSlots != null && !techSlots.isEmpty()) {
            cache.addAll(techSlots);
        }
        markDirty();
    }

    /**
     * 添加单个 TechSlot
     */
    public synchronized void addTechSlot(TechSlot techSlot) {
        if (techSlot != null) {
            cache.add(techSlot);
            markDirty();
        }
    }

    /**
     * 移除单个 TechSlot
     */
    public synchronized boolean removeTechSlot(TechSlot techSlot) {
        if (cache.remove(techSlot)) {
            markDirty();
            return true;
        }
        return false;
    }

    /**
     * 清空所有 TechSlot
     */
    public synchronized void clearTechSlots() {
        if (!cache.isEmpty()) {
            cache.clear();
            markDirty();
        }
        // 清空时重置哈希值
        cachedDataHash = 0;
    }

    /**
     * 检查数据哈希值是否匹配（用于快速判断数据是否变化）
     *
     * @param currentHash 当前数据的哈希值
     * @return true 表示哈希值匹配，数据未变化；false 表示需要更新
     */
    public boolean isHashMatched(int currentHash) {
        return cachedDataHash != 0 && cachedDataHash == currentHash;
    }

    /**
     * 更新缓存的哈希值
     *
     * @param newHash 新的哈希值
     */
    public void updateHash(int newHash) {
        this.cachedDataHash = newHash;
    }

    /**
     * 获取当前缓存的哈希值
     *
     * @return 缓存的哈希值
     */
    public int getCachedHash() {
        return cachedDataHash;
    }

    /**
     * 重置哈希值（用于失效缓存）
     */
    public void resetHash() {
        this.cachedDataHash = 0;
    }

    /**
     * 标记数据为脏（内联方法，减少代码重复）
     */
    private void markDirty() {
        dirty = true;
    }

    /**
     * 获取缓存的 TechSlot 列表（高性能，零GC）
     *
     * 性能优化：
     * - 快速路径：数据未改变时直接返回快照（纳秒级）
     * - 慢速路径：数据改变时才创建新快照
     * - 双重检查锁定，避免不必要的同步
     *
     * @return 不可变的 TechSlot 列表快照
     */
    public List<TechSlot> getCachedTechSlots() {
        // 快速路径：99% 的情况走这里，零开销
        if (!dirty) {
            return snapshot;
        }

        // 慢速路径：数据改变时才进入
        synchronized (this) {
            if (dirty) {
                snapshot = List.copyOf(cache);
                dirty = false;
            }
            return snapshot;
        }
    }

    public void clearFocus(){

    }

    public void setFocusTechSlot(TechSlot techSlot) {

    }
    /**
     * 获取 TechSlot 数量
     */
    public int size() {
        return cache.size();
    }

    /**
     * 检查是否为空
     */
    public boolean isEmpty() {
        return cache.isEmpty();
    }


    /**
     * 简化版本的 initializePositions，直接使用 vecMap 中的值
     * 使用 try-catch 处理反射异常
     */
    public synchronized void initializePositionsWithVecMap(Object vecMap, int guiLeftOffset, int guiTopOffset) {
        if (vecMap == null || isEmpty()) {
            return;
        }

        if (!(vecMap instanceof Map)) {
            return;
        }

        try {
            @SuppressWarnings("unchecked")
            Map<Object, Object> map = (Map<Object, Object>) vecMap;

            for (var techSlot : cache) {
                var techId = techSlot.getTechInstance().getIdentifier();
                var vec = map.get(techId);
                if (vec != null) {
                    // 使用反射获取 x 和 y
                    int x = (int) vec.getClass().getMethod("x").invoke(vec);
                    int y = (int) vec.getClass().getMethod("y").invoke(vec);

                    // 计算新位置
                    int newX = guiLeftOffset + x;
                    int newY = guiTopOffset + y;
                    techSlot.setPosition(newX, newY);
                }
            }
        } catch (Exception e) {
            // 如果出现异常，静默处理
            e.printStackTrace();
        }
    }
}
