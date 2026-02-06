package org.research.api.recipe.transfer;

import mezz.jei.api.recipe.RecipeType;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

import java.util.HashMap;

public class RecipeTransferRegistration<C extends AbstractContainerMenu, R> {

    private HashMap<RecipeType<?>,RecipeTransferBuilder<C,R>> transferMap = new HashMap<>();
    private HashMap<RecipeType<?>,RecipeTransferHandler<C,R>> handlerMap = new HashMap<>();


    private  RecipeTransferRegistration() {}


    /**
     * 连续性配方
     * @param containerClass
     * @param menuType
     * @param recipeType
     * @param recipeSlotStart
     * @param recipeSlotCount
     * @param inventorySlotStart
     * @param inventorySlotCount
     * @param <C>
     * @param <R>
     */
    public void addRecipeTransferHandler(RecipeType<R> recipeType,
                                                                              Class<? extends C> containerClass,MenuType<C> menuType,
                                                                              int recipeSlotStart, int recipeSlotCount,
                                                                              int inventorySlotStart, int inventorySlotCount){
        var transferBuilder = new RecipeTransferBuilder<C,R>(containerClass, menuType, recipeSlotStart, recipeSlotCount, inventorySlotStart, inventorySlotCount);
        var handle = new RecipeTransferHandler<C,R>(transferBuilder);

        transferMap.put(recipeType,transferBuilder);
        handlerMap.put(recipeType,handle);
    }

    /**
     * 非连续性配方
     * @param recipeType
     * @param builder
     */
    public void addRecipeTransferHandler(RecipeType<R> recipeType,
                                                                              RecipeTransferBuilder<C,R> builder){
        transferMap.put(recipeType,builder);
    }

}
