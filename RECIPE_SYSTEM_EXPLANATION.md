# 配方系统设计说明

## 重要概念区分

### 1. Forge 的注册系统（Forge Registration System）
**用途**：注册游戏对象（物品、方块、实体等）到 Forge 的全局注册表
**时机**：Mod 加载阶段（游戏启动时）
**目的**：让 Minecraft 知道你的 Mod 添加了哪些内容

```java
// 这是 Forge 的注册
public static final DeferredRegister<Item> ITEMS = 
    DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

public static final RegistryObject<Item> DIAMOND = 
    ITEMS.register("diamond", () -> new Item(...));
```

---

### 2. 你的 RecipeGUIRegistry（自定义注册表）
**用途**：注册"配方类型 -> GUI 管理器"的映射关系
**时机**：客户端初始化时
**目的**：让你的 Mod 知道如何为不同配方类型显示 GUI

```java
// 这是你的自定义注册系统（与 Forge 无关）
RecipeGUIRegistry.registerAll(); // 客户端启动时调用

public static void registerAll() {
    // 映射：CraftingRecipe -> CraftTable
    RecipeGUIManagerFactory.register(RecipeType.CRAFTING, CraftTable::new);
    // 映射：SmeltingRecipe -> FurnaceTable
    RecipeGUIManagerFactory.register(RecipeType.SMELTING, FurnaceTable::new);
}
```

---

## 配方的注册方式

### ✅ 推荐方式：JSON 数据包
在 `data/yourmod/recipes/` 文件夹下创建 JSON 文件：

```json
{
  "type": "minecraft:crafting_shaped",
  "pattern": ["###", "# #", "###"],
  "key": {
    "#": { "item": "minecraft:stick" }
  },
  "result": { "item": "yourmod:custom_item" }
}
```

**优点**：
- 不需要代码，运行时加载
- 支持数据包覆盖
- 与 KubeJS、CraftTweaker 兼容
- 服务端可以自定义配方

---

### ⚠️ 不推荐：代码注册配方
```java
// 不推荐！配方应该用 JSON
RecipeProvider provider = new RecipeProvider(...) {
    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
        ShapedRecipeBuilder.shaped(Items.DIAMOND_SWORD)
            .pattern(" D ")
            .pattern(" D ")
            .pattern(" S ")
            .define('D', Items.DIAMOND)
            .define('S', Items.STICK)
            .save(consumer);
    }
};
```

**缺点**：
- 硬编码，不灵活
- 无法被数据包覆盖
- 增加代码复杂度

---

### ✅ 需要注册的：自定义配方序列化器
如果你创建了**新的配方类型**（不是原版的 crafting/smelting），需要注册序列化器：

```java
public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = 
    DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MODID);

// 注册自定义配方类型的序列化器
public static final RegistryObject<RecipeSerializer<ResearchRecipe>> RESEARCH_RECIPE = 
    RECIPE_SERIALIZERS.register("research_recipe", 
                                () -> new ResearchRecipeSerializer());
```

然后在 JSON 中使用：
```json
{
  "type": "yourmod:research_recipe",  // 使用你注册的类型
  "ingredients": [...],
  "result": {...}
}
```

---

## 你的系统设计总结

### RecipeGUIRegistry 的作用
**不是 Forge 注册**，而是一个**策略模式注册表**：

```
配方类型             GUI 管理器类
RecipeType.CRAFTING  -> CraftTable
RecipeType.SMELTING  -> FurnaceTable
RecipeType.SMITHING  -> SmithingTable
```

### 使用流程
1. **Mod 初始化时**（客户端）：
   ```java
   @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
   public class ClientSetup {
       @SubscribeEvent
       public static void onClientSetup(FMLClientSetupEvent event) {
           RecipeGUIRegistry.registerAll(); // 注册 GUI 映射
       }
   }
   ```

2. **运行时**：
   ```java
   // 当玩家切换槽位时
   recipeGUIData.setFocusSlot(newSlot); 
   // -> 自动查找对应的 RecipeType
   // -> 从工厂创建对应的 GUI 管理器
   // -> 渲染 GUI
   ```

---

## 为什么这样设计？

### 问题：如果不用注册表
```java
// 每次都要手动判断类型（糟糕的设计）
private RecipeGUIManager<?> createGUIManager(Recipe<?> recipe) {
    if (recipe instanceof CraftingRecipe) {
        return new CraftTable(screen, recipe);
    } else if (recipe instanceof SmeltingRecipe) {
        return new FurnaceTable(screen, recipe);
    } else if (recipe instanceof SmithingRecipe) {
        return new SmithingTable(screen, recipe);
    }
    // 每次添加新类型都要修改这里！
    return null;
}
```

### 解决方案：使用注册表
```java
// 添加新配方类型只需一行
RecipeGUIManagerFactory.register(RecipeType.CRAFTING, CraftTable::new);
RecipeGUIManagerFactory.register(RecipeType.SMELTING, FurnaceTable::new);

// 运行时自动查找
RecipeGUIManager<?> manager = RecipeGUIManagerFactory.create(screen, recipe);
```

**优点**：
- ✅ 添加新类型只需一行代码
- ✅ 不需要修改核心逻辑
- ✅ 支持第三方扩展（其他 Mod 可以注册自己的配方 GUI）
- ✅ 符合开闭原则（Open-Closed Principle）

---

## 总结

| 概念 | 用途 | 何时注册 | 示例 |
|------|------|---------|------|
| **Forge 注册** | 注册游戏对象到全局注册表 | Mod 加载时 | `ITEMS.register("diamond", ...)` |
| **配方 JSON** | 定义配方数据 | 无需注册（数据包） | `data/mod/recipes/xxx.json` |
| **配方序列化器** | 注册自定义配方类型 | Mod 加载时 | `RECIPE_SERIALIZERS.register(...)` |
| **RecipeGUIRegistry** | 注册配方 GUI 映射 | 客户端初始化时 | `register(CRAFTING, CraftTable::new)` |

**关键理解**：
- 配方本身不需要代码注册（用 JSON）
- 只有自定义配方**类型**需要注册序列化器
- 你的 RecipeGUIRegistry 是内部系统，与 Forge 注册表无关
