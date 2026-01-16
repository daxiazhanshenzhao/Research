package org.research.api.event.handle;


import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.research.api.event.listener.InventoryChangeListener;

@Mod.EventBusSubscriber
public class ModEventHandle {


    @SubscribeEvent
    public static void onPlayerLoadIn(PlayerEvent.PlayerLoggedInEvent event) {
        var player = event.getEntity();
        player.inventoryMenu.addSlotListener(new InventoryChangeListener(player));
    }
}
