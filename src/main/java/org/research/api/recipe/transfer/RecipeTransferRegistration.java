package org.research.api.recipe.transfer;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.HashMap;
import java.util.HashSet;

@Getter
@Setter
public class RecipeTransferRegistration {

    private HashMap<RecipeType<?>,RecipeTransferBuilder<?,?>> builderMap = new HashMap<>();
    private HashMap<RecipeType<?>,RecipeTransferHandler<?,?>> handlerMap = new HashMap<>();

    // HashSet缓存（用于MenuType高频渲染查询，O(1)查找性能）
    private final HashSet<MenuType<?>> menuTypeCache = new HashSet<>();

    public RecipeTransferRegistration() {}


    public <C extends AbstractContainerMenu, R> void addRecipeTransferHandler(RecipeType<?> recipeType,
                                         Class<? extends C> containerClass,MenuType<C> menuType,
                                         int recipeSlotStart, int recipeSlotCount,
                                         int inventorySlotStart, int inventorySlotCount){
        var transferBuilder = new RecipeTransferBuilder<C,R>(containerClass, menuType, recipeSlotStart, recipeSlotCount, inventorySlotStart, inventorySlotCount);
        var handle = new RecipeTransferHandler<>(transferBuilder);

        builderMap.put(recipeType,transferBuilder);
        handlerMap.put(recipeType,handle);
        menuTypeCache.add(menuType);
    }

    public <C extends AbstractContainerMenu, R> void addRecipeTransferHandler(RecipeType<?> recipeType,
                                                                              RecipeTransferBuilder<C,R> builder){
        builderMap.put(recipeType,builder);
        handlerMap.put(recipeType,new RecipeTransferHandler<>(builder));
        menuTypeCache.add(builder.getMenuType());
    }

    public boolean containsMenuType(MenuType<?> menuType) {
        return menuTypeCache.contains(menuType);
    }


}
