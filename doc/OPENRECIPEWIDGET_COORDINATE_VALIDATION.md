# OpenRecipeWidget 坐标验证功能说明

## 概述
为 `OpenRecipeWidget` 添加了完整的坐标验证机制，防止组件位置异常导致的坐标乱飞问题。

## 添加的功能

### 1. 初始位置记录
```java
private final int initialX;
private final int initialY;
private boolean isPositionValidated = true;
```
- 在构造函数中记录组件的初始位置
- 作为后续所有坐标验证的基准点

### 2. 坐标验证方法 `validatePosition(int x, int y)`
验证坐标是否在合理范围内，包括：

#### 屏幕边界检查
```java
// 检查坐标是否在屏幕范围内（留出边界）
if (x < -50 || x > screenWidth + 50) {
    return false;
}
if (y < -50 || y > screenHeight + 50) {
    return false;
}
```
- 允许组件稍微超出屏幕边界（±50像素）
- 防止组件完全飞出屏幕

#### 偏移范围检查
```java
// 检查坐标偏移是否合理（不应该偏离初始位置太远）
int maxOffset = 500; // 最大允许偏移
if (Math.abs(x - initialX) > maxOffset || Math.abs(y - initialY) > maxOffset) {
    return false;
}
```
- 限制组件相对于初始位置的最大偏移量为500像素
- 防止累积误差导致组件越跑越远

### 3. 期望坐标计算 `getExpectedX(boolean isOpen)`
```java
private int getExpectedX(boolean isOpen) {
    return isOpen ? initialX + offX : initialX;
}
```
- 根据当前开关状态计算期望的X坐标
- 用于验证位置是否与状态同步

### 4. 位置修复方法 `fixPosition()`
```java
private void fixPosition() {
    boolean isOpen = screenManager.getScreenData().isOpenRecipe();
    int expectedX = getExpectedX(isOpen);
    int expectedY = initialY + offY;
    
    if (!validatePosition(expectedX, expectedY)) {
        // 如果期望坐标也不合理，回退到初始位置并关闭状态
        screenManager.getScreenData().setOpenRecipe(false);
        this.setPosition(initialX, initialY);
        isPositionValidated = false;
    } else {
        this.setPosition(expectedX, expectedY);
        isPositionValidated = true;
    }
}
```
- 自动修复异常的坐标位置
- 如果修复失败，强制回到初始位置

### 5. 渲染时验证 `renderWidget()`
```java
@Override
protected void renderWidget(...) {
    // 渲染前验证坐标
    if (!validatePosition(this.getX(), this.getY())) {
        fixPosition();
    }
    
    // 验证位置是否与状态同步
    boolean isOpen = screenManager.getScreenData().isOpenRecipe();
    int expectedX = getExpectedX(isOpen);
    if (Math.abs(this.getX() - expectedX) > 5) { // 允许5像素的误差
        fixPosition();
    }
    
    // ...原有渲染逻辑
}
```
- 每帧渲染前检查坐标合法性
- 检查位置与状态是否同步（允许5像素误差）
- 发现异常立即修复

### 6. 点击时验证 `mouseReleased()`
```java
@Override
public boolean mouseReleased(...) {
    // 点击前验证当前坐标
    if (!validatePosition(this.getX(), this.getY())) {
        fixPosition();
        return super.mouseReleased(mouseX, mouseY, button);
    }
    
    // ...状态切换逻辑
    
    // 验证新位置是否合理
    if (validatePosition(newX, newY)) {
        this.setPosition(newX, newY);
        isPositionValidated = true;
    } else {
        // 新位置不合理，回退状态并修复位置
        screenManager.getScreenData().setOpenRecipe(wasOpen);
        fixPosition();
        isPositionValidated = false;
    }
    
    return super.mouseReleased(mouseX, mouseY, button);
}
```
- 点击前验证当前位置
- 计算新位置后验证是否合法
- 如果新位置不合法，回退状态变更

## 防护机制

### 多层防护
1. **渲染时检查**：每帧自动检测和修复
2. **交互时检查**：点击操作前后都进行验证
3. **状态同步检查**：确保位置与开关状态一致
4. **边界限制**：防止超出屏幕和初始位置范围

### 故障恢复
- 如果检测到异常坐标，自动尝试修复到期望位置
- 如果期望位置也不合法，强制回到初始位置并重置状态
- 记录验证状态，便于调试追踪

## 优点

### 1. 防止坐标累积误差
通过记录初始位置，所有计算都基于初始位置，避免相对计算累积误差

### 2. 自动修复机制
发现异常时自动修复，用户无感知，提升用户体验

### 3. 多重验证
渲染和交互时都进行验证，确保万无一失

### 4. 状态同步
确保组件位置始终与开关状态保持一致

### 5. 安全回退
如果无法修复，有安全的回退机制，不会导致崩溃或界面错乱

## 测试建议

1. **正常使用测试**：反复点击开关按钮，观察位置是否正确
2. **窗口缩放测试**：改变窗口大小，观察组件位置调整
3. **极限测试**：快速连续点击，测试状态同步
4. **异常模拟**：尝试触发边界情况，验证修复机制

## 维护建议

1. 如果需要调整最大偏移量限制，修改 `validatePosition()` 中的 `maxOffset` 值
2. 如果需要调整状态同步误差，修改 `renderWidget()` 中的误差阈值（当前为5像素）
3. 建议保留 `isPositionValidated` 字段，虽然当前未使用，但可用于调试和日志记录
