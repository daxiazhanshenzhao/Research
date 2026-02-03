# SkillsScreen 拖拽与点击检测详解

## 概述

`SkillsScreen` 中鼠标左键的处理分为三个阶段：
1. **Press（按下）** - `mouseClicked`：记录拖拽起点，标记可拖拽状态
2. **Drag（拖动）** - `mouseDragged`：累计移动距离，超过阈值后移动内容
3. **Release（释放）** - `mouseReleased`：根据移动距离判断是点击还是拖拽

关键设计：通过 `dragTotal` 阈值（2px）区分点击和拖拽，防止小的手抖被误判为拖拽。

---

## 1. 按下阶段：`mouseClicked` 与 `mouseClickedWithCategory`

### 入口方法：`mouseClicked`
```java
public boolean mouseClicked(double mouseX, double mouseY, int button) {
    if (button == GLFW.GLFW_MOUSE_BUTTON_1) {  // 仅处理左键
        optActiveCategoryData.ifPresent(activeCategoryData ->
            mouseClickedWithCategory(mouseX, mouseY, activeCategoryData)
        );
    }
    // ... 处理标签页按钮 ...
    return true;
}
```

### 核心逻辑：`mouseClickedWithCategory`
```java
private void mouseClickedWithCategory(double mouseX, double mouseY, ClientCategoryData activeCategoryData) {
    var mouse = getMousePos(mouseX, mouseY);

    // 检查鼠标是否在内容区域内
    if (isInsideContent(mouse)) {
        // 记录拖拽起点（以当前内容位置为基准）
        dragStartX = mouseX - activeCategoryData.getX();
        dragStartY = mouseY - activeCategoryData.getY();
        
        // 重置累计移动距离
        dragTotal = 0;
        
        // 允许拖拽
        canDrag = true;
    } else {
        // 不在内容区域，禁止拖拽
        canDrag = false;
    }

    // ... 处理标签页切换 ...
}
```

**关键点：**
- `dragStartX / dragStartY`：拖拽起点，使用 **屏幕坐标** 记录（未转换）
- `dragTotal`：累计的拖拽距离，初始为 0
- `canDrag`：拖拽使能标志，仅当鼠标在内容区域时为 true

---

## 2. 拖动阶段：`mouseDragged`

### 核心逻辑
```java
public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
    // 如果按下时不在内容区域，忽略此次拖动
    if (!canDrag) {
        return true;
    }

    if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
        // 累计本次拖动的绝对距离
        dragTotal += Math.abs(deltaX);
        dragTotal += Math.abs(deltaY);
        
        // 超过 2px 阈值后才真正移动内容
        if (dragTotal > 2) {
            optActiveCategoryData.ifPresent(activeCategoryData -> {
                // 计算新的内容偏移位置
                applyChangesWithLimits(
                    (int) Math.round(mouseX - dragStartX),           // 新 X 位置
                    (int) Math.round(mouseY - dragStartY),           // 新 Y 位置
                    activeCategoryData.getScale(),                   // 保持当前缩放
                    activeCategoryData
                );
            });
        }
    }

    return true;
}
```

**流程说明：**

1. **检查拖拽使能**：如果 `canDrag=false`，直接返回，不处理任何拖动

2. **累计移动距离**：
   ```
   dragTotal += |deltaX| + |deltaY|
   ```
   - `deltaX / deltaY` 是本帧的移动增量（像素）
   - 使用绝对值，确保无论方向如何都累计距离

3. **判断拖拽阈值**：
   - `dragTotal ≤ 2`：认为是点击（允许小的手抖）
   - `dragTotal > 2`：认为是拖拽，开始移动内容

4. **移动内容**：使用 `applyChangesWithLimits` 更新 `activeCategoryData` 的位置，**同时移动背景和技能节点**

---

## 3. 释放阶段：`mouseReleased` 与 `mouseReleasedWithCategory`

### 入口方法：`mouseReleased`
```java
public boolean mouseReleased(double mouseX, double mouseY, int button) {
    if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
        // 如果拖拽距离超过 2px，视为拖拽结束，不触发点击
        if (dragTotal > 2) {
            return true;  // 拖拽结束，无后续操作
        }

        // 否则继续检测点击逻辑
        optActiveCategoryData.ifPresent(activeCategoryData ->
            mouseReleasedWithCategory(mouseX, mouseY, activeCategoryData)
        );
    }

    return true;
}
```

### 点击检测：`mouseReleasedWithCategory`
```java
private void mouseReleasedWithCategory(double mouseX, double mouseY, ClientCategoryData activeCategoryData) {
    // 转换为屏幕坐标
    var mouse = getMousePos(mouseX, mouseY);
    
    // 转换为内容坐标（考虑平移与缩放）
    var transformedMouse = getTransformedMousePos(mouseX, mouseY, activeCategoryData);
    
    var activeCategory = activeCategoryData.getConfig();

    // 检查鼠标是否在内容区域内
    if (isInsideContent(mouse)) {
        // 遍历所有技能
        for (var skill : activeCategory.skills().values()) {
            var definition = activeCategory.definitions().get(skill.definitionId());
            if (definition == null) {
                continue;
            }

            // 检测点击是否命中该技能
            if (isInsideSkill(transformedMouse, skill, definition)) {
                // 发送点击包到服务器
                SkillsClientMod.getInstance()
                    .getPacketSender()
                    .send(new SkillClickOutPacket(activeCategory.id(), skill.id()));
            }
        }
    }
}
```

**执行条件：**
- `dragTotal ≤ 2`（认为是点击，不是拖拽）
- 鼠标在内容区域内

**坐标转换：**
```
内容坐标 = (
    (屏幕X - 内容偏移X - 屏幕中心X) / 缩放,
    (屏幕Y - 内容偏移Y - 屏幕中心Y) / 缩放
)
```

---

## 4. 坐标空间与转换

### 屏幕坐标 vs 内容坐标

**屏幕坐标**（Screen Space）
- 原点在屏幕左上角
- 直接来自鼠标事件的 `mouseX / mouseY`
- 用于 UI 区域判断（标签页、内容框边界等）

**内容坐标**（Content Space）
- 原点在屏幕中心
- 考虑了内容的平移（`activeCategoryData.getX/Y`）和缩放（`activeCategoryData.getScale`）
- 用于技能节点的命中检测

### 转换方法

#### 屏幕坐标
```java
private Vector2i getMousePos(double mouseX, double mouseY) {
    return new Vector2i((int) mouseX, (int) mouseY);
}
```

#### 内容坐标
```java
private Vector2i getTransformedMousePos(double mouseX, double mouseY, 
                                        ClientCategoryData activeCategoryData) {
    return new Vector2i(
        (int) Math.round((mouseX - activeCategoryData.getX() - width / 2.0) 
                         / activeCategoryData.getScale()),
        (int) Math.round((mouseY - activeCategoryData.getY() - height / 2.0) 
                         / activeCategoryData.getScale())
    );
}
```

公式说明：
- `mouseX - activeCategoryData.getX()`：消除内容的 X 偏移
- `- width / 2.0`：转换原点到屏幕中心
- `/ activeCategoryData.getScale()`：消除缩放因子

---

## 5. 命中检测细节

### 内容区域
```java
private boolean isInsideContent(Vector2i mouse) {
    return mouse.x >= contentPaddingLeft 
        && mouse.y >= contentPaddingTop 
        && mouse.x < width - contentPaddingRight 
        && mouse.y < height - contentPaddingBottom;
}
```
检查鼠标是否在可交互的内容框内。

### 技能按钮
```java
private boolean isInsideSkill(Vector2i transformedMouse, ClientSkillConfig skill, 
                               ClientSkillDefinitionConfig definition) {
    var halfSize = Math.round(13f * definition.size());
    return transformedMouse.x >= skill.x() - halfSize 
        && transformedMouse.y >= skill.y() - halfSize 
        && transformedMouse.x < skill.x() + halfSize 
        && transformedMouse.y < skill.y() + halfSize;
}
```

**原理：**
- 使用 **内容坐标** 进行判断
- 以技能中心为基准，构建正方形判定框
- 半径 = `13 * 技能定义的size`
- 判定框范围：`[center - halfSize, center + halfSize)`

---

## 6. 状态变量说明

| 变量 | 类型 | 用途 | 赋值时机 |
|------|------|------|---------|
| `dragStartX / dragStartY` | `double` | 拖拽起点坐标（屏幕坐标） | `mouseClicked` |
| `dragTotal` | `double` | 累计拖拽距离（像素） | `mouseClicked` 时清零，`mouseDragged` 时累加 |
| `canDrag` | `boolean` | 是否允许拖拽 | 根据鼠标在 `mouseClicked` 时是否在内容区域 |

---

## 7. 背景与内容同步移动

拖拽过程中，背景与技能节点共享同一套矩阵变换：

```java
matrices.translate(
    activeCategoryData.getX() + width / 2f,
    activeCategoryData.getY() + height / 2f,
    0f
);
matrices.scale(
    activeCategoryData.getScale(),
    activeCategoryData.getScale(),
    1f
);
```

因此：
- 修改 `activeCategoryData.getX()/getY()` 会同时平移背景和技能
- 修改 `activeCategoryData.getScale()` 会同时缩放背景和技能
- **背景与技能始终保持一致的变换**

---

## 8. `applyChangesWithLimits` - 限制拖拽范围

```java
private void applyChangesWithLimits(int x, int y, float scale, 
                                    ClientCategoryData activeCategoryData) {
    var halfWidth = this.width / 2;
    var halfHeight = this.height / 2;

    // 限制缩放范围
    scale = MathHelper.clamp(scale, minScale, maxScale);

    // 限制 X 偏移范围
    activeCategoryData.setX(MathHelper.clamp(
        x,
        (int) Math.ceil(halfWidth - contentPaddingRight - bounds.max().x() * scale),
        (int) Math.floor(contentPaddingLeft - halfWidth - bounds.min().x() * scale)
    ));

    // 限制 Y 偏移范围
    activeCategoryData.setY(MathHelper.clamp(
        y,
        (int) Math.ceil(halfHeight - contentPaddingBottom - bounds.max().y() * scale),
        (int) Math.floor(contentPaddingTop - halfHeight - bounds.min().y() * scale)
    ));

    activeCategoryData.setScale(scale);
}
```

**作用：**
- 防止用户拖拽超出内容边界
- 基于 `bounds`（内容的逻辑边界）和 `scale`（当前缩放）动态计算允许的偏移范围
- 使用 `MathHelper.clamp` 确保 X、Y 位置在安全范围内

---

## 9. 完整交互时序

```
用户按下左键
    ↓
mouseClicked() 被调用
    ↓
    ├─ 检查鼠标是否在内容区域
    ├─ 记录 dragStartX / dragStartY
    ├─ dragTotal = 0
    └─ canDrag = true（或 false）
    
    ↓（持续按住并移动）
    
mouseDragged() 被调用（可能多次）
    ↓
    ├─ 检查 canDrag
    ├─ dragTotal += |deltaX| + |deltaY|
    │
    └─ 若 dragTotal > 2
        └─ applyChangesWithLimits() → 更新位置，移动背景与技能
    
    ↓（释放左键）
    
mouseReleased() 被调用
    ↓
    ├─ 若 dragTotal > 2
    │   └─ 认为是拖拽，结束（不进行点击检测）
    │
    └─ 若 dragTotal ≤ 2
        ├─ 认为是点击
        ├─ getTransformedMousePos() 获取内容坐标
        └─ 遍历技能，isInsideSkill() 判定
            └─ 命中 → 发送 SkillClickOutPacket
```

---

## 10. 拖拽与点击的关键区别

| 特性 | 点击（Click） | 拖拽（Drag） |
|------|---|---|
| **dragTotal** | ≤ 2 px | > 2 px |
| **触发时机** | `mouseReleased` 时 | `mouseDragged` 中持续更新 |
| **坐标使用** | 内容坐标，进行命中检测 | 屏幕坐标，直接计算新位置 |
| **结果** | 发送 `SkillClickOutPacket` 到服务器 | 平移背景与技能节点 |
| **约束** | 必须在内容区域内 | 同样受 `applyChangesWithLimits` 限制 |

---

## 11. 总结

**按下（Press）**
- 记录起点 + 标志位

**拖动（Drag）**
- 累计距离 + 超阈值后更新位置

**释放（Release）**
- 根据距离判断是点击（发送包）还是拖拽（无操作）

**关键创新：** 用 `dragTotal` 和 2px 阈值巧妙地区分手抖（点击）和有意拖拽，同时保证背景、技能、缩放的同步变换。
