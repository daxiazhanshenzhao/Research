# SearchTechSlot 视图定位功能

## 概述
当用户点击 `SearchTechSlot` 按钮后，视图会自动平移，将目标科技移动到 **GUI 右边 3/4 处（水平）和高度 1/2 处（垂直）**，因为 GUI 左半部分被遮挡，这样可以确保科技完全可见。

## 功能特性

### 1. 智能定位
- 点击搜索结果中的任何科技
- 视图自动平移，将该科技移动到最佳可视位置
- **水平位置**：GUI 右边 3/4 处（避开左侧遮挡区域）
- **垂直位置**：GUI 高度 1/2 处（垂直居中）

### 2. 保持缩放级别
- 只改变平移偏移量（offsetX, offsetY）
- 不改变缩放级别（scale）
- 确保用户的缩放设置不受影响

### 3. 边界限制
- 自动应用边界限制（clampOffset）
- 防止视图移出背景范围

## 实现细节

### 1. ClientScreenManager.centerOnTechSlot()

```java
/**
 * 将视图移动到指定的科技槽位
 * 目标位置：GUI 右边 3/4（水平）+ GUI 高度 1/2（垂直）
 * @param techSlot 目标科技槽位
 */
public void centerOnTechSlot(TechSlot techSlot) {
    if (techSlot == null || techSlot.getTechInstance().isEmpty()) {
        return;
    }

    // 直接获取科技槽位的世界坐标（左上角）
    int techWorldX = techSlot.getX();
    int techWorldY = techSlot.getY();

    // 获取 inside 区域的信息
    int insideX = screenData.getInsideX();
    int insideY = screenData.getInsideY();
    int insideWidth = screenConfigData.insideUV().width();
    int insideHeight = screenConfigData.insideUV().height();

    // 计算目标屏幕位置
    // 水平：GUI 右边 3/4 处（因为左半部分被遮挡）
    // 垂直：GUI 高度 1/2 处（垂直居中）
    int targetScreenX = insideX + (int)(insideWidth * 0.75);
    int targetScreenY = insideY + insideHeight / 2;

    // 获取视口中心（用于坐标转换）
    int centerX = getCenterX();
    int centerY = getCenterY();

    // 计算偏移量，使科技的世界坐标在屏幕上显示在目标位置
    // 正向变换公式：screenX = (worldX - centerX) * scale + centerX + offsetX
    // 求解 offsetX：offsetX = screenX - (worldX - centerX) * scale - centerX
    // 简化：offsetX = targetScreenX - worldX * scale + centerX * (scale - 1)
    float scale = mouseData.getScale();
    double newOffsetX = targetScreenX - techWorldX * scale + centerX * (scale - 1);
    double newOffsetY = targetScreenY - techWorldY * scale + centerY * (scale - 1);

    // 设置新的偏移量
    mouseData.setOffsetX(newOffsetX);
    mouseData.setOffsetY(newOffsetY);

    // 限制偏移范围
    clampOffset();
}
```

### 2. SearchTechSlot.onPress()

```java
@Override
public void onPress() {
    TechSlot techSlot = manager.getTechIconById(id);
    if (techSlot != null && techSlot != TechSlot.EMPTY && !techSlot.getTechInstance().isEmpty()) {
        // 1. 发送焦点数据包到服务器
        PacketInit.sendToServer(
            new ClientSetFocusPacket(techSlot.getTechInstance().getIdentifier())
        );
        
        // 2. 设置客户端焦点
        manager.getTechSlotData().setFocusTechSlot(techSlot);
        
        // 3. 将视图中心移动到目标科技位置
        manager.centerOnTechSlot(techSlot);
    }
}
```

## 坐标变换原理

### 坐标系统

```
世界坐标系（背景纹理坐标）
    ↓ 应用变换（缩放 + 平移）
屏幕坐标系（视口坐标）
```

### 变换公式

**正向变换**（世界坐标 → 屏幕坐标）：
```
screenX = (worldX - centerX) * scale + centerX + offsetX
screenY = (worldY - centerY) * scale + centerY + offsetY
```

**逆向变换**（屏幕坐标 → 世界坐标）：
```
worldX = (screenX - offsetX - centerX) / scale + centerX
worldY = (screenY - offsetY - centerY) / scale + centerY
```

### 定位计算

要将世界坐标 `(techWorldX, techWorldY)` 移动到目标屏幕位置 `(targetScreenX, targetScreenY)`：

1. **目标屏幕位置**：
   ```
   targetScreenX = insideX + insideWidth * 0.75  // GUI 右边 3/4 处
   targetScreenY = insideY + insideHeight * 0.5  // GUI 高度 1/2 处
   ```

2. **代入正向变换公式**：
   ```
   targetScreenX = (techWorldX - centerX) * scale + centerX + offsetX
   targetScreenY = (techWorldY - centerY) * scale + centerY + offsetY
   ```

3. **求解 offset**：
   ```
   offsetX = targetScreenX - (techWorldX - centerX) * scale - centerX
   offsetX = targetScreenX - techWorldX * scale + centerX * scale - centerX
   offsetX = targetScreenX - techWorldX * scale + centerX * (scale - 1)
   
   offsetY = targetScreenY - techWorldY * scale + centerY * (scale - 1)
   ```

### 为什么使用 TechSlot 的原始坐标？

- `TechSlot.getX()` 和 `TechSlot.getY()` 已经是正确的世界坐标
- 这些坐标在初始化时通过 `TechSlotData.initializePositionsWithVecMap()` 设置
- 直接使用避免重复计算，提高性能和准确性

## 执行流程

```
用户点击 SearchTechSlot
        ↓
  onPress() 触发
        ↓
  获取目标 TechSlot
        ↓
  发送焦点数据包（服务器同步）
        ↓
  设置客户端焦点（高亮显示）
        ↓
  调用 centerOnTechSlot()
        ↓
  计算目标科技的世界坐标中心
        ↓
  计算新的偏移量（使科技居中）
        ↓
  应用偏移量到 MouseData
        ↓
  调用 clampOffset()（边界限制）
        ↓
  下一帧渲染时，视图自动居中
```

## 视觉效果

### GUI 布局说明
```
┌───────────────────────────────────────────────┐
│              视口区域（Inside）                │
│                                              │
│  ░░░░░░░░                                    │
│  ░遮挡░░   可视区域                           │
│  ░区域░░                                     │
│  ░░░░░░░░                                    │
│         ↑ 1/4      ↑ 3/4                     │
│                    🎯 目标位置                │
│                    （右边 3/4，高度 1/2）     │
│                                              │
└───────────────────────────────────────────────┘
```

### 点击前
```
┌───────────────────────────────────────────────┐
│              视口区域                         │
│                                              │
│  ░░░░░░░░       🎯 用户当前视图中心           │
│  ░遮挡░░                                     │
│  ░区域░░                                     │
│  ░░░░░░░░                                    │
│                                              │
│                                              │
└───────────────────────────────────────────────┘

     科技 A 在视口外 ⭐
```

### 点击后
```
┌───────────────────────────────────────────────┐
│              视口区域                         │
│                                              │
│  ░░░░░░░░                                    │
│  ░遮挡░░              ⭐ 科技 A               │
│  ░区域░░              （完全可见）            │
│  ░░░░░░░░                                    │
│         ↑ 1/4      ↑ 3/4                     │
│                    🎯 目标位置                │
└───────────────────────────────────────────────┘
```

### 为什么选择 3/4 位置？

1. **避开遮挡**：GUI 左半部分被其他 UI 元素遮挡
2. **最佳可视性**：右边 3/4 处确保科技完全可见
3. **视觉平衡**：不会太靠边缘，也不会被遮挡
4. **配方显示**：科技定位后，配方页面在右侧显示时不会重叠

## 边界处理

### clampOffset() 的作用
- 限制偏移量在合理范围内
- 防止视图超出背景纹理边界
- 如果目标科技靠近边缘，可能无法完全居中

### 边界情况
1. **科技在背景边缘**：视图会尽可能接近科技，但不会超出边界
2. **背景小于视口**：视图保持居中，不会移动
3. **缩放级别很大**：边界限制更严格

## 用户体验

### 优点
1. **快速定位**：无需手动拖拽，直接跳转到目标科技
2. **视觉连贯**：配合焦点高亮，清晰指示当前选中的科技
3. **操作便捷**：一键完成搜索、定位、聚焦

### 交互流程
```
搜索关键词 → 查看结果 → 点击物品 → 视图居中 → 查看科技详情
```

## 扩展性

### 添加动画效果
可以在 `centerOnTechSlot` 中添加平滑过渡动画：

```java
public void centerOnTechSlot(TechSlot techSlot, boolean animated) {
    if (animated) {
        // 使用插值实现平滑移动
        animateToPosition(targetOffsetX, targetOffsetY, duration);
    } else {
        // 直接跳转
        mouseData.setOffsetX(newOffsetX);
        mouseData.setOffsetY(newOffsetY);
    }
}
```

### 添加缩放级别调整
可以在居中时自动调整缩放级别：

```java
public void centerOnTechSlot(TechSlot techSlot, float targetScale) {
    // 先调整缩放级别
    mouseData.setScale(targetScale);
    
    // 再计算居中偏移
    // ...
}
```

## 测试要点

- [ ] 点击搜索结果后视图正确居中
- [ ] 缩放级别保持不变
- [ ] 边界限制正常工作
- [ ] 多次点击不同科技都能正确居中
- [ ] 科技在边缘时不会超出边界
- [ ] 焦点高亮与视图移动同步

## 相关文件

- `ClientScreenManager.java` - 视图管理和坐标变换
- `SearchTechSlot.java` - 搜索槽位组件
- `MouseData.java` - 鼠标和视图状态数据
- `TechSlot.java` - 科技槽位组件

## 性能考虑

- **计算成本**：O(1)，只涉及简单的数学运算
- **内存占用**：无额外内存分配
- **帧率影响**：瞬时完成，无卡顿

## 与其他功能的集成

1. **搜索功能**：点击搜索结果 → 自动居中
2. **焦点系统**：居中的同时设置焦点
3. **配方显示**：居中后可以查看配方详情
4. **拖拽系统**：不影响后续的手动拖拽操作

## 总结

通过添加 `centerOnTechSlot` 方法，用户可以在点击搜索结果后自动定位到目标科技，大大提升了搜索功能的可用性和用户体验。该功能与现有的焦点系统、配方显示系统无缝集成，形成了完整的科技浏览工作流。
