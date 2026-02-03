# 配方页面交互逻辑隔离实现

## 修改目标

当鼠标在配方页面区域内时：
- ❌ **不触发** TechSlot 的任何逻辑（点击、拖拽、滚动、tooltip）
- ✅ **触发** Screen 自身的 widget 逻辑（如 OpenRecipeWidget 按钮）

## 修改的方法

### 1. mouseReleased - 鼠标释放事件

**修改前：**
```java
@Override
public boolean mouseReleased(double mouseX, double mouseY, int button) {
    if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
        ClientResearchData.getManager().handleMouseReleased(mouseX, mouseY, button);
    }
    return super.mouseReleased(mouseX, mouseY, button);
}
```

**修改后：**
```java
@Override
public boolean mouseReleased(double mouseX, double mouseY, int button) {
    if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
        ClientScreenManager manager = ClientResearchData.getManager();
        
        // 如果鼠标在配方页面上，不触发 TechSlot 的逻辑，让 widget 处理
        if (!isMouseOnRecipePage(mouseX, mouseY, manager)) {
            manager.handleMouseReleased(mouseX, mouseY, button);
        }
    }
    return super.mouseReleased(mouseX, mouseY, button);
}
```

**效果**：在配方页面上释放鼠标不会触发 TechSlot 的 focus 设置

---

### 2. mouseDragged - 鼠标拖拽事件

**修改前：**
```java
@Override
public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
    if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
        ClientResearchData.getManager().handleMouseDrag(mouseX, mouseY, button, dragX, dragY);
        return true;
    }
    return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
}
```

**修改后：**
```java
@Override
public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
    if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
        ClientScreenManager manager = ClientResearchData.getManager();
        
        // 如果鼠标在配方页面上，不触发 TechSlot 的拖拽逻辑
        if (!isMouseOnRecipePage(mouseX, mouseY, manager)) {
            manager.handleMouseDrag(mouseX, mouseY, button, dragX, dragY);
        }
        return true;
    }
    return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
}
```

**效果**：在配方页面上拖拽不会触发科技树的平移

---

### 3. mouseClicked - 鼠标点击事件

**修改前：**
```java
@Override
public boolean mouseClicked(double mouseX, double mouseY, int button) {
    if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
        ClientResearchData.getManager().handleMouseClick(mouseX, mouseY, button);
        return true;
    }
    return super.mouseClicked(mouseX, mouseY, button);
}
```

**修改后：**
```java
@Override
public boolean mouseClicked(double mouseX, double mouseY, int button) {
    if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
        ClientScreenManager manager = ClientResearchData.getManager();
        
        // 如果鼠标在配方页面上，不触发 TechSlot 的点击逻辑，让 widget 处理
        if (!isMouseOnRecipePage(mouseX, mouseY, manager)) {
            manager.handleMouseClick(mouseX, mouseY, button);
        }
        return true;
    }
    return super.mouseClicked(mouseX, mouseY, button);
}
```

**效果**：在配方页面上点击不会触发 TechSlot 的点击声音和拖拽初始化

---

### 4. mouseScrolled - 鼠标滚轮事件

**修改前：**
```java
@Override
public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
    ClientResearchData.getManager().handleMouseScrolled(mouseX, mouseY, delta);
    return super.mouseScrolled(mouseX, mouseY, delta);
}
```

**修改后：**
```java
@Override
public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
    ClientScreenManager manager = ClientResearchData.getManager();
    
    // 如果鼠标在配方页面上，不触发缩放逻辑
    if (!isMouseOnRecipePage(mouseX, mouseY, manager)) {
        manager.handleMouseScrolled(mouseX, mouseY, delta);
    }
    return super.mouseScrolled(mouseX, mouseY, delta);
}
```

**效果**：在配方页面上滚动鼠标不会缩放科技树

---

### 5. renderTooltips - 渲染 Tooltip

**修改前：**
```java
private void renderTooltips(GuiGraphics context, ClientScreenManager manager,
                            int screenMouseX, int screenMouseY) {
    // 检查鼠标是否在内部区域
    if (!manager.isMouseInSide(screenMouseX, screenMouseY)) {
        return;
    }

    // 使用 manager 的方法查找当前悬停的 TechSlot
    TechSlot hoveredTechSlot = manager.findHoveredTechSlot();
    if (hoveredTechSlot == null) {
        return;
    }

    // 渲染 TechSlot 的 tooltip
    hoveredTechSlot.renderTooltip(context, screenMouseX, screenMouseY);
}
```

**修改后：**
```java
private void renderTooltips(GuiGraphics context, ClientScreenManager manager,
                            int screenMouseX, int screenMouseY) {
    // 如果鼠标在配方页面上，不渲染 TechSlot 的 tooltip
    if (isMouseOnRecipePage(screenMouseX, screenMouseY, manager)) {
        return;
    }
    
    // 检查鼠标是否在内部区域
    if (!manager.isMouseInSide(screenMouseX, screenMouseY)) {
        return;
    }

    // 使用 manager 的方法查找当前悬停的 TechSlot
    TechSlot hoveredTechSlot = manager.findHoveredTechSlot();
    if (hoveredTechSlot == null) {
        return;
    }

    // 渲染 TechSlot 的 tooltip
    hoveredTechSlot.renderTooltip(context, screenMouseX, screenMouseY);
}
```

**效果**：在配方页面上悬停不会显示 TechSlot 的 tooltip

---

## 核心判断方法

所有修改都依赖于 `isMouseOnRecipePage()` 方法：

```java
private boolean isMouseOnRecipePage(double mouseX, double mouseY, ClientScreenManager manager) {
    var screenData = manager.getScreenData();
    var config = manager.getScreenConfigData();
    
    int guiLeft = screenData.getGuiLeft();
    int guiTop = screenData.getGuiTop();
    
    // 1. 检查切换按钮区域
    int buttonX = guiLeft + config.window().u() + OPEN_BUTTON_WIDTH;
    int buttonY = guiTop + config.window().v() + OPEN_BUTTON_HEIGHT;
    int buttonWidth = 7;
    int buttonHeight = 13;
    
    if (mouseX >= buttonX && mouseX < buttonX + buttonWidth &&
        mouseY >= buttonY && mouseY < buttonY + buttonHeight) {
        return true;
    }

    // 2. 根据打开/关闭状态检查配方页面区域
    if (screenData.isOpenRecipe()) {
        // 打开状态：145x213
        int recipeX = screenData.getGuiTextureWidth();
        int recipeY = guiTop + screenData.getGuiTextureHeight();
        int recipeWidth = InsideContext.RECIPE_PAGE_OPEN.width();
        int recipeHeight = InsideContext.RECIPE_PAGE_OPEN.height();
        
        return mouseX >= recipeX && mouseX < recipeX + recipeWidth &&
               mouseY >= recipeY && mouseY < recipeY + recipeHeight;
    } else {
        // 关闭状态：35x213
        int recipeX = screenData.getGuiTextureWidth();
        int recipeY = guiTop + screenData.getGuiTextureHeight();
        int recipeWidth = InsideContext.RECIPE_PAGE_CLOSED.width();
        int recipeHeight = InsideContext.RECIPE_PAGE_CLOSED.height();
        
        return mouseX >= recipeX && mouseX < recipeX + recipeWidth &&
               mouseY >= recipeY && mouseY < recipeY + recipeHeight;
    }
}
```

## 交互流程

### 在主界面区域（非配方页面）

```
用户点击 TechSlot
    ↓
mouseClicked() → isMouseOnRecipePage() = false
    ↓
manager.handleMouseClick() ✅ 执行
    ↓
TechSlot 播放点击声音
```

### 在配方页面区域

```
用户点击切换按钮
    ↓
mouseClicked() → isMouseOnRecipePage() = true
    ↓
manager.handleMouseClick() ❌ 不执行
    ↓
super.mouseClicked() ✅ 让 Screen 的 widget 处理
    ↓
OpenRecipeWidget.onPress() ✅ 执行
    ↓
配方页面切换开关
```

## 受影响的功能

### 被禁用的功能（在配方页面区域内）
1. ❌ TechSlot 点击
2. ❌ TechSlot 拖拽
3. ❌ 科技树平移
4. ❌ 科技树缩放
5. ❌ TechSlot tooltip
6. ❌ Focus 状态设置

### 正常工作的功能（在配方页面区域内）
1. ✅ OpenRecipeWidget 按钮点击
2. ✅ 未来的配方列表滚动
3. ✅ 未来的配方项点击
4. ✅ 未来的配方 tooltip

## 优势

1. **清晰的交互边界**：配方页面和主界面互不干扰
2. **符合用户预期**：在配方页面上的操作不会影响背后的科技树
3. **可扩展性**：未来添加配方相关的 widget 会自动继承正确的交互逻辑
4. **性能优化**：配方页面区域不会进行不必要的 TechSlot 查找和渲染

## 测试场景

### 场景 1：配方页面关闭
- 点击切换按钮 → ✅ 页面打开，科技树不受影响
- 在主界面拖拽 → ✅ 科技树正常平移
- 在主界面点击 TechSlot → ✅ 正常设置 focus

### 场景 2：配方页面打开
- 在配方页面区域拖拽 → ❌ 科技树不平移
- 在配方页面区域滚动 → ❌ 科技树不缩放
- 悬停在配方页面区域 → ❌ 不显示 TechSlot tooltip
- 点击切换按钮 → ✅ 页面关闭

### 场景 3：边界测试
- 鼠标从主界面拖拽到配方页面 → ✅ 拖拽可以开始但进入配方区域后停止响应
- 鼠标从配方页面移出到主界面 → ✅ tooltip 正确显示

---

**实现日期**: 2026-02-03  
**实现者**: GitHub Copilot  
**相关文件**: 
- `ResearchScreenV2.java` - 所有鼠标事件处理
- `IS_MOUSE_ON_RECIPE_PAGE.md` - 判断方法详细说明
