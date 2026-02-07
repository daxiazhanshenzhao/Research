package org.research.api.recipe.transfer;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

/**
 * 每次从物品栏向配方栏传输时的操作记录
 * @param inventorySlot
 * @param craftingSlot
 */
public record TransferOperation(Slot inventorySlot, Slot craftingSlot) {
    public static TransferOperation readPacketData(FriendlyByteBuf buf, AbstractContainerMenu container) {
        int inventorySlotIndex = buf.readVarInt();
        int craftingSlotIndex = buf.readVarInt();

        Slot inventorySlot = container.getSlot(inventorySlotIndex);
        Slot craftingSlot = container.getSlot(craftingSlotIndex);
        return new TransferOperation(inventorySlot, craftingSlot);
    }

    public void writePacketData(FriendlyByteBuf buf) {
        buf.writeVarInt(inventorySlot.index);
        buf.writeVarInt(craftingSlot.index);
    }
}