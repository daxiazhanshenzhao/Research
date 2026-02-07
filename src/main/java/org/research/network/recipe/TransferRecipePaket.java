package org.research.network.recipe;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.network.NetworkEvent;
import org.research.api.recipe.transfer.TransferOperation;

import java.util.List;
import java.util.function.Supplier;

public class TransferRecipePaket {
    public TransferRecipePaket(
            List<TransferOperation> transferOperations,
            List<Slot> craftingSlots,
            List<Slot> inventorySlots,
            boolean maxTransfer,
            boolean requireCompleteSets

    ) {

    }

    public TransferRecipePaket(FriendlyByteBuf buf){
        // 空包，不需要读取数据
    }

    public void toBytes(FriendlyByteBuf buf){
        // 空包，不需要写入数据
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier){
        NetworkEvent.Context context = supplier.get();
        context.setPacketHandled(true);

        context.enqueueWork(() -> {
            if (context.getSender() != null) {
                var player = context.getSender();

            }
        });

        return true;
    }
}
