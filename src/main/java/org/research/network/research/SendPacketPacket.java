package org.research.network.research;


import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.research.api.init.PacketInit;
import org.research.api.util.ResearchAPI;

import java.util.function.Supplier;

/**
 * 在客户端通知服务端同步数据
 */
public class SendPacketPacket {

    public SendPacketPacket() {

    }

    public SendPacketPacket(FriendlyByteBuf buf){
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

                // 获取玩家的科技树数据并发送给客户端
                ResearchAPI.getTechTreeData(player).ifPresent(techTree -> {
                    var syncData = techTree.getSyncData();
                    PacketInit.sendToPlayer(new ClientboundSyncPlayerData(syncData), player);
                });
            }
        });

        return true;
    }
}
