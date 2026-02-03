# TechSlot 点击、拖拽、声音、数据包完整实现

## 概述

在 `ResearchScreenV2` 中实现了完整的交互逻辑：
- **点击 TechSlot** → 播放声音 + 发送焦点数据包
- **拖拽防误触** → 使用 2px 阈值区分点击和拖拽
- **高度封装** → 所有逻辑封装在 `ClientScreenManager` 中，使用 `MouseData` 统一管理状态

---

## 核心实现

### 1. 鼠标点击 - `handleMouseClick`

**触发时机：** 用户按下左键

**功能：**
```java
public void handleMouseClick(double mouseX, double mouseY, int button) {
    // 检查鼠标是否在内部区域
    if (isMouseInSide(mouseX, mouseY)) {
        // 1. 记录拖拽起点（屏幕坐标）
        mouseData.setDragStartX(mouseX);
        mouseData.setDragStartY(mouseY);
        
        // 2. 重置累计拖拽距离
        mouseData.setDragTotal(0);
        
        // 3. 标记允许拖拽
        mouseData.setCanDrag(true);
        
        // 4. 检测点击的 TechSlot
        TechSlot clickedSlot = findClickedTechSlot(worldMouseX, worldMouseY);
        if (!clickedSlot.getTechInstance().isEmpty()) {
            // 5. 触发槽位的点击事件（自动播放声音）
            clickedSlot.mouseClicked(worldMouseX, worldMouseY, button);
        }
    } else {
        // 不在内容区域，禁止拖拽
        mouseData.setCanDrag(false);
    }
}
```

**关键点：**
- ✅ 使用 `MouseData` 中的字段管理拖拽状态
- ✅ `TechSlot.mouseClicked()` 已内置音效播放
- ✅ 记录拖拽起点用于防误触

---

### 2. 鼠标拖拽 - `handleMouseDrag`

**触发时机：** 按住左键并移动

**功能：**
```java
public void handleMouseDrag(double mouseX, double mouseY, int button, double dragX, double dragY) {
    // 1. 检查按下时是否允许拖拽
    if (!mouseData.isCanDrag()) {
        return;
    }

    // 2. 累计拖拽距离（使用绝对值）
    mouseData.setDragTotal(mouseData.getDragTotal() + Math.abs(dragX) + Math.abs(dragY));
    
    // 3. 只有超过阈值才真正执行拖拽操作
    if (mouseData.getDragTotal() > DRAG_THRESHOLD) {
        float scale = mouseData.getScale();
        // 应用拖拽偏移
        mouseData.setOffsetX(mouseData.getOffsetX() + dragX / scale);
        mouseData.setOffsetY(mouseData.getOffsetY() + dragY / scale);

        // 限制偏移范围，防止拖出边界
        clampOffset();
    }
    
    // 4. 始终更新转换后的鼠标坐标（用于 hover 检测等）
    updateTransformedMouseCoords(mouseX, mouseY);
}
```

**防误触原理：**
- `DRAG_THRESHOLD = 2.0`（单位：像素）
- 累计移动距离 ≤ 2px → 认为是手抖，不进行拖拽
- 累计移动距离 > 2px → 认为是有意拖拽，执行平移

**人体工学优势：**
- 给用户 2 像素的容差，防止误操作
- 提高了交互的容忍度和流畅度

---

### 3. 鼠标释放 - `handleMouseReleased`

**触发时机：** 释放左键

**功能：**
```java
public void handleMouseReleased(double mouseX, double mouseY, int button) {
    // 1. 检查是否在内容区域内
    if (!isMouseInSide(mouseX, mouseY)) {
        mouseData.setDragTotal(0);
        mouseData.setCanDrag(false);
        return;
    }

    // 2. 只有拖拽距离未超过阈值时，认为是点击操作
    if (mouseData.getDragTotal() <= DRAG_THRESHOLD) {
        double worldMouseX = mouseData.getTransformedMouseX();
        double worldMouseY = mouseData.getTransformedMouseY();
        
        // 3. 查找被点击的 TechSlot
        TechSlot clickedSlot = findClickedTechSlot(worldMouseX, worldMouseY);
        if (!clickedSlot.getTechInstance().isEmpty()) {
            // 4. 发送焦点数据包到服务器
            sendFocusPacket(clickedSlot);
        }
    }

    // 5. 重置拖拽状态
    mouseData.setDragTotal(0);
    mouseData.setCanDrag(false);
}
```

**发送数据包：**
```java
private void sendFocusPacket(TechSlot slot) {
    PacketInit.sendToServer(new ClientSetFocusPacket(slot.getTechInstance().getIdentifier()));
}
```

---

## 完整交互流程

```
用户点击 → ResearchScreenV2.mouseClicked()
    ↓
    manager.handleMouseClick(mouseX, mouseY, button)
    ├─ 初始化拖拽状态
    │  ├─ dragStartX/Y = 当前位置
    │  ├─ dragTotal = 0
    │  └─ canDrag = true
    │
    ├─ 查找被点击的 TechSlot
    └─ 触发 TechSlot.mouseClicked()
       └─ 播放点击音效 🔊

用户拖拽 → ResearchScreenV2.mouseDragged() （多次）
    ↓
    manager.handleMouseDrag(mouseX, mouseY, button, dragX, dragY)
    ├─ 累计移动距离: dragTotal += |dragX| + |dragY|
    │
    ├─ 若 dragTotal ≤ 2px
    │  └─ 不执行拖拽，保留状态等待释放
    │
    └─ 若 dragTotal > 2px
       └─ 更新 offset，平移背景和技能节点

用户释放 → ResearchScreenV2.mouseReleased()
    ↓
    manager.handleMouseReleased(mouseX, mouseY, button)
    ├─ 若 dragTotal ≤ 2px
    │  ├─ 认为是点击
    │  ├─ 查找被点击的 TechSlot
    │  └─ 发送焦点数据包到服务器 📦
    │     ClientSetFocusPacket(techId)
    │
    └─ 重置拖拽状态
       ├─ dragTotal = 0
       └─ canDrag = false
```

---

## 状态变量管理

所有状态通过 `MouseData` 统一管理：

| 变量 | 类型 | 用途 | 初始化 | 重置 |
|------|------|------|--------|------|
| `dragStartX` | `double` | 拖拽起点 X | `mouseClicked` | `mouseReleased` |
| `dragStartY` | `double` | 拖拽起点 Y | `mouseClicked` | `mouseReleased` |
| `dragTotal` | `double` | 累计拖拽距离 | 清零 | `mouseReleased` |
| `canDrag` | `boolean` | 是否允许拖拽 | 根据区域检测 | `mouseReleased` |
| `offsetX/Y` | `double` | 当前平移偏移 | 无初始化 | 在拖拽时更新 |
| `scale` | `float` | 当前缩放比例 | 1.0f | 在滚轮时更新 |

---

## 坐标转换

### 屏幕坐标 vs 世界坐标

**屏幕坐标（Screen Space）**
- 原点在屏幕左上角
- 直接来自鼠标事件参数
- 用于：UI 区域判断、tooltip 渲染

**世界坐标（World Space）**
- 原点在屏幕中心
- 已消除 offset 和 scale 的影响
- 用于：TechSlot 命中检测、内容区域判断

**转换方法：**
```java
double worldX = (screenX - offsetX - centerX) / scale
double worldY = (screenY - offsetY - centerY) / scale
```

由 `MouseData` 自动管理，通过 `getTransformedMouseX/Y()` 获取。

---

## 数据包流向

### 客户端 → 服务器

**数据包类型：** `ClientSetFocusPacket`

**内容：**
```java
public class ClientSetFocusPacket {
    private ResourceLocation focusTechId;  // 科技 ID
}
```

**发送时机：**
- 用户点击（拖拽距离 ≤ 2px）
- 鼠标在内容区域内
- 点击了有效的 TechSlot

**服务器处理（已实现）：**
```java
ResearchApi.getTechTreeData(sender).ifPresent(data -> {
    data.focus(focusTechId);
});
```

---

## 封装设计

### 高度封装的优势

1. **职责分离**
   - `MouseData`：数据管理
   - `ClientScreenManager`：业务逻辑
   - `ResearchScreenV2`：事件分发

2. **易于维护**
   - 修改防误触阈值只需改一个常量
   - 所有拖拽相关逻辑在一个类中
   - 坐标转换逻辑集中在 `MouseData`

3. **复用性强**
   - 可复用相同的防误触机制
   - 坐标转换逻辑可被多个组件使用
   - 数据包发送逻辑可扩展

### 关键设计点

```java
// 常量定义
private static final double DRAG_THRESHOLD = 2.0;

// 状态管理（通过 MouseData）
mouseData.setDragTotal(...)
mouseData.setCanDrag(...)
mouseData.getDragTotal()
mouseData.isCanDrag()

// 逻辑分离
handleMouseClick()      // 初始化
handleMouseDrag()       // 累计距离
handleMouseReleased()   // 处理结果

// 坐标转换
updateTransformedMouseCoords()
findClickedTechSlot()
```

---

## 总结

✅ **完整流程**
- 点击 → 播放声音（via TechSlot.mouseClicked）
- 拖拽 → 防误触（2px 阈值）+ 平移内容
- 释放 → 发送数据包到服务器

✅ **高度封装**
- 所有状态通过 `MouseData` 统一管理
- 业务逻辑完全在 `ClientScreenManager` 中
- `ResearchScreenV2` 只负责事件分发

✅ **人体工学**
- 2 像素容差，防止误操作
- 平滑的拖拽体验
- 及时的音频反馈

✅ **可维护性**
- 代码结构清晰
- 易于扩展新功能
- 减少重复代码

