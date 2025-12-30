package org.research.network.research;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.research.api.init.PacketInit;
import org.research.api.util.ResearchApi;

import java.util.function.Supplier;

/**
 * 客户端向服务端发送
 */
public class ClientOpenScreenPaket {


    public ClientOpenScreenPaket() {

    }

    public ClientOpenScreenPaket(FriendlyByteBuf buf){
    }

    public void toBytes(FriendlyByteBuf buf){
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier){
        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() -> {
            if (context.getSender() != null) {
                var player = context.getSender();

                ResearchApi.getTechTreeData(player).ifPresent(techTree -> {
                    var syncData = techTree.getSyncData();
                    PacketInit.sendToPlayer(new OpenScreenPacket(syncData), player);
                });
            }




        });
        return true;
    }

}
