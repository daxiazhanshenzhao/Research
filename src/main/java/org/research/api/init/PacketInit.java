package org.research.api.init;


import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import org.research.Research;
import org.research.network.InventoryChangePaket;
import org.research.network.gui.OpenScreenPacket;
import org.research.network.research.*;

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

        net.messageBuilder(PlayTechSoundPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(PlayTechSoundPacket::new)
                .encoder(PlayTechSoundPacket::toBytes)
                .consumerMainThread(PlayTechSoundPacket::handle)
                .add();

        // ✅ 注册背包变化包（服务端 -> 客户端）
        net.messageBuilder(InventoryChangePaket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(InventoryChangePaket::decode)
                .encoder(InventoryChangePaket::encode)
                .consumerMainThread(InventoryChangePaket::handle)
                .add();

        //client -> server
        net.messageBuilder(SendPacketPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(SendPacketPacket::new)
                .encoder(SendPacketPacket::toBytes)
                .consumerMainThread(SendPacketPacket::handle)
                .add();
        net.messageBuilder(ClientSetFocusPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(ClientSetFocusPacket::new)
                .encoder(ClientSetFocusPacket::toBytes)
                .consumerMainThread(ClientSetFocusPacket::handle)
                .add();
        net.messageBuilder(ClientClearFocusPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(ClientClearFocusPacket::new)
                .encoder(ClientClearFocusPacket::toBytes)
                .consumerMainThread(ClientClearFocusPacket::handle)
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
