package org.research.network.research;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.research.api.client.ClientResearchData;
import org.research.api.tech.SyncData;
import org.research.gui.minecraft.ResearchScreen;

import java.util.function.Supplier;

/*
    服务端发送
 */
public class OpenScreenPacket {

    private SyncData syncData;

    public OpenScreenPacket(SyncData syncData) {
        this.syncData = syncData;
    }

    public OpenScreenPacket(FriendlyByteBuf buf) {
        this.syncData = SyncData.SYNCED_SPELL_DATA.read(buf);
    }

    public void encode(FriendlyByteBuf buf) {
        SyncData.SYNCED_SPELL_DATA.write(buf, syncData);
    }


    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().setPacketHandled(true);

        ctx.get().enqueueWork(() -> {
            // 先更新客户端缓存，确保数据可用
            if (syncData != null) {
                ClientResearchData.playerSyncedDataLookup.put(syncData.getPlayerId(), syncData);
                System.out.println("[OpenScreenPacket] Updated client cache with playerId: " + syncData.getPlayerId() + ", cacheds size: " + syncData.getCacheds().size());
            }

            // 然后打开屏幕
            Minecraft.getInstance().setScreen(new ResearchScreen(syncData));
        });

        return true;
    }
}
