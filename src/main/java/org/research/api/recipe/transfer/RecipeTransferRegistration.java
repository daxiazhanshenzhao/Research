package org.research.api.recipe.transfer;

import lombok.Getter;
import lombok.Setter;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

import java.util.HashMap;

@Getter
@Setter
public class RecipeTransferRegistration {

    private HashMap<RecipeType<?>,RecipeTransferBuilder<?,?>> builderMap = new HashMap<>();
    private HashMap<RecipeType<?>,RecipeTransferHandler<?,?>> handlerMap = new HashMap<>();


    private  RecipeTransferRegistration() {}


    /**
     * 连续性配方
     * @param recipeType 配方类型
     * @param containerClass 容器类
     * @param menuType 菜单类型
     * @param recipeSlotStart 配方槽起始位置
     * @param recipeSlotCount 配方槽数量
     * @param inventorySlotStart 物品栏起始位置
     * @param inventorySlotCount 物品栏数量
     * @param <C> 容器类型
     * @param <R> 配方类型
     */
    public <C extends AbstractContainerMenu, R> void addRecipeTransferHandler(RecipeType<R> recipeType,
                                         Class<? extends C> containerClass,MenuType<C> menuType,
                                         int recipeSlotStart, int recipeSlotCount,
                                         int inventorySlotStart, int inventorySlotCount){
        var transferBuilder = new RecipeTransferBuilder<C,R>(containerClass, menuType, recipeSlotStart, recipeSlotCount, inventorySlotStart, inventorySlotCount);
        var handle = new RecipeTransferHandler<>(transferBuilder);

        builderMap.put(recipeType,transferBuilder);
        handlerMap.put(recipeType,handle);
    }

    /**
     * 非连续性配方
     * @param recipeType 配方类型
     * @param builder 配方传输构建器
     * @param <C> 容器类型
     * @param <R> 配方类型
     */
    public <C extends AbstractContainerMenu, R> void addRecipeTransferHandler(RecipeType<R> recipeType,
                                                                              RecipeTransferBuilder<C,R> builder){
        builderMap.put(recipeType,builder);
        handlerMap.put(recipeType,new RecipeTransferHandler<>(builder));
    }

}
