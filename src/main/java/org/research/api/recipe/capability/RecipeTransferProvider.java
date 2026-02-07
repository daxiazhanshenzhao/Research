package org.research.api.recipe.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.antlr.v4.runtime.misc.NotNull;
import org.research.api.init.CapInit;
import org.research.api.tech.capability.ITechTreeManager;
import org.research.api.tech.capability.TechTreeManager;

import javax.annotation.Nullable;

public class RecipeTransferProvider implements ICapabilitySerializable<CompoundTag> {

    private final RecipeTransferManager data;
    private final LazyOptional<IRecipeTransferManager> lazyOptional;

    public RecipeTransferProvider(ServerPlayer player) {
        this.data = new RecipeTransferManager(player);
        this.lazyOptional = LazyOptional.of(() -> data);
    }


    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction direction) {
        return CapInit.RecipeTransferData.orEmpty(capability, lazyOptional);
    }

    @Override
    public CompoundTag serializeNBT() {
        return data.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {

    }

}
