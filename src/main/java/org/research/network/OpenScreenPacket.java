package org.research.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;
import org.research.api.init.CapInit;
import org.research.api.tech.SyncData;
import org.research.gui.ResearchScreen;

import java.util.function.Supplier;

/*
    服务端发送
 */
public class OpenScreenPacket {

    private SyncData syncData;

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
