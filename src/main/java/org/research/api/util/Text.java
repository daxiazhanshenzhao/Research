package org.research.api.util;


import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.research.Research;
import org.research.api.init.TechInit;
import org.research.api.tech.PlayerTechTreeData;

@Mod.EventBusSubscriber
public class Text {
    @SubscribeEvent
    public static void rightClick(PlayerInteractEvent.RightClickItem event) {
        Item item = event.getItemStack().getItem();
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            ResearchApi.getTechTreeData(serverPlayer).ifPresent(techTree -> {
                if (item.equals(Items.STICK) ) {
                    Research.LOGGER.info(techTree.serializeNBT().toString());
                }
                if (item.equals(Items.IRON_INGOT)) {
                    Research.LOGGER.info(techTree.getCacheds().toString());
                    Research.LOGGER.info(techTree.getSyncData().getCacheds().toString());

                }

                if (item.equals(Items.GOLD_INGOT)) {

                }

            });

        }







    }
}
