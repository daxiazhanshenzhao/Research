# SearchTechSlot 锁渲染实现

## 概述
为 `SearchTechSlot` 组件添加了锁的渲染功能，与 `TechSlot` 保持一致的视觉效果，当科技处于锁定状态时显示锁图标覆盖层。

## 实现细节

### 1. 添加锁纹理定义
```java
// 锁的纹理定义（与 TechSlot 保持一致）
private static final BlitContext LOCK = BlitContext.of(Texture.TEXTURE, 0, 24, 10, 15);
```

**纹理坐标：**
- UV 坐标：(0, 24)
- 尺寸：10x15 像素
- 纹理图集：512x512

### 2. 渲染流程

#### renderWidget() 方法
```java
@Override
protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    // 1. 渲染背景
    // 2. 渲染物品图标
    // 3. 渲染锁（如果科技被锁定）
    renderLock(guiGraphics, techSlot);
}
```

#### renderLock() 方法
```java
private void renderLock(GuiGraphics guiGraphics, TechSlot techSlot) {
    // 检查科技是否为空
    if (techSlot == null || techSlot.getTechInstance().isEmpty()) {
        return;
    }
    
    // 检查科技状态是否为锁定
    if (!techSlot.getTechInstance().getState().isLocked()) {
        return;
    }

    // 渲染锁图标（z轴提升以显示在物品上方）
    guiGraphics.pose().pushPose();
    guiGraphics.pose().translate(0, 0, +100);  // z轴向前移动，显示在物品上方
    guiGraphics.blit(LOCK.texture(), getX() + 5, getY() + 3,
            LOCK.u(), LOCK.v(), LOCK.width(), LOCK.height(), 512, 512);
    guiGraphics.pose().popPose();
}
```

## 渲染层级

```
Z轴深度（从后到前）：
┌─────────────────────────────────┐
│ 1. 背景纹理 (z = 0)              │
│    - 普通：RECIPE_SEARCH_TECH_BUTTON
│    - 悬停：RECIPE_SEARCH_TECH_BUTTON_ACTIVE
├─────────────────────────────────┤
│ 2. 物品图标 (z = 0)              │
│    - renderItem()               │
├─────────────────────────────────┤
│ 3. 锁覆盖层 (z = +100)           │
│    - 仅在 isLocked() 时显示      │
└─────────────────────────────────┘
```

## 锁定状态判断

锁的显示依赖于 `TechInstance` 的状态：

```java
techSlot.getTechInstance().getState().isLocked()
```

### TechState 枚举值
- `LOCKED` - 锁定状态（显示锁）
- `AVAILABLE` - 可用状态（不显示锁）
- `COMPLETED` - 完成状态（不显示锁）

## 视觉效果

### 锁定的科技
```
┌──────────────┐
│  [物品图标]   │
│     🔒       │  ← 锁图标覆盖在物品右下角
└──────────────┘
```

### 未锁定的科技
```
┌──────────────┐
│  [物品图标]   │
│              │
└──────────────┘
```

## 锁图标位置

- **X 偏移**：`getX() + 5` （相对于槽位左上角向右偏移 5 像素）
- **Y 偏移**：`getY() + 3` （相对于槽位左上角向下偏移 3 像素）

这样可以让锁图标显示在物品图标的右下区域，不会完全遮挡物品。

## 与 TechSlot 的一致性

| 特性 | TechSlot | SearchTechSlot |
|------|----------|----------------|
| 锁纹理坐标 | (0, 24) | (0, 24) ✓ |
| 锁尺寸 | 10x15 | 10x15 ✓ |
| Z轴偏移 | +100 | +100 ✓ |
| X偏移 | +5 | +5 ✓ |
| Y偏移 | +3 | +3 ✓ |
| 判断条件 | isLocked() | isLocked() ✓ |

## 用户体验

1. **视觉反馈**：玩家可以直观地看到哪些科技被锁定
2. **搜索结果**：即使在搜索结果中，锁定状态也能清晰显示
3. **状态同步**：锁定状态实时反映科技的实际状态

## 测试要点

- [ ] 锁定的科技显示锁图标
- [ ] 未锁定的科技不显示锁图标
- [ ] 锁图标显示在物品上方（z轴正确）
- [ ] 锁图标位置正确（右下角）
- [ ] 搜索不同科技时锁状态正确更新
- [ ] 锁图标不会遮挡 tooltip

## 相关文件

- `SearchTechSlot.java` - 搜索槽位组件
- `TechSlot.java` - 科技槽位组件（参考实现）
- `Texture.java` - 纹理资源定义
- `TechInstance.java` - 科技实例（状态管理）

## 扩展性

如果需要添加其他状态覆盖层（如"进行中"、"新解锁"等），可以按照相同的模式：

```java
private void renderStatusOverlay(GuiGraphics guiGraphics, TechSlot techSlot) {
    if (shouldShowStatus(techSlot)) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, +100);
        // 渲染状态图标
        guiGraphics.pose().popPose();
    }
}
```
