# ResearchOverlay 实时数据更新实现

## 问题描述
`ResearchOverlay` 之前只能在打开 `ResearchScreenV2` 工作台时才更新数据，无法实时显示焦点科技的配方变化。

## 根本原因
`ClientOverlayManager` 使用了 `cachedRecipeData` 缓存，但这个缓存**从不清除**，导致即使服务端数据已同步到客户端，Overlay 仍然显示旧的缓存数据。

## 解决方案

### 核心思路
在服务端数据同步到客户端时，**自动清除 `ClientOverlayManager` 的缓存**，强制 Overlay 在下一帧重新获取最新数据。

---

## 实现的修改

### 1. 添加 clearCache() 方法
**文件**: `ClientOverlayManager.java`  
**位置**: 第 67-78 行

```java
// ==================== 缓存管理方法 ====================

/**
 * 清除所有缓存数据
 * 当服务端数据更新时调用，确保 Overlay 显示最新数据
 */
public void clearCache() {
    cachedRecipeData = null;
    cachedCenterHeight = -1;
}
```

**功能**:
- 清除配方数据缓存 (`cachedRecipeData`)
- 清除中心高度缓存 (`cachedCenterHeight`)
- 强制下一次渲染时重新计算

---

### 2. 在数据同步时自动清除缓存
**文件**: `ClientboundSyncPlayerData.java`  
**位置**: 第 27-38 行

**修改前**:
```java
public boolean handle(Supplier<NetworkEvent.Context> supplier){
    NetworkEvent.Context context = supplier.get();
    context.setPacketHandled(true);

    context.enqueueWork(() -> {
        if (syncData != null) {
            ClientResearchData.playerSyncedDataLookup.put(syncData.getPlayerId(), syncData);
        }
    });

    return true;
}
```

**修改后**:
```java
public boolean handle(Supplier<NetworkEvent.Context> supplier){
    NetworkEvent.Context context = supplier.get();
    context.setPacketHandled(true);

    context.enqueueWork(() -> {
        if (syncData != null) {
            ClientResearchData.playerSyncedDataLookup.put(syncData.getPlayerId(), syncData);
            // 清除 OverlayManager 的缓存，确保 Overlay 能实时显示最新数据
            ClientResearchData.getOverlayManager().clearCache();
        }
    });

    return true;
}
```

**说明**:
- 每次服务端发送 `ClientboundSyncPlayerData` 数据包时
- 客户端更新 `playerSyncedDataLookup` 后
- 立即调用 `clearCache()` 清除 Overlay 的缓存

---

## 完整的数据流程

### 服务端 → 客户端 → Overlay 实时更新

```
┌─────────────────────────────────────────────────────────────────────┐
│ 服务端 (Server)                                                       │
├─────────────────────────────────────────────────────────────────────┤
│                                                                       │
│  1. 玩家完成科技 / 切换焦点                                             │
│     ↓                                                                │
│  2. PlayerTechTreeData 状态变化                                       │
│     ↓                                                                │
│  3. autoSync() 检测到数据哈希值变化                                     │
│     ↓                                                                │
│  4. 创建 SyncData 并发送 ClientboundSyncPlayerData 数据包              │
│                                                                       │
└───────────────────────────────┬─────────────────────────────────────┘
                                │ 网络传输
                                ↓
┌─────────────────────────────────────────────────────────────────────┐
│ 客户端 (Client)                                                       │
├─────────────────────────────────────────────────────────────────────┤
│                                                                       │
│  5. ClientboundSyncPlayerData.handle() 接收数据包                     │
│     ↓                                                                │
│  6. 更新 ClientResearchData.playerSyncedDataLookup                   │
│     ↓                                                                │
│  7. 调用 ClientResearchData.getOverlayManager().clearCache() ✨ 新增  │
│     ↓                                                                │
│  8. cachedRecipeData = null                                         │
│     cachedCenterHeight = -1                                         │
│                                                                       │
└───────────────────────────────┬─────────────────────────────────────┘
                                │ 下一帧渲染
                                ↓
┌─────────────────────────────────────────────────────────────────────┐
│ ResearchOverlay 渲染 (每帧都调用)                                      │
├─────────────────────────────────────────────────────────────────────┤
│                                                                       │
│  9. ResearchOverlay.render() 被调用                                  │
│     ↓                                                                │
│ 10. manager.getRecipeDisplayData()                                  │
│     ↓                                                                │
│ 11. 检查 cachedRecipeData                                            │
│     └─> null（已被清除）✨                                            │
│     ↓                                                                │
│ 12. 从 ClientResearchData.getSyncData() 获取最新数据                  │
│     ↓                                                                │
│ 13. syncData.getFocusTech() 获取焦点科技 ID                           │
│     ↓                                                                │
│ 14. syncData.getCacheds().get(focusTechId) 获取 TechInstance         │
│     ↓                                                                │
│ 15. 解析配方并缓存                                                     │
│     ↓                                                                │
│ 16. 渲染最新的配方信息 ✅                                              │
│                                                                       │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 关键时机

### 何时清除缓存？
✅ **每次服务端同步数据时** (`ClientboundSyncPlayerData.handle()`)

### 何时重新获取数据？
✅ **下一帧渲染时** (`ResearchOverlay.render()`)

### 数据同步频率
✅ **服务端自动检测** (`PlayerTechTreeData.autoSync()` 在每个 tick 调用)
- 通过计算 techMap 的哈希值判断数据是否变化
- 只有变化时才发送数据包（节省带宽）

---

## 实现效果对比

### 修改前 ❌
| 场景 | 行为 |
|------|------|
| 完成科技 | Overlay 不更新 |
| 切换焦点 | Overlay 不更新 |
| 打开工作台 | Overlay 更新 ✓ |
| 关闭工作台 | Overlay 保持旧数据 |

### 修改后 ✅
| 场景 | 行为 |
|------|------|
| 完成科技 | Overlay 实时更新 ✓ |
| 切换焦点 | Overlay 实时更新 ✓ |
| 打开工作台 | Overlay 实时更新 ✓ |
| 关闭工作台 | Overlay 持续实时更新 ✓ |

---

## 性能考虑

### 缓存策略
- ✅ 数据变化时才清除缓存（不是每帧都清除）
- ✅ 清除后立即重新缓存，不会每帧都重新计算
- ✅ 服务端只在数据变化时才发送数据包

### 网络开销
- ✅ 服务端使用哈希值判断，避免不必要的同步
- ✅ 数据包只包含必要的信息（科技 ID、状态、焦点）

### 渲染开销
- ✅ 缓存配方数据和中心高度
- ✅ 只在数据变化时重新解析配方
- ✅ 文本渲染使用预计算的坐标

---

## 测试验证

### 测试用例 1: 完成科技自动更新
1. 玩家完成一个科技
2. 父节点状态变为 COMPLETED 或 WAITING
3. 子节点自动解锁并获得焦点
4. **验证**: Overlay 立即显示新焦点科技的配方（无需打开工作台）

### 测试用例 2: 手动切换焦点
1. 玩家手动点击切换焦点科技
2. 服务端 `focus()` 方法更新焦点
3. `autoSync()` 检测到变化并同步
4. **验证**: Overlay 立即显示新焦点科技的配方

### 测试用例 3: 多路径选择
1. 完成有多个子节点的科技
2. 父节点变为 WAITING，所有子节点解锁
3. 焦点保持在父节点（等待玩家选择）
4. **验证**: Overlay 显示父节点的配方
5. 玩家选择一个子节点
6. **验证**: Overlay 立即更新为子节点的配方

### 测试用例 4: 长期游戏
1. 玩家连续完成多个科技
2. 每次完成都会自动切换焦点
3. **验证**: Overlay 每次都实时更新，无延迟
4. **验证**: 无内存泄漏（缓存正常清除和重建）

---

## 代码审查要点

### ✅ 线程安全
- `ClientboundSyncPlayerData.handle()` 在主线程执行（`context.enqueueWork()`）
- `clearCache()` 和 `getRecipeDisplayData()` 都在主线程调用
- 无并发问题

### ✅ 空值处理
- `syncData` 为 null 时不清除缓存
- `getRecipeDisplayData()` 返回 null 时 Overlay 渲染空数据
- 所有 null 检查已就位

### ✅ 缓存一致性
- 清除缓存后，下次调用 `getRecipeDisplayData()` 一定会重新获取
- 缓存变量都正确初始化为 null 或 -1

---

## 相关文档
- [自动聚焦解锁子节点功能实现](AUTO_FOCUS_ON_UNLOCK.md)
- [ClientOverlayManager 实时数据获取优化](OVERLAY_REALTIME_DATA.md)

---

## 日期
2026-02-05

## 版本历史
- v1.0: 初始实现 - 从 TechSlotData 获取数据
- v1.1: 优化 - 直接从 SyncData 获取数据
- v1.2: **完善 - 添加自动缓存清除机制，实现实时更新** ✅
