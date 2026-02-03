# Focus 数据实时更新修复

## 问题分析

### 原始问题
- Focus 数据不会实时更新
- 只有重新打开 ResearchScreenV2 时才会刷新
- 点击 TechSlot 后，焦点状态无法立即显示

### 根本原因

在 ResearchScreenV2 中，`tick()` 方法是空的：
```java
@Override
public void tick() {
    super.tick();
    // 空白！没有实时更新逻辑
}
```

**数据流问题：**
```
1. 客户端点击 TechSlot
   ↓
2. 发送 ClientSetFocusPacket 到服务器
   ↓
3. 服务器更新 PlayerTechTreeData 中的 TechInstance（focus = true）
   ↓
4. 服务器同步给客户端（通过 ClientboundSyncPlayerData）
   ↓
5. 客户端更新 ClientResearchData.getSyncData() ✅
   ↓
6. ❌ 但缓存的 TechSlot 中的 TechInstance 没有更新！
   ↓
7. 渲染时使用的仍然是旧的 TechInstance（focus = false）
   ↓
8. 需要重新打开屏幕才能重新初始化 TechSlot
```

### 数据结构关系

```
ClientResearchData.getSyncData()  ← 服务器同步过来，包含最新数据
         ↓
         ├─ SyncData.getCacheds()  → Map<ResourceLocation, TechInstance>
         
ClientScreenManager.getTechSlotData()  ← 缓存的渲染数据
         ↓
         └─ TechSlotData.getCachedTechSlots()  → List<TechSlot>
            ↓
            └─ TechSlot.tech  ← 这里的 TechInstance 需要实时同步！
               ↓
               └─ TechInstance.focused  ← 用于决定是否显示焦点样式
```

TechSlot 中的 TechInstance 需要保持与 SyncData 中的数据一致！

---

## 解决方案

### 实现思路

在 `tick()` 方法中每帧检查是否有更新：

```java
@Override
public void tick() {
    super.tick();
    
    // 实时更新所有 TechSlot 的 TechInstance 数据
    ClientResearchData.getManager().ifPresent(manager -> {
        var syncData = ClientResearchData.getSyncData();
        var techSlotData = manager.getTechSlotData();
        
        if (techSlotData != null && !techSlotData.isEmpty() && syncData != null) {
            var cachedTechSlots = techSlotData.getCachedTechSlots();
            var cacheds = syncData.getCacheds();
            
            // 更新每个 TechSlot 中的 TechInstance
            for (var techSlot : cachedTechSlots) {
                var currentTechId = techSlot.getTechInstance().getIdentifier();
                var updatedTechInstance = cacheds.get(currentTechId);
                
                // 如果服务器有更新的数据，则更新该槽位
                if (updatedTechInstance != null && !updatedTechInstance.equals(techSlot.getTechInstance())) {
                    techSlot.updateInstance(updatedTechInstance);
                }
            }
        }
    });
}
```

### 工作原理

1. **每帧检查**（tick 每帧调用一次）
2. **获取最新的 SyncData**（从服务器同步的数据）
3. **遍历所有缓存的 TechSlot**
4. **比较并更新**
   - 如果 SyncData 中的 TechInstance 与缓存的不同
   - 调用 `TechSlot.updateInstance()` 更新引用
5. **自动触发重新渲染**
   - 下一个 render() 调用时，TechSlot 会使用新的 TechInstance
   - `renderWidget()` 中的 `tech.isFocused()` 返回最新值

### 性能考量

```java
// 比较逻辑使用了 equals() 方法
if (updatedTechInstance != null && !updatedTechInstance.equals(techSlot.getTechInstance()))
```

**性能优化：**
- 只有数据真的改变时才调用 `updateInstance()`
- 避免每帧都无谓地重新赋值
- TechInstance 的 `equals()` 方法已考虑了 focused 字段

---

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
服务器处理，更新 PlayerTechTreeData
    ↓
服务器同步 ClientboundSyncPlayerData
    ↓
客户端收到，更新 ClientResearchData.getSyncData()
    ↓
ResearchScreenV2.tick() ← 现在有实现了！
    ↓
检查 SyncData 是否有更新
    ↓
TechSlot.updateInstance(newTechInstance)
    ↓
ResearchScreenV2.render() 下一帧
    ↓
TechSlot.renderWidget()
    ↓
tech.isFocused() → true ✅
    ↓
显示焦点样式 🎯
```

---

## 关键点总结

| 组件 | 职责 | 更新时机 |
|------|------|---------|
| `ClientResearchData.getSyncData()` | 存储服务器同步的最新数据 | 服务器主动同步时 |
| `TechSlotData` | 缓存用于渲染的 TechSlot 列表 | resize() 时初始化，tick() 时更新 |
| `TechSlot` | GUI 组件，负责渲染 | 每帧都调用 render() |
| `ResearchScreenV2.tick()` | 同步逻辑 | **每帧调用**（现已实现） |

---

## 测试验证

### 测试步骤
1. 打开 ResearchScreenV2
2. 点击一个 TechSlot
3. 观察该槽位是否立即显示焦点样式（不需要关闭和重新打开）

### 预期行为
- ✅ 点击后立即显示焦点样式
- ✅ 焦点样式实时更新（不需要重新打开屏幕）
- ✅ 其他玩家的焦点变化也会实时显示

### 性能影响
- **极小**：每帧只是简单的循环和比较操作
- 只有数据真的改变时才会调用 `updateInstance()`
- 对渲染性能无显著影响

---

## 扩展思考

### 其他实时更新需求

同样的方式可以用于其他需要实时同步的数据：
- 科技状态改变（LOCKED → AVAILABLE → COMPLETED）
- 科技解锁变化
- 其他与 TechInstance 相关的属性

只需在 `tick()` 中添加相应的更新逻辑即可。

