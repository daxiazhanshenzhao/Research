package org.research.api.util;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.util.LazyOptional;
import org.research.api.client.ClientResearchData;
import org.research.api.init.CapInit;
import org.research.api.recipe.capability.IRecipeTransferManager;
import org.research.api.tech.SyncData;
import org.research.api.tech.capability.ITechTreeManager;

public class ResearchAPI {

    public static LazyOptional<ITechTreeManager> getTechTreeData(ServerPlayer serverPlayer) {
        return serverPlayer.getCapability(CapInit.ResearchData);
    }


    public static SyncData getSyncData(ServerPlayer serverPlayer) {
        return getTechTreeData(serverPlayer)
                .map(ITechTreeManager::getSyncData)
                .orElse(ClientResearchData.emptySyncedData);
    }

    public static LazyOptional<IRecipeTransferManager> getRecipeTransferData(ServerPlayer serverPlayer) {
        return serverPlayer.getCapability(CapInit.RecipeTransferData);
    }
}
