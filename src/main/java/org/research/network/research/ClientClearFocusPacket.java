package org.research.network.research;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.research.api.tech.capability.ITechTreeCapability;
import org.research.api.util.ResearchAPI;

import java.util.function.Supplier;

public class ClientClearFocusPacket {
    public ClientClearFocusPacket() {

    }

    public ClientClearFocusPacket(FriendlyByteBuf buf){
    }

    public void toBytes(FriendlyByteBuf buf){

    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier){
        NetworkEvent.Context context = supplier.get();
        context.setPacketHandled(true);

        context.enqueueWork(() -> {
            if (context.getSender() != null) {
                var player = context.getSender();
                ResearchAPI.getTechTreeData(player).ifPresent(ITechTreeCapability::clearFocus);
            }
        });
        return true;
    }
}
