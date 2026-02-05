# ClientOverlayManager 实时数据获取优化

## 问题描述
之前 `ClientOverlayManager` 的 `getRecipeDisplayData()` 方法依赖于 `ClientScreenManager` 的 `techSlotData`，这个数据只在打开工作台时更新，导致覆盖层无法实时获取焦点科技的配方信息。

## 解决方案
修改 `ClientOverlayManager` 的 `getRecipeDisplayData()` 方法，**直接从 `SyncData` 中获取焦点科技的 `TechInstance`**，而不依赖 `TechSlotData`。

## 实现的修改

### 1. 更新导入
**位置**: `ClientOverlayManager.java` 第 1-18 行

**添加**:
```java
import org.research.api.tech.TechInstance;
```

**移除**:
```java
import org.research.api.gui.ClientScreenManager;
import org.research.gui.minecraft.component.TechSlot;
```

**原因**: 不再需要通过 `ClientScreenManager` 和 `TechSlot` 来查找焦点科技。

---

### 2. 重写 getRecipeDisplayData() 方法
**位置**: `ClientOverlayManager.java` 第 264-322 行

**修改前的流程**:
```
获取 SyncData
  ↓
获取焦点科技 ID
  ↓
从 ClientScreenManager 获取 TechSlotData ❌ (只在打开工作台时更新)
  ↓
遍历所有 TechSlot 查找匹配的槽位 ❌ (低效)
  ↓
从 TechSlot 获取 TechInstance
  ↓
获取配方信息
```

**修改后的流程**:
```
获取 SyncData
  ↓
获取焦点科技 ID
  ↓
直接从 SyncData.cacheds 获取 TechInstance ✅ (实时同步)
  ↓
获取配方信息
```

**新实现**:
```java
public RecipeDisplayData getRecipeDisplayData() {
    // 如果已缓存，直接返回
    if (cachedRecipeData != null) {
        return cachedRecipeData;
    }

    // 获取 SyncData（服务端同步的数据）
    var syncData = ClientResearchData.getSyncData();
    if (syncData == null || syncData.getPlayerId() == -999) {
        return null;
    }

    // 获取服务端焦点科技的 ID
    var focusTechId = syncData.getFocusTech();
    if (focusTechId == null || focusTechId.equals(TechInstance.EMPTY.getIdentifier())) {
        return null;
    }

    // ✅ 直接从 syncData 的 cacheds 中获取焦点科技的 TechInstance
    TechInstance focusTechInstance = syncData.getCacheds().get(focusTechId);
    if (focusTechInstance == null || focusTechInstance.isEmpty()) {
        return null;
    }

    // 获取配方信息
    var recipeWrapper = focusTechInstance.getRecipe();
    if (recipeWrapper == null) {
        return null;
    }

    // 获取配方实例
    Recipe<?> recipe = IRecipe.getClientRecipe(recipeWrapper, Minecraft.getInstance());
    if (recipe == null) {
        return null;
    }

    // 获取对应的 RecipeCategory
    var categoryMap = ClientResearchData.recipeCategories.getRecipeCategories();
    RecipeCategory<?> category = categoryMap.get(recipe.getType());
    if (category == null) {
        return null;
    }

    // 初始化配方并获取 builderSlots
    @SuppressWarnings("unchecked")
    RecipeCategory<Recipe> genericCategory = (RecipeCategory<Recipe>) category;
    genericCategory.setRecipe(recipe);
    Map<Integer, SlotBuilder> builderSlots = category.getBuilder().getBuilderSlots();

    // 处理配方数据并缓存
    cachedRecipeData = processRecipeData(builderSlots);
    return cachedRecipeData;
}
```

---

## 关键改进

### ✅ 实时性
- **之前**: 依赖 `TechSlotData`，只在打开工作台时更新
- **现在**: 直接从 `SyncData` 获取，服务端每次状态变化都会自动同步

### ✅ 效率
- **之前**: 需要遍历所有 `TechSlot` 查找匹配的槽位 (O(n))
- **现在**: 直接通过 Map 键查找 (O(1))

### ✅ 简洁性
- **之前**: 依赖 `ClientScreenManager` → `TechSlotData` → `TechSlot` → `TechInstance`
- **现在**: 直接 `SyncData` → `TechInstance`

### ✅ 独立性
- **之前**: 依赖 GUI 系统（`ClientScreenManager`）
- **现在**: 独立于 GUI，只依赖数据同步系统

---

## SyncData 的数据结构

`SyncData` 包含以下关键信息：

```java
public class SyncData {
    // 玩家 ID
    private int playerId;
    
    // 所有科技实例的缓存 (包含状态和焦点信息)
    private Map<ResourceLocation, TechInstance> cacheds;
    
    // 科技位置映射
    private Map<ResourceLocation, Vec2i> vecMap;
    
    // 当前阶段
    private int stage;
    
    // 获取焦点科技 ID
    public ResourceLocation getFocusTech() {
        for (TechInstance tech : cacheds.values()) {
            if (tech.isFocused()) {
                return tech.getIdentifier();
            }
        }
        return TechInstance.EMPTY.getIdentifier();
    }
    
    // 获取所有科技实例
    public Map<ResourceLocation, TechInstance> getCacheds() {
        return cacheds;
    }
}
```

---

## TechInstance 的关键方法

```java
public class TechInstance {
    // 检查是否为空科技
    public boolean isEmpty() {
        return this.tech.equals(TechInit.EMPTY_TECH.get());
    }
    
    // 获取配方包装器
    public RecipeWrapper getRecipe() {
        return getTech().getTechBuilder().recipe;
    }
    
    // 获取科技 ID
    public ResourceLocation getIdentifier() {
        return tech.getIdentifier();
    }
    
    // 检查是否被聚焦
    public boolean isFocused() {
        return focused;
    }
}
```

---

## 数据同步流程

```
服务端 (PlayerTechTreeData)
  ↓
状态变化 (focus/unlock/complete)
  ↓
autoSync() 检测到数据变化
  ↓
创建 SyncData 并发送到客户端
  ↓
客户端接收并存储到 ClientResearchData.playerSyncedDataLookup
  ↓
ClientOverlayManager 从 SyncData 获取数据 ✅ 实时更新
```

---

## 测试建议

1. **实时性测试**:
   - 不打开工作台，完成一个科技
   - 验证覆盖层立即显示新焦点科技的配方

2. **多路径测试**:
   - 完成有多个子节点的科技
   - 手动选择不同的子节点
   - 验证覆盖层正确显示对应配方

3. **性能测试**:
   - 快速切换焦点科技
   - 验证没有延迟或卡顿

4. **边界测试**:
   - 焦点科技没有配方
   - 焦点科技为 EMPTY
   - SyncData 为空或无效

---

## 注意事项

### 缓存管理 ✅ 已实现
**问题**: 之前 `cachedRecipeData` 缓存永远不会清除，导致即使服务端数据更新，Overlay 仍显示旧数据

**解决**: 
1. 在 `ClientOverlayManager` 中添加 `clearCache()` 方法
2. 在 `ClientboundSyncPlayerData` 处理数据更新时自动调用 `clearCache()`

**实现代码**:

`ClientOverlayManager.java`:
```java
/**
 * 清除所有缓存数据
 * 当服务端数据更新时调用，确保 Overlay 显示最新数据
 */
public void clearCache() {
    cachedRecipeData = null;
    cachedCenterHeight = -1;
}
```

`ClientboundSyncPlayerData.java`:
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

**效果**: 
- ✅ 每次服务端同步数据时，自动清除客户端缓存
- ✅ Overlay 在下一帧渲染时会重新从 SyncData 获取最新数据
- ✅ 无需打开工作台，Overlay 即可实时显示数据变化

---

## 日期
2026-02-05

## 相关文档
- [自动聚焦解锁子节点功能实现](AUTO_FOCUS_ON_UNLOCK.md)
