package org.research.api.event.handle;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.research.api.client.ClientResearchData;
import org.research.api.event.custom.InventoryChangeEvent;
import org.research.api.util.ResearchAPI;

@Mod.EventBusSubscriber
public class PlayerEventHandle {



    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {

        //ResearchData
        if (event.player instanceof ServerPlayer serverPlayer) {
            ResearchAPI.getTechTreeData(serverPlayer).ifPresent(techTree -> {
                techTree.tick(serverPlayer,serverPlayer.tickCount);
            });
        }


    }

    @SubscribeEvent
    public static void inventoryChange(InventoryChangeEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            ResearchAPI.getTechTreeData(serverPlayer).ifPresent(techTree -> {
                techTree.tryComplete(event.getItem());
            });
        }

        if (event.getEntity() instanceof LocalPlayer player) {
            ClientResearchData.getClientInventoryData().updateInventoryData();
            ClientResearchData.getOverlayManager().clearCache();

        }
    }



}
