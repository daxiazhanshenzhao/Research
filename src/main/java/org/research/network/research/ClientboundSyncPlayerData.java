package org.research.network.research;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.research.api.client.ClientResearchData;
import org.research.api.tech.SyncData;

import java.util.function.Supplier;

//将服务端数据给客户端
public class ClientboundSyncPlayerData {

    SyncData syncData;

    public ClientboundSyncPlayerData(SyncData syncData) {
        this.syncData = syncData;
    }

    public ClientboundSyncPlayerData(FriendlyByteBuf buf){
        syncData = SyncData.SYNCED_SPELL_DATA.read(buf);
    }

    public void toBytes(FriendlyByteBuf buf){
        SyncData.SYNCED_SPELL_DATA.write(buf, syncData);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier){
        NetworkEvent.Context context = supplier.get();
        context.setPacketHandled(true);

        context.enqueueWork(() -> {
            if (syncData != null) {
                ClientResearchData.playerSyncedDataLookup.put(syncData.getPlayerId(), syncData);
            }
        });

        return true;
    }

}
