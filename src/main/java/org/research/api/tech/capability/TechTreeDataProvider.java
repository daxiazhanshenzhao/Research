package org.research.api.tech.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.antlr.v4.runtime.misc.NotNull;
import org.research.api.init.CapInit;
import org.research.api.tech.PlayerTechTreeData;

import javax.annotation.Nullable;

public class TechTreeDataProvider implements ICapabilitySerializable<CompoundTag> {

    private final ITechTreeCapability techTreeData;
    private final LazyOptional<ITechTreeCapability> lazyOptional;

    //techInstance
    public static final String ID = "tech_id";
    public static final String STATE = "tech_state";

    //techTree
    public static final String





    public TechTreeDataProvider(ServerPlayer player) {
        this.techTreeData = new PlayerTechTreeData(player);
        this.lazyOptional = LazyOptional.of(() -> techTreeData);
    }


    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction direction) {
        return CapInit.BODY_DATA.orEmpty(capability, lazyOptional);
    }

    @Override
    public CompoundTag serializeNBT() {
        return null;
    }

    @Override
    public void deserializeNBT(CompoundTag compoundTag) {

    }
}
