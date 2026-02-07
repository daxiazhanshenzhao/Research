package org.research.api.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import org.research.api.recipe.RecipeIngredientRole;
import org.research.api.recipe.RecipeWrapper;
import org.research.api.recipe.category.SlotBuilder;

import java.util.*;

public class RecipeUtil {

    public static ItemStack getResultItem(Recipe<?> recipe) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) {
            throw new NullPointerException("level must not be null.");
        }
        RegistryAccess registryAccess = level.registryAccess();
        return recipe.getResultItem(registryAccess);
    }




    public static Recipe<?> getServerRecipe(RecipeWrapper recipeWrapper, MinecraftServer server){
        var recipeId = recipeWrapper.recipe();
        var type = recipeWrapper.type();
        var recipeManager = server.getRecipeManager();
        
        // 使用byKey直接获取配方
        return getRecipe(recipeId, type, recipeManager);
    }



    public static Recipe<?> getClientRecipe(RecipeWrapper recipeWrapper, Minecraft client){
        var recipeId = recipeWrapper.recipe();
        var type = recipeWrapper.type();

        // 从客户端获取配方管理器
        if (client.level == null) {
            return null;
        }

        RecipeManager recipeManager = client.level.getRecipeManager();

        // 使用byKey直接获取配方
        return getRecipe(recipeId, type, recipeManager);
    }

    private static Recipe<?> getRecipe(ResourceLocation recipeId, RecipeType<?> type, RecipeManager recipeManager) {
        Optional<? extends Recipe<?>> recipeOpt = recipeManager.byKey(recipeId);

        if (recipeOpt.isPresent()) {
            Recipe<?> recipe = recipeOpt.get();
            // 检查配方类型是否匹配
            if (recipe.getType().equals(type)) {
                return recipe;
            }
        }
        return null;
    }

    // ==================== 配方数据处理方法 ====================

    /**
     * 配方显示数据
     * 包含输出和输入物品列表
     */
    public static class RecipeDisplayData {
        /** 输出物品列表 */
        public final List<ItemDisplay> outputs = new ArrayList<>();

        /** 输入物品列表 */
        public final List<ItemDisplay> inputs = new ArrayList<>();
    }

    /**
     * 物品显示数据
     * 包含物品名称和数量
     */
    public static class ItemDisplay {
        /** 物品名称 */
        public final String name;

        /** 配方需要的数量 */
        public int count;

        /** 玩家当前拥有的数量（用于显示 n/m 格式）*/
        public int currentCount = -1;

        /**
         * 构造函数
         * @param name 物品名称
         * @param count 物品数量
         */
        public ItemDisplay(String name, int count) {
            this.name = name;
            this.count = count;
        }

        /**
         * 设置玩家当前拥有的数量
         * @param currentCount 当前数量
         */
        public void setCurrentCount(int currentCount) {
            this.currentCount = currentCount;
        }

        /**
         * 获取显示文本（名称 x 数量）
         * 如果设置了 currentCount，则显示为 "名称 n/m" 格式
         * 当玩家拥有的数量超过需求时，显示上限为需求数量
         * @return 格式化的显示文本
         */
        public String getDisplayText() {
            if (currentCount >= 0) {
                // ✅ 显示 n/m 格式，但 n 不超过 m
                // 如果玩家拥有的数量超过需求，只显示需求数量
                int displayCount = Math.min(currentCount, count);
                return name + " " + displayCount + "/" + count;
            } else if (count > 1) {
                // 显示 x数量 格式
                return name + " x" + count;
            }
            return name;
        }

        /**
         * 检查玩家是否拥有足够的物品
         * @return 如果拥有足够的数量返回 true，否则返回 false
         */
        public boolean hasEnough() {
            return currentCount >= 0 && currentCount >= count;
        }
    }

    /**
     * 处理配方数据，合并相同物品并计算数量
     * 遍历所有槽位构建器，根据物品角色（输入/输出/催化剂）分类，
     * 并将相同名称的物品合并，累加其数量
     *
     * @param builderSlots 槽位构建器映射表，键为槽位索引，值为槽位构建器
     * @return 处理后的配方显示数据
     */
    public static RecipeDisplayData processRecipeData(Map<Integer, SlotBuilder> builderSlots) {
        RecipeDisplayData data = new RecipeDisplayData();

        // 使用 LinkedHashMap 保持物品顺序，同时用于合并相同物品
        Map<String, ItemDisplay> outputMap = new LinkedHashMap<>();
        Map<String, ItemDisplay> inputMap = new LinkedHashMap<>();

        // 遍历所有槽位构建器
        for (SlotBuilder slotBuilder : builderSlots.values()) {
            RecipeIngredientRole role = slotBuilder.getRole();
            List<List<ItemStack>> ingredients = slotBuilder.getIngredients();

            // 跳过空的材料列表
            if (ingredients.isEmpty()) {
                continue;
            }

            // 获取第一个材料选项
            List<ItemStack> firstIngredient = ingredients.get(0);
            if (!firstIngredient.isEmpty()) {
                ItemStack itemStack = firstIngredient.get(0);
                if (!itemStack.isEmpty()) {
                    String itemName = itemStack.getHoverName().getString();
                    int count = itemStack.getCount();

                    // 根据物品角色分类
                    if (role == RecipeIngredientRole.OUTPUT) {
                        mergeItem(outputMap, itemName, count);
                    } else if (role == RecipeIngredientRole.INPUT || role == RecipeIngredientRole.CATALYST) {
                        mergeItem(inputMap, itemName, count);
                    }
                }
            }
        }

        // 将 Map 转换为 List
        data.outputs.addAll(outputMap.values());
        data.inputs.addAll(inputMap.values());

        return data;
    }

    /**
     * 合并相同物品，累加数量
     * 如果物品已存在于映射表中，则累加其数量；
     * 如果是新物品，则创建新的 ItemDisplay 对象并添加到映射表
     *
     * @param map 物品映射表，键为物品名称，值为物品显示对象
     * @param itemName 物品名称
     * @param count 物品数量
     */
    private static void mergeItem(Map<String, ItemDisplay> map, String itemName, int count) {
        if (map.containsKey(itemName)) {
            // 物品已存在，累加数量
            map.get(itemName).count += count;
        } else {
            // 新物品，添加到映射表
            map.put(itemName, new ItemDisplay(itemName, count));
        }
    }
}
