# Focus 实时更新 - 快速参考

## 问题和解决方案

### 问题
```
❌ 点击 TechSlot 后，focus 状态不会立即显示
❌ 必须关闭和重新打开 ResearchScreenV2 才能看到更新
```

### 原因
- `ResearchScreenV2.tick()` 方法是空的
- 缓存的 TechSlot 中的 TechInstance 没有实时更新
- 虽然服务器数据已同步到 `ClientResearchData.getSyncData()`，但缓存的 UI 组件没有使用新数据

### 解决方案
在 `ResearchScreenV2.tick()` 中实时同步 TechSlot 的数据：

```java
@Override
public void tick() {
    super.tick();
    
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
                
                if (updatedTechInstance != null && !updatedTechInstance.equals(techSlot.getTechInstance())) {
                    techSlot.updateInstance(updatedTechInstance);
                }
            }
        }
    });
}
```

## 修复效果

| 行为 | 修复前 | 修复后 |
|------|--------|--------|
| 点击后立即显示 focus 样式 | ❌ | ✅ |
| 需要重新打开屏幕 | ✅ | ❌ |
| 实时同步其他玩家的数据 | ❌ | ✅ |
| 性能影响 | N/A | 极小（每帧简单比较） |

## 实现细节

### 更新流程
```
tick() 每帧调用
    ↓
获取最新的 SyncData
    ↓
遍历缓存的 TechSlot
    ↓
比较 SyncData 中的 TechInstance 与缓存的是否不同
    ↓
调用 TechSlot.updateInstance() 更新
    ↓
下一个 render() 使用新数据
    ↓
显示新的 focus 状态
```

### 性能优化
- 使用 `equals()` 进行比较，只更新改变的数据
- 避免每帧都无谓地重新赋值
- 循环遍历复杂度：O(n)，n 为 TechSlot 数量（通常 < 100）

## 相关文件修改

- `ResearchScreenV2.java` - 实现 `tick()` 方法
- 无需修改其他文件

## 测试

```java
// 1. 打开屏幕
2. 点击任意 TechSlot
3. 观察：应该立即显示焦点样式（边框变亮，背景变黑）
4. 无需重新打开屏幕
```

