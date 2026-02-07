package org.research.api.recipe.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.Slot;

import java.util.List;

public interface IRecipeTransferManager {

    void setItems(

    );

    void deserializeNBT(CompoundTag compoundTag);
    CompoundTag serializeNBT();
}
