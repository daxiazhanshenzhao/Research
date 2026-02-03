# SkillsScreen 技能 Tooltip 对齐与渲染

本文说明 `SkillsScreen` 中技能 Tooltip 如何对齐鼠标坐标，以及 Tooltip 的构建与渲染流程。

---

## 1. 对齐鼠标坐标：命中检测使用“内容坐标”

### 1.1 坐标空间

- **屏幕坐标（Screen Space）**：`mouseX / mouseY`，原点是屏幕左上角。
- **内容坐标（Content Space）**：技能节点所在的逻辑坐标系，原点在屏幕中心，受内容平移与缩放影响。

### 1.2 屏幕坐标 -> 内容坐标

`getTransformedMousePos` 将鼠标屏幕坐标转换成内容坐标：

```java
(int) Math.round((mouseX - activeCategoryData.getX() - width / 2.0) / activeCategoryData.getScale())
(int) Math.round((mouseY - activeCategoryData.getY() - height / 2.0) / activeCategoryData.getScale())
```

说明：
- `activeCategoryData.getX()/getY()` 是内容平移偏移。
- `width/2`、`height/2` 将原点移到屏幕中心。
- 除以 `scale` 把缩放还原到内容坐标。

### 1.3 命中检测位置

在 `drawContentWithCategory` 中：

```java
var mouse = getMousePos(mouseX, mouseY);
var transformedMouse = getTransformedMousePos(mouseX, mouseY, activeCategoryData);

if (isInsideContent(mouse)) {
    var optHoveredSkill = activeCategory.skills().values().stream()
        .filter(skill -> activeCategory.getDefinitionById(skill.definitionId())
            .map(definition -> isInsideSkill(transformedMouse, skill, definition))
            .orElse(false)
        )
        .findFirst();
    // ...
}
```

技能的“对齐”并不是把 Tooltip 强制吸附到鼠标，而是 **用内容坐标判断鼠标是否落在技能节点范围**。这样即便背景被拖拽或缩放，命中检测仍正确对齐。

### 1.4 技能命中规则

`isInsideSkill` 使用内容坐标进行矩形范围判定：

```java
var halfSize = Math.round(13f * definition.size());
return transformedMouse.x >= skill.x() - halfSize
    && transformedMouse.y >= skill.y() - halfSize
    && transformedMouse.x < skill.x() + halfSize
    && transformedMouse.y < skill.y() + halfSize;
```

---

## 2. Tooltip 构建流程

当鼠标命中技能后，会构建 Tooltip 文本列表并调用 `setTooltip(lines)`：

```java
var lines = new ArrayList<OrderedText>();
lines.add(definition.title().asOrderedText());
lines.addAll(Tooltip.wrapLines(client, Texts.setStyleIfAbsent(
    definition.description().copy(),
    Style.EMPTY.withFormatting(Formatting.GRAY)
)));
if (Screen.hasShiftDown()) {
    lines.addAll(Tooltip.wrapLines(client, Texts.setStyleIfAbsent(
        definition.extraDescription().copy(),
        Style.EMPTY.withFormatting(Formatting.GRAY)
    )));
}
if (client.options.advancedItemTooltips) {
    lines.add(Text.literal(hoveredSkill.id()).formatted(Formatting.DARK_GRAY).asOrderedText());
}
setTooltip(lines);
```

构建内容：
- **标题**：`definition.title()`
- **描述**：`definition.description()`，灰色格式
- **扩展描述**：按住 Shift 时显示 `definition.extraDescription()`
- **高级信息**：开启高级 Tooltip 时附加技能 ID

---

## 3. Tooltip 的渲染流程

### 3.1 SkillsScreen 的职责

`SkillsScreen` 只负责：
- 判断鼠标是否命中技能
- 生成 Tooltip 文本列表
- 调用 `setTooltip(lines)`

### 3.2 真正的绘制位置

Tooltip 的具体绘制位置 **由 Minecraft GUI 系统处理**：
- `Screen` 会在本帧的 GUI 渲染阶段渲染 Tooltip
- Tooltip 默认跟随当前鼠标坐标显示
- `SkillsScreen` 不直接绘制 Tooltip，而是把内容交给 `Screen`

因此：
- **对齐方式**：命中检测用内容坐标对齐技能图标
- **显示位置**：由 Minecraft 默认 Tooltip 绘制逻辑决定（鼠标附近）

---

## 4. 关键方法索引

- 坐标转换：`getTransformedMousePos(...)`
- 命中检测：`isInsideSkill(...)`
- Tooltip 构建：`drawContentWithCategory(...)` 内 `setTooltip(lines)`
- Tooltip 绘制：由 `Screen` / `DrawContext` 默认流程执行
