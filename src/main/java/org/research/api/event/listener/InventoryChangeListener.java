package org.research.api.event.listener;


import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import org.research.api.event.custom.InventoryChangeEvent;
import org.research.api.init.PacketInit;
import org.research.network.InventoryChangePaket;


public record InventoryChangeListener(Player player) implements ContainerListener {


    @Override
    public void slotChanged(AbstractContainerMenu containerToSend, int dataSlotIndex, ItemStack stack) {
        if (!stack.isEmpty() && containerToSend.getSlot(dataSlotIndex).container == player.getInventory()) {
            MinecraftForge.EVENT_BUS.post(new InventoryChangeEvent(player, stack, dataSlotIndex));

            // ✅ 如果是服务端玩家，发包到客户端
            if (player instanceof ServerPlayer serverPlayer) {
                InventoryChangePaket packet = new InventoryChangePaket(stack, dataSlotIndex);
                PacketInit.sendToPlayer(packet, serverPlayer);
            }
        }
    }

    @Override
    public void dataChanged(AbstractContainerMenu containerMenu, int dataSlotIndex, int value) {

    }
}
