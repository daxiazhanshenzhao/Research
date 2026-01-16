package org.research.api.event.handle;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.research.api.event.custom.InventoryChangeEvent;
import org.research.api.util.ResearchApi;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
    public static void inventoryChange(InventoryChangeEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            ResearchApi.getTechTreeData(serverPlayer).ifPresent(techTree -> {
                techTree.tryComplete(event.getItem());
            });
        }

    }



}
