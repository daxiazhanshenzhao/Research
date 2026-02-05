# 搜索功能实现说明

## 概述
搜索功能允许玩家通过物品名称搜索科技，支持多语言（自动适配当前游戏语言），并提供分页浏览功能。

## 功能特性

### 1. 多语言支持
- 自动使用物品的本地化名称进行搜索
- 支持任何 Minecraft 支持的语言（中文、英文、日文等）
- 搜索时使用 `ItemStack.getHoverName().getString()` 获取本地化名称

### 2. 智能搜索模式
- **空搜索**：搜索框为空时，显示所有科技物品，按首字母排序
- **关键词搜索**：输入关键词后，模糊匹配包含该关键词的物品名称

### 3. 分页功能
- 每页显示 20 个搜索结果（5列 × 4行）
- 支持上一页/下一页翻页
- 翻页按钮自动显示/隐藏

## 架构设计

### 核心类和方法

#### 1. TechSlotData（数据层）
负责搜索逻辑和分页管理：

```java
// 执行搜索
public synchronized void performSearch(String searchText)

// 切换页码
public synchronized boolean changePage(boolean next)

// 获取当前页槽位
public TechSlot getSlotById(int id)

// 获取总页数
public int getTotalPages()
```

**关键常量：**
- `SLOTS_PER_PAGE = 20`：每页槽位数量

#### 2. ClientScreenManager（管理层）
提供搜索功能的封装接口：

```java
// 处理搜索框输入
public void handleSearchEditBox(String value)

// 根据ID获取槽位
public TechSlot getTechIconById(int id)

// 处理翻页按钮
public void handleChangePageButton(boolean nextPage)
```

#### 3. SearchTechSlot（UI层）
搜索结果槽位组件：

- 渲染物品图标
- 显示物品 tooltip
- 点击后聚焦对应科技

#### 4. SearchEditBox（UI层）
搜索输入框：

- 每帧调用 `tick()` 方法触发搜索
- 自动同步输入内容到搜索逻辑

#### 5. ChangePageButton（UI层）
翻页按钮：

- 左箭头：上一页
- 右箭头：下一页
- Hover 时高亮显示

## 搜索流程

```
用户输入 → SearchEditBox.tick() → ClientScreenManager.handleSearchEditBox()
         ↓
    TechSlotData.performSearch()
         ↓
    [搜索框为空] → populateAllItemsSorted()（按首字母排序所有物品）
         ↓
    [有关键词] → 遍历 cache，匹配物品本地化名称
         ↓
    updateCurrentPageSlots()（更新当前页显示的20个槽位）
         ↓
    SearchTechSlot.renderWidget()（渲染每个槽位的物品图标）
```

## 初始化流程

1. **ResearchScreenV2.init()**
   - 创建 20 个 `SearchTechSlot` 组件（ID: 0-19）
   - 创建 2 个 `ChangePageButton`（上一页、下一页）
   - 调用 `manager.handleSearchEditBox("")` 触发初始搜索

2. **首次显示**
   - 搜索框为空，显示按首字母排序的前 20 个物品
   - 如果物品总数 > 20，显示翻页按钮

## 使用示例

### 搜索特定物品
```java
// 用户在搜索框输入 "iron"
SearchEditBox → 输入 "iron"
              ↓
TechSlotData → 搜索所有包含 "iron" 的物品
              ↓
显示结果：Iron Ingot, Iron Ore, Iron Block...
```

### 浏览所有物品
```java
// 用户清空搜索框
SearchEditBox → 输入 ""
              ↓
TechSlotData → 显示所有物品，按 A-Z 排序
              ↓
第1页：显示前 20 个物品
点击"下一页" → 显示第 21-40 个物品
```

## 关键实现细节

### 1. 首字母排序
```java
validSlots.sort((slot1, slot2) -> {
    String name1 = item1.getHoverName().getString().toLowerCase();
    String name2 = item2.getHoverName().getString().toLowerCase();
    return name1.compareTo(name2);
});
```

### 2. 多语言匹配
```java
// 获取本地化名称（自动适配当前语言）
String itemName = resultItem.getHoverName().getString().toLowerCase();
if (itemName.contains(lowerSearchText)) {
    searchResults.add(slot);
}
```

### 3. 分页计算
```java
int startIndex = currentPage * SLOTS_PER_PAGE;
int endIndex = Math.min(startIndex + SLOTS_PER_PAGE, searchResults.size());
```

## UI 布局

```
配方页面（145px 宽）
┌─────────────────────────────────┐
│  [搜索框 65x17]                  │
├─────────────────────────────────┤
│  [物品1] [物品2] [物品3] [物品4] [物品5]  │  ← 第1排
│  [物品6] [物品7] [物品8] [物品9] [物品10] │  ← 第2排
│  [物品11][物品12][物品13][物品14][物品15] │  ← 第3排
│  [物品16][物品17][物品18][物品19][物品20] │  ← 第4排
├─────────────────────────────────┤
│  [◀] 页码显示 [▶]               │
└─────────────────────────────────┘

每个物品槽位：20x20 像素
槽位间距：2px
```

## 扩展性

### 添加新的搜索条件
可以在 `TechSlotData.performSearch()` 中扩展搜索逻辑：

```java
// 示例：同时搜索科技ID
String techId = slot.getTechInstance().getIdentifier().toString();
if (techId.contains(lowerSearchText)) {
    searchResults.add(slot);
}
```

### 调整每页显示数量
修改 `TechSlotData.SLOTS_PER_PAGE` 常量，并相应调整 UI 布局。

## 性能优化

1. **惰性渲染**：只渲染当前页的 20 个槽位
2. **缓存机制**：搜索结果缓存在 `searchResults` 中
3. **增量更新**：只在输入变化时重新搜索

## 测试要点

- [ ] 搜索框为空时显示前20个物品（按A-Z排序）
- [ ] 输入英文关键词能正确搜索
- [ ] 输入中文关键词能正确搜索（如果游戏语言为中文）
- [ ] 翻页功能正常工作
- [ ] Hover 显示物品 tooltip
- [ ] 点击物品后正确聚焦对应科技
- [ ] 搜索结果数量正确显示
