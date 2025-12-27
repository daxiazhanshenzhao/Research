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

public class OpenScreenPacket {

    private SyncData syncData;

    public OpenScreenPacket(SyncData syncData) {
        this.syncData = syncData;
    }

    public static void encode(OpenScreenPacket msg, FriendlyByteBuf buf) {
        SyncData.SYNCED_SPELL_DATA.write(buf, msg.syncData);
    }

    public static OpenScreenPacket decode(FriendlyByteBuf buf) {
        return new OpenScreenPacket(SyncData.SYNCED_SPELL_DATA.read(buf));
    }

    public static void handle(OpenScreenPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sender = ctx.get().getSender();
            if (sender != null) {
                sender.getCapability(CapInit.ResearchData).ifPresent(researchData -> {
                    var data = researchData.getSyncData();
                    Minecraft.getInstance().setScreen(new ResearchScreen(msg.syncData));
                });

            }
        });
        ctx.get().setPacketHandled(true);
    }
}
