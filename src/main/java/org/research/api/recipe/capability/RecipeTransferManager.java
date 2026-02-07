package org.research.api.recipe.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class RecipeTransferManager implements IRecipeTransferManager {

    public ServerPlayer player;
    public AbstractContainerMenu container;

    public  RecipeTransferManager(ServerPlayer player) {
        this.player = player;
    }

    @Override
    public void setItems() {

    }

    @Override
    public void deserializeNBT(CompoundTag compoundTag) {

    }

    @Override
    public CompoundTag serializeNBT() {
        return new CompoundTag();
    }


}
