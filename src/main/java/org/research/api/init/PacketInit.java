package org.research.api.init;


import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import org.research.Research;
import org.research.api.util.ResearchApi;
import org.research.network.ClientboundSyncPlayerData;
import org.research.network.OpenScreenPacket;

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
    }


    public static <T> void sendToPlayer(T data, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), data);
    }
}
