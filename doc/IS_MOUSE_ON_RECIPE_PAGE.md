# isMouseOnRecipePage 方法实现说明

## 方法签名
```java
private boolean isMouseOnRecipePage(double mouseX, double mouseY, ClientScreenManager manager)
```

## 功能说明
判断鼠标是否在配方页面区域内，包括切换按钮和配方页面本身。

## 实现逻辑

### 1. 切换按钮检测（优先级最高）
```java
int buttonX = guiLeft + config.window().u() + OPEN_BUTTON_WIDTH;
int buttonY = guiTop + config.window().v() + OPEN_BUTTON_HEIGHT;
int buttonWidth = 7;
int buttonHeight = 13;

if (mouseX >= buttonX && mouseX < buttonX + buttonWidth &&
    mouseY >= buttonY && mouseY < buttonY + buttonHeight) {
    return true;
}
```

**说明**：
- 按钮位置从 `init()` 方法中的 `OpenRecipeWidget` 创建位置获取
- 按钮大小：7x13（来自 `OpenRecipeWidget` 的常量）
- 无论配方页面打开或关闭，按钮都被认为是在配方页面区域内

### 2. 配方页面打开状态
```java
if (screenData.isOpenRecipe()) {
    int recipeX = screenData.getGuiTextureWidth();
    int recipeY = guiTop + screenData.getGuiTextureHeight();
    int recipeWidth = InsideContext.RECIPE_PAGE_OPEN.width();  // 145
    int recipeHeight = InsideContext.RECIPE_PAGE_OPEN.height(); // 213
    
    return mouseX >= recipeX && mouseX < recipeX + recipeWidth &&
           mouseY >= recipeY && mouseY < recipeY + recipeHeight;
}
```

**说明**：
- 配方页面打开时的尺寸：145x213（来自 `InsideContext.RECIPE_PAGE_OPEN`）
- 位置：使用 `ScreenData` 中保存的纹理坐标

### 3. 配方页面关闭状态
```java
else {
    int recipeX = screenData.getGuiTextureWidth();
    int recipeY = guiTop + screenData.getGuiTextureHeight();
    int recipeWidth = InsideContext.RECIPE_PAGE_CLOSED.width();  // 35
    int recipeHeight = InsideContext.RECIPE_PAGE_CLOSED.height(); // 213
    
    return mouseX >= recipeX && mouseX < recipeX + recipeWidth &&
           mouseY >= recipeY && mouseY < recipeY + recipeHeight;
}
```

**说明**：
- 配方页面关闭时的尺寸：35x213（来自 `InsideContext.RECIPE_PAGE_CLOSED`）
- 只显示一个窄条，宽度仅 35 像素

## 使用场景

此方法可用于：
1. 判断鼠标点击是否应该被配方页面处理
2. 阻止配方页面区域的拖拽操作
3. 控制 tooltip 显示逻辑
4. 区分配方页面和主界面的交互

## 示例用法

```java
@Override
public boolean mouseClicked(double mouseX, double mouseY, int button) {
    ClientScreenManager manager = ClientResearchData.getManager();
    
    // 如果鼠标在配方页面上，不触发主界面的点击逻辑
    if (isMouseOnRecipePage(mouseX, mouseY, manager)) {
        // 处理配方页面的点击
        return true;
    }
    
    // 处理主界面的点击
    // ...
}
```

## 相关常量和类

- `OPEN_BUTTON_WIDTH = 18` - 按钮相对窗口的 X 偏移
- `OPEN_BUTTON_HEIGHT = 85` - 按钮相对窗口的 Y 偏移
- `InsideContext.RECIPE_PAGE_OPEN` - 打开状态的纹理信息（145x213）
- `InsideContext.RECIPE_PAGE_CLOSED` - 关闭状态的纹理信息（35x213）
- `OpenRecipeWidget` - 切换按钮组件（7x13）

## 注意事项

1. **坐标系统**：使用的是屏幕坐标（screen coordinates），不是转换后的世界坐标
2. **按钮优先**：切换按钮始终被认为是配方页面的一部分
3. **动态尺寸**：配方页面的宽度会根据打开/关闭状态动态改变
4. **Z轴层级**：配方页面渲染在最上层（z=5000），按钮渲染在 z=1600

---

**实现日期**: 2026-02-03  
**实现者**: GitHub Copilot
