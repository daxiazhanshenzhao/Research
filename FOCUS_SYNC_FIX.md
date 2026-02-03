# Focus 数据同步修复文档

## 问题描述

按下 focus 按钮后，服务端会响应并修改数据，但客户端界面没有立即更新。只有重新打开 ResearchScreenV2 才能看到更新后的焦点状态。

## 根本原因

这个问题由 **三个独立的 bug** 共同导致：

### Bug 1: 服务端修改数据后没有立即同步到客户端

**位置**: `PlayerTechTreeData.java`

**问题代码**:
```java
@Override
public void focus(ResourceLocation techId) {
    // ... 检查逻辑 ...
    
    clearFocus();
    if (!instance.getState().equals(TechState.WAITING)) {
        clearWaiting();
    }
    
    instance.setFocused(true);
    player.sendSystemMessage(Component.literal("Focused on tech: " + techId.toString()));
    
    // ❌ 缺少 syncToClient() 调用！
}
```

**问题分析**:
- `focus()` 方法修改了 `techMap` 中的数据（设置 `isFocused = true`）
- 但没有调用 `syncToClient()` 立即同步数据
- 现有的同步机制依赖于 `tick()` 中的 `autoSync()`
- `autoSync()` 通过哈希值检测数据变化，但只在每个 tick 执行一次
- 导致数据同步有延迟（最多 1 tick = 50ms）

### Bug 2: 客户端使用过时的 SyncData 缓存

**位置**: `ClientScreenManager.java`

**问题代码**:
```java
public TechSlotData getTechSlotData() {
    if (!techSlotData.isEmpty()) {
        // ❌ 延迟获取 syncData，导致使用过时的缓存对象
        if (syncData == null) {
            syncData = ClientResearchData.getSyncData();
        }
        
        // 使用旧的 syncData 对象计算哈希值
        int currentHash = syncData.getDataHash();
        if (techSlotData.isHashMatched(currentHash)) {
            return techSlotData; // ❌ 返回过时的数据！
        }
        // ...
    }
    // ...
}
```

**问题分析**:
- `syncData` 是一个成员变量，在第一次调用时被缓存
- 当服务端发送 `ClientboundSyncPlayerData` 更新 `ClientResearchData.playerSyncedDataLookup` 后
- `ClientScreenManager.syncData` **仍然引用旧的对象**
- 即使 `ClientResearchData.getSyncData()` 返回了新数据，但没有被使用
- 导致哈希值比对时使用旧数据的哈希值，检测不到变化
- 只有重新打开界面（创建新的 ClientScreenManager 实例）才会获取新数据

### Bug 3: 重建 TechSlot 对象导致丢失转换后的屏幕坐标

**位置**: `ClientScreenManager.java`

**问题代码**:
```java
private void rebuildTechSlots(SyncData data) {
    var techs = data.getCacheds();
    var vecs = data.getVecMap();
    
    var newSlots = new java.util.ArrayList<TechSlot>(techs.size());
    
    for (var entry : techs.entrySet()) {
        var identifier = entry.getKey();
        var tech = entry.getValue();
        var pos = vecs.get(identifier);
        
        if (pos != null) {
            // ❌ 创建新的 TechSlot 对象，丢失了已转换的屏幕坐标
            newSlots.add(new TechSlot(pos.x(), pos.y(), tech));
        }
    }
    
    // ❌ 完全替换槽位列表
    techSlotData.setCachedTechSlots(newSlots);
}
```

**问题分析**:
- `rebuildTechSlots()` 完全重建了所有 TechSlot 对象
- TechSlot 对象包含：
  - 世界坐标（Vec2i：x, y）
  - **屏幕坐标（通过 `setPosition()` 转换后的实际渲染位置）**
  - TechInstance 数据（包含 focused 状态）
- 当数据更新时，`rebuildTechSlots()` 被调用：
  - 创建全新的 TechSlot 对象
  - **丢失了已转换的屏幕坐标**（需要重新调用 `initializePositionsWithVecMap()`）
  - 但 `render()` 方法不会重新初始化坐标
- 导致 TechSlot 的位置信息不正确，或者需要重新打开界面才能正确显示

## 修复方案

### 修复 1: 服务端立即同步数据

**文件**: `PlayerTechTreeData.java`

```java
@Override
public void focus(ResourceLocation techId) {
    // ... 检查逻辑 ...
    
    clearFocus();
    if (!instance.getState().equals(TechState.WAITING)) {
        clearWaiting();
    }
    
    instance.setFocused(true);
    player.sendSystemMessage(Component.literal("Focused on tech: " + techId.toString()));
    
    // ✅ 立即同步到客户端，确保焦点状态实时更新
    syncToClient();
}

@Override
public void clearFocus() {
    for (var techInstance : techMap.values()) {
        if (techInstance.isFocused()) {
            techInstance.setFocused(false);
        }
    }
    
    // ✅ 立即同步到客户端，确保焦点清除状态实时更新
    syncToClient();
}
```

### 修复 2: 客户端每次都获取最新数据

**文件**: `ClientScreenManager.java`

```java
public TechSlotData getTechSlotData() {
    // ✅ 每次都重新获取 syncData，确保使用最新的数据
    syncData = ClientResearchData.getSyncData();
    
    // 验证 syncData 是否有效
    if (syncData.getPlayerId() == -999) {
        return techSlotData;
    }

    // 快速检查：已有数据且哈希未变 -> 直接返回
    if (!techSlotData.isEmpty()) {
        int currentHash = syncData.getDataHash();
        if (techSlotData.isHashMatched(currentHash)) {
            return techSlotData; // ✅ 现在使用的是最新数据的哈希值
        }

        // 哈希值不匹配，需要更新数据
        techSlotData.updateHash(currentHash);
        rebuildTechSlots(syncData);
        return techSlotData;
    }

    // 首次初始化，构建槽位数据
    int currentHash = syncData.getDataHash();
    techSlotData.updateHash(currentHash);
    rebuildTechSlots(syncData);

    return techSlotData;
}
```

### 修复 3: 增量更新 TechSlot 数据，避免重建对象

**文件**: `ClientScreenManager.java`

```java
public TechSlotData getTechSlotData() {
    // 每次都重新获取 syncData，确保使用最新的数据
    syncData = ClientResearchData.getSyncData();
    
    if (syncData.getPlayerId() == -999) {
        return techSlotData;
    }

    if (!techSlotData.isEmpty()) {
        int currentHash = syncData.getDataHash();
        if (techSlotData.isHashMatched(currentHash)) {
            return techSlotData;
        }

        // ✅ 数据变化时，只更新 TechInstance，不重建对象
        techSlotData.updateHash(currentHash);
        updateTechSlots(syncData);  // 增量更新
        return techSlotData;
    }

    // 首次初始化，需要完整构建
    int currentHash = syncData.getDataHash();
    techSlotData.updateHash(currentHash);
    rebuildTechSlots(syncData);  // 完整重建

    return techSlotData;
}

/**
 * 增量更新：只更新 TechInstance 数据，保留坐标
 */
private void updateTechSlots(SyncData data) {
    var techs = data.getCacheds();
    var cachedSlots = techSlotData.getCachedTechSlots();

    // ✅ 遍历现有的 TechSlot，只更新数据
    for (var slot : cachedSlots) {
        var identifier = slot.getTechInstance().getIdentifier();
        var newTechInstance = techs.get(identifier);
        
        if (newTechInstance != null) {
            // ✅ 只更新 TechInstance，保留坐标信息
            slot.updateInstance(newTechInstance);
        }
    }
}

/**
 * 完整重建：用于首次初始化
 */
private void rebuildTechSlots(SyncData data) {
    var techs = data.getCacheds();
    var vecs = data.getVecMap();
    var newSlots = new java.util.ArrayList<TechSlot>(techs.size());

    for (var entry : techs.entrySet()) {
        var identifier = entry.getKey();
        var tech = entry.getValue();
        var pos = vecs.get(identifier);

        if (pos != null) {
            newSlots.add(new TechSlot(pos.x(), pos.y(), tech));
        }
    }

    techSlotData.setCachedTechSlots(newSlots);
}
```

**TechSlot 类已有的支持**:
```java
// TechSlot.java 中已经实现的方法
public void updateInstance(TechInstance newInstance) {
    this.tech = newInstance;
}
```

## 修复后的数据流

```
用户点击 TechSlot
    ↓
ResearchScreenV2.mouseReleased()
    ↓
ClientScreenManager.handleMouseReleased()
    ↓
发送 ClientSetFocusPacket(techId)
    ↓
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
服务器处理 ClientSetFocusPacket
    ↓
PlayerTechTreeData.focus(techId)
    ├─ 修改 techMap 数据
    └─ ✅ 立即调用 syncToClient()
        ↓
        发送 ClientboundSyncPlayerData
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    ↓
客户端收到 ClientboundSyncPlayerData
    ↓
更新 ClientResearchData.playerSyncedDataLookup
    ↓
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
下一帧 ResearchScreenV2.render()
    ↓
ClientScreenManager.getTechSlotData()
    ├─ ✅ 每次都重新获取 syncData（最新数据）
    ├─ 计算新的哈希值
    ├─ 检测到哈希值变化
    └─ ✅ updateTechSlots() 增量更新槽位数据
        ├─ 遍历现有的 TechSlot 对象
        └─ 调用 slot.updateInstance(newTechInstance)
            ├─ ✅ 更新 TechInstance 数据（包含 focused 状态）
            └─ ✅ 保留已转换的屏幕坐标
        ↓
TechSlot.render()
    ├─ tech.isFocused() → true ✅
    └─ 显示焦点样式 🎯
```

## 性能影响

### 修复 1: syncToClient() 立即同步
- **网络开销**: 每次 focus 操作发送 1 个数据包（~100-500 bytes）
- **影响**: 极小，focus 操作频率低（用户手动点击）

### 修复 2: 每帧获取 syncData
- **CPU 开销**: `ClientResearchData.getSyncData()` 是一次 HashMap 查找，O(1)
- **影响**: 可忽略不计，HashMap 查找性能极高（~10ns）
- **优化**: 哈希值验证机制避免了不必要的重建操作

## 验证方法

1. 启动游戏并打开 ResearchScreenV2
2. 点击任意可用的 TechSlot
3. **预期结果**: 
   - 立即看到焦点样式（金色边框、发光效果等）
   - 无需重新打开界面
4. 点击另一个 TechSlot
5. **预期结果**: 
   - 前一个 TechSlot 的焦点样式消失
   - 新 TechSlot 立即显示焦点样式

## 相关文件

- `PlayerTechTreeData.java` - 服务端科技树数据管理
- `ClientScreenManager.java` - 客户端界面数据管理
- `ClientboundSyncPlayerData.java` - 服务端→客户端数据同步包
- `ClientSetFocusPacket.java` - 客户端→服务端焦点设置包
- `SyncData.java` - 同步数据对象（包含哈希计算）
- `ClientResearchData.java` - 客户端数据缓存

## 总结

这个问题是经典的"缓存过期 + 数据丢失"问题：
1. **服务端**: 修改数据后没有立即通知客户端
2. **客户端缓存**: 使用过时的缓存对象，无法检测到数据更新
3. **客户端重建**: 完全重建对象导致丢失转换后的坐标信息

修复方案：
1. **服务端**: 数据修改后立即同步（push 模式）
2. **客户端缓存**: 每次都获取最新数据，避免使用过时缓存（pull 模式）
3. **客户端更新**: 增量更新数据，保留对象状态（避免重建）

三个修复缺一不可，共同确保数据实时同步且不丢失状态。

---

**修复日期**: 2026-02-03  
**修复作者**: GitHub Copilot  
**影响范围**: Focus 焦点状态的实时更新
