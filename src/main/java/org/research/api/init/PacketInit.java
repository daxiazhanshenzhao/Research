package org.research.api.init;


import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import org.research.Research;
import org.research.network.research.ClientOpenScreenPaket;
import org.research.network.research.ClientboundSyncPlayerData;
import org.research.network.research.OpenScreenPacket;
import org.research.network.research.SendPacketPacket;

@Mod.EventBusSubscriber
public class PacketInit {

    private static SimpleChannel INSTANCE;
    private static int packetId = 0;
    private static int id() {
        return packetId++;
    }

    public static void register(){
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(Research.asResource("packet"))
                .networkProtocolVersion(()->"1.0")
                .clientAcceptedVersions(s->true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        INSTANCE = net;


        //server -> client
        net.messageBuilder(ClientboundSyncPlayerData.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(ClientboundSyncPlayerData::new)
                .encoder(ClientboundSyncPlayerData::toBytes)
                .consumerMainThread(ClientboundSyncPlayerData::handle)
                .add();


        net.messageBuilder(OpenScreenPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(OpenScreenPacket::new)
                .encoder(OpenScreenPacket::encode)
                .consumerMainThread(OpenScreenPacket::handle)
                .add();

        //client -> server
        net.messageBuilder(ClientOpenScreenPaket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(ClientOpenScreenPaket::new)
                .encoder(ClientOpenScreenPaket::toBytes)
                .consumerMainThread(ClientOpenScreenPaket::handle)
                .add();

        net.messageBuilder(SendPacketPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(SendPacketPacket::new)
                .encoder(SendPacketPacket::toBytes)
                .consumerMainThread(SendPacketPacket::handle)
                .add();
    }


    public static <T> void sendToPlayer(T data, ServerPlayer player) {
        if (INSTANCE != null) {
            INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), data);
        }


    }

    public static <T> void sendToServer(T data) {
        if (INSTANCE != null) {
            INSTANCE.sendToServer(data);
        }

    }
}
