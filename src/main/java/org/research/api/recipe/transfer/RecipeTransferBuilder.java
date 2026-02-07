package org.research.api.recipe.transfer;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class RecipeTransferBuilder<C extends AbstractContainerMenu, R> {

    private final Class<? extends AbstractContainerMenu> containerClass;
    private final MenuType<? extends AbstractContainerMenu> menuType;
    private final int recipeSlotStart;
    private final int recipeSlotCount;
    private final int inventorySlotStart;
    private final int inventorySlotCount;

    public <C extends AbstractContainerMenu> RecipeTransferBuilder(Class<? extends C> containerClass, MenuType<C> menuType, int recipeSlotStart, int recipeSlotCount, int inventorySlotStart, int inventorySlotCount) {
        this.containerClass = containerClass;
        this.menuType = menuType;
        this.recipeSlotStart = recipeSlotStart;
        this.recipeSlotCount = recipeSlotCount;
        this.inventorySlotStart = inventorySlotStart;
        this.inventorySlotCount = inventorySlotCount;
    }

    public boolean canHandle(C container, R recipe) {
        return true;
    }

    public boolean requireCompleteSets(C container, R recipe) {
        return true;
    }


    public List<Slot> getRecipeSlots(C container, R recipe) {
        List<Slot> slots = new ArrayList<>();
        for (int i = recipeSlotStart; i < recipeSlotStart + recipeSlotCount; i++) {
            Slot slot = container.getSlot(i);
            slots.add(slot);
        }
        return slots;
    }

    public List<Slot> getInventorySlots(C container, R recipe) {
        List<Slot> slots = new ArrayList<>();
        for (int i = inventorySlotStart; i < inventorySlotStart + inventorySlotCount; i++) {
            Slot slot = container.getSlot(i);
            slots.add(slot);
        }
        return slots;
    }
}
