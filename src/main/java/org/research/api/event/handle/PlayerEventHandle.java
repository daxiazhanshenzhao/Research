package org.research.api.event.handle;


import com.alessandro.astages.event.custom.PlayerInventoryChangedEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.relauncher.Side;
import org.research.api.init.CapInit;
import org.research.api.tech.capability.ITechTreeCapability;
import org.research.api.util.ResearchApi;
import org.research.player.inventory.IResearchContainer;

@Mod.EventBusSubscriber
public class PlayerEventHandle {



    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {

        //ResearchData
        if (event.player instanceof ServerPlayer serverPlayer) {
            ResearchApi.getTechTreeData(serverPlayer).ifPresent(techTree -> {
                techTree.tick(serverPlayer,serverPlayer.tickCount);
            });
        }


    }

    @SubscribeEvent
    public static void inventoryChange(PlayerInventoryChangedEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            ResearchApi.getTechTreeData(serverPlayer).ifPresent(techTree -> {
                techTree.tryComplete(event.getItem());
            });
        }

    }

}
