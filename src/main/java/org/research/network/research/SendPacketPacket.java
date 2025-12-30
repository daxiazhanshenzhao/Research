package org.research.network.research;


import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.research.api.init.PacketInit;
import org.research.api.util.ResearchApi;

import java.util.function.Supplier;

/**
 * 在客户端通知服务端同步数据
 */
public class SendPacketPacket {

    public SendPacketPacket() {

    }

    public SendPacketPacket(FriendlyByteBuf buf){
    }

    public void toBytes(FriendlyByteBuf buf){
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier){
        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() -> {
            if (context.getSender() != null) {
                var player = context.getSender();
                PacketInit.sendToPlayer(new ClientboundSyncPlayerData(), player);
            }




        });
        return true;
    }
}
