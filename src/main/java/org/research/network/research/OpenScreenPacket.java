package org.research.network.research;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.research.api.tech.SyncData;
import org.research.gui.ResearchScreen;

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
        ctx.get().enqueueWork(() -> {
            Minecraft.getInstance().setScreen(new ResearchScreen(syncData));
        });
        return true;
    }
}
