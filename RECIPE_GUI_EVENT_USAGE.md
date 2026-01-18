# RecipeGUI 事件注册系统使用指南

## 概述

`RegisterRecipeGUIEvent` 是一个 Forge MOD 总线事件，用于注册配方 GUI 管理器。它支持三种注册方式，从简单到复杂满足不同需求。

---

## 三种注册方式

### 方式 1：传统类注册（适合复杂 GUI）

**使用场景**：需要复杂逻辑、自定义方法、或需要继承的场景

**示例**：工作台配方（已实现）

```java
@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RecipeGUIRegistry {
    
    @SubscribeEvent
    public static void onRegisterRecipeGUI(RegisterRecipeGUIEvent event) {
        // 注册自定义类
        event.register(RecipeType.CRAFTING, CraftTable::new);
    }
}
```

**对应的类**：

```java
public class CraftTable extends RecipeGUIManager<CraftingRecipe> {
    
    public CraftTable(ResearchContainerScreen screen, CraftingRecipe recipe) {
        super(screen, recipe);
    }

    @Override
    public void setRecipe() {
        // 复杂的 3×3 网格布局逻辑
        // ... 
    }

    @Override
    public ResourceLocation getBackgroundTexture() {
        return new ResourceLocation("research", "textures/gui/crafting.png");
    }
}
```

**优点**：
- 逻辑清晰，易于维护
- 可以添加自定义方法
- 适合复杂布局

**缺点**：
- 需要创建新类
- 简单配方显得冗余

---

### 方式 2：构建器注册（推荐，适合简单 GUI）

**使用场景**：简单的配方布局，不需要额外方法

**示例 1：熔炉配方**

```java
@SubscribeEvent
public static void onRegisterRecipeGUI(RegisterRecipeGUIEvent event) {
    
    event.builder(RecipeType.SMELTING)
            .setRecipeHandler((manager, recipe) -> {
                // 获取输入和输出
                List<ItemStack> inputs = List.of(recipe.getIngredients().get(0).getItems());
                ItemStack output = IRecipe.getResultItem(recipe);
                
                int baseX = manager.getGUILeft() + 30;
                int baseY = manager.getGUITop() + 30;
                
                // 输入槽（左侧）
                RecipeTechSlot inputSlot = new RecipeTechSlot(
                    baseX, baseY, inputs, manager.screen
                );
                manager.addWidgets(inputSlot, RecipeIngredientRole.INPUT);
                
                // 输出槽（右侧）
                RecipeTechSlot outputSlot = new RecipeTechSlot(
                    baseX + 60, baseY, List.of(output), manager.screen
                );
                manager.addWidgets(outputSlot, RecipeIngredientRole.OUTPUT);
            })
            .setBackground(new ResourceLocation("research", "textures/gui/furnace.png"))
            .register();
}
```

**示例 2：切石机配方**

```java
event.builder(RecipeType.STONECUTTING)
        .setRecipeHandler((manager, recipe) -> {
            int x = manager.getGUILeft() + 40;
            int y = manager.getGUITop() + 30;
            
            // 输入
            manager.addWidgets(
                new RecipeTechSlot(x, y, 
                    List.of(recipe.getIngredients().get(0).getItems()), 
                    manager.screen),
                RecipeIngredientRole.INPUT
            );
            
            // 输出
            manager.addWidgets(
                new RecipeTechSlot(x + 50, y, 
                    List.of(IRecipe.getResultItem(recipe)), 
                    manager.screen),
                RecipeIngredientRole.OUTPUT
            );
        })
        .setBackground(new ResourceLocation("minecraft", "textures/gui/container/stonecutter.png"))
        .register();
```

**优点**：
- 无需创建类
- 代码简洁
- 适合简单布局

**缺点**：
- 无法添加自定义方法
- 逻辑复杂时可读性下降

---

### 方式 3：快捷注册（最简洁）

**使用场景**：非常简单的配方，只有几个槽位

**示例**：铁砧配方

```java
@SubscribeEvent
public static void onRegisterRecipeGUI(RegisterRecipeGUIEvent event) {
    
    event.registerSimple(
            RecipeType.SMITHING,
            (manager, recipe) -> {
                int baseX = manager.getGUILeft() + 20;
                int baseY = manager.getGUITop() + 20;
                
                // 输入 1
                manager.addWidgets(
                    new RecipeTechSlot(baseX, baseY, 
                        List.of(recipe.getIngredients().get(0).getItems()), 
                        manager.screen),
                    RecipeIngredientRole.INPUT
                );
                
                // 输入 2
                manager.addWidgets(
                    new RecipeTechSlot(baseX + 30, baseY, 
                        List.of(recipe.getIngredients().get(1).getItems()), 
                        manager.screen),
                    RecipeIngredientRole.INPUT
                );
                
                // 输出
                manager.addWidgets(
                    new RecipeTechSlot(baseX + 80, baseY, 
                        List.of(IRecipe.getResultItem(recipe)), 
                        manager.screen),
                    RecipeIngredientRole.OUTPUT
                );
            },
            new ResourceLocation("minecraft", "textures/gui/container/smithing.png")
    );
}
```

**优点**：
- 最简洁
- 一行调用完成注册

**缺点**：
- 与方式 2 本质相同，只是语法糖

---

## API 参考

### RegisterRecipeGUIEvent 方法

| 方法 | 说明 |
|------|------|
| `register(RecipeType, BiFunction)` | 注册自定义管理器类 |
| `builder(RecipeType)` | 获取构建器 |
| `registerSimple(RecipeType, RecipeHandler, ResourceLocation)` | 快捷注册 |
| `isRegistered(RecipeType)` | 检查是否已注册 |

### ManagerBuilder 方法

| 方法 | 说明 |
|------|------|
| `setRecipeHandler(RecipeHandler)` | 设置 setRecipe 实现 |
| `setBackground(ResourceLocation)` | 设置背景纹理 |
| `register()` | 完成注册 |

### RecipeHandler 接口

```java
@FunctionalInterface
public interface RecipeHandler<T extends Recipe<?>> {
    void setup(RecipeGUIManager<T> manager, T recipe);
}
```

**参数说明**：
- `manager`：可以调用 `addWidgets()`, `getGUILeft()`, `getGUITop()` 等方法
- `recipe`：当前配方实例

---

## 最佳实践

### 1. 选择合适的注册方式

```
复杂布局（如工作台）     -> 方式 1（传统类）
简单布局（如熔炉）       -> 方式 2（构建器）
极简布局（如切石机）     -> 方式 3（快捷注册）
```

### 2. 坐标计算

```java
// 获取 GUI 左上角坐标
int guiLeft = manager.getGUILeft();
int guiTop = manager.getGUITop();

// 基于 GUI 计算槽位坐标
int slotX = guiLeft + 30;  // 距离左边 30 像素
int slotY = guiTop + 20;   // 距离顶部 20 像素
```

### 3. 槽位间距

```java
int slotSize = 18;  // 标准槽位大小（16 像素 + 2 像素间距）

// 水平排列
int x1 = baseX;
int x2 = baseX + slotSize;
int x3 = baseX + slotSize * 2;

// 垂直排列
int y1 = baseY;
int y2 = baseY + slotSize;
int y3 = baseY + slotSize * 2;
```

### 4. 多变体物品

```java
// 原料可能有多个变体（如木板有多种）
List<ItemStack> variants = List.of(ingredient.getItems());

RecipeTechSlot slot = new RecipeTechSlot(x, y, variants, manager.screen);
manager.addWidgets(slot, RecipeIngredientRole.INPUT);
```

---

## 完整示例

### 注册多个配方类型

```java
@Mod.EventBusSubscriber(modid = Research.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RecipeGUIRegistry {

    @SubscribeEvent
    public static void onRegisterRecipeGUI(RegisterRecipeGUIEvent event) {
        
        // 工作台（复杂布局，使用类）
        event.register(RecipeType.CRAFTING, CraftTable::new);
        
        // 熔炉（简单布局，使用构建器）
        event.builder(RecipeType.SMELTING)
                .setRecipeHandler(RecipeGUIRegistry::setupFurnace)
                .setBackground(new ResourceLocation(Research.MODID, "textures/gui/furnace.png"))
                .register();
        
        // 高炉（复用熔炉布局）
        event.builder(RecipeType.BLASTING)
                .setRecipeHandler(RecipeGUIRegistry::setupFurnace)
                .setBackground(new ResourceLocation(Research.MODID, "textures/gui/blast_furnace.png"))
                .register();
        
        // 切石机（极简布局，快捷注册）
        event.registerSimple(
                RecipeType.STONECUTTING,
                RecipeGUIRegistry::setupStonecutter,
                new ResourceLocation(Research.MODID, "textures/gui/stonecutter.png")
        );
    }
    
    // 提取复用逻辑
    private static <T extends Recipe<?>> void setupFurnace(RecipeGUIManager<T> manager, T recipe) {
        List<ItemStack> inputs = List.of(recipe.getIngredients().get(0).getItems());
        ItemStack output = IRecipe.getResultItem(recipe);
        
        int baseX = manager.getGUILeft() + 30;
        int baseY = manager.getGUITop() + 30;
        
        manager.addWidgets(
            new RecipeTechSlot(baseX, baseY, inputs, manager.screen),
            RecipeIngredientRole.INPUT
        );
        
        manager.addWidgets(
            new RecipeTechSlot(baseX + 60, baseY, List.of(output), manager.screen),
            RecipeIngredientRole.OUTPUT
        );
    }
    
    private static <T extends Recipe<?>> void setupStonecutter(RecipeGUIManager<T> manager, T recipe) {
        // 与 setupFurnace 相同，但坐标不同
        // ...
    }
}
```

---

## 扩展性

### 其他模组如何添加配方支持

其他模组只需监听同一事件：

```java
@Mod.EventBusSubscriber(modid = "mymod", bus = Mod.EventBusSubscriber.Bus.MOD)
public class MyModRecipeGUI {
    
    @SubscribeEvent
    public static void registerMyRecipes(RegisterRecipeGUIEvent event) {
        // 注册自定义配方类型
        event.builder(MyModRecipeTypes.MY_CUSTOM_RECIPE)
                .setRecipeHandler((manager, recipe) -> {
                    // 自定义布局
                })
                .setBackground(new ResourceLocation("mymod", "textures/gui/my_gui.png"))
                .register();
    }
}
```

---

## 总结

| 特性 | 方式 1（类） | 方式 2（构建器） | 方式 3（快捷） |
|------|-------------|------------------|---------------|
| 代码量 | 多 | 中 | 少 |
| 适用场景 | 复杂逻辑 | 简单布局 | 极简布局 |
| 可扩展性 | 高 | 低 | 低 |
| 推荐度 | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐ |

**推荐策略**：
- 默认使用**方式 2（构建器）**
- 遇到复杂需求时升级到**方式 1（类）**
- 极简场景用**方式 3（快捷）**作为语法糖
