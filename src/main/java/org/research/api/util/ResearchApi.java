package org.research.api.util;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.common.util.LazyOptional;
import org.research.api.init.CapInit;
import org.research.api.tech.capability.ITechTreeCapability;

public class ResearchApi {

    public static LazyOptional<ITechTreeCapability> getTechTreeData(ServerPlayer serverPlayer) {

        return serverPlayer.getCapability(CapInit.ResearchData);
    }



}
