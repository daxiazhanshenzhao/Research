package org.research.network.research;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.research.api.tech.SyncData;
import org.research.gui.minecraft.ResearchScreenV2;

import java.util.function.Supplier;

/*
    服务端发送
 */
public class OpenScreenPacket {


    public OpenScreenPacket() {

    }

    public OpenScreenPacket(FriendlyByteBuf buf) {

    }

    public void encode(FriendlyByteBuf buf) {
    }


    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().setPacketHandled(true);

        ctx.get().enqueueWork(() -> {
            // 然后打开屏幕
            Minecraft.getInstance().setScreen(new ResearchScreenV2());
        });

        return true;
    }
}
