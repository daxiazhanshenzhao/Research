package org.research.api.provider;


import net.minecraft.server.level.ServerPlayer;
import org.research.api.init.PacketInit;
import org.research.api.util.ResearchApi;
import org.research.network.research.ClientOpenScreenPaket;
import org.research.network.research.OpenScreenPacket;


public class ScreenProvider {





    public static void serverOpenResearchScreen() {
        PacketInit.sendToServer(new ClientOpenScreenPaket());
    }


    public static void serverOpenResearchScreen(ServerPlayer player) {


        ResearchApi.getTechTreeData(player).ifPresent(techTree -> {
            PacketInit.sendToPlayer(new OpenScreenPacket(techTree.getSyncData()),player);
        });


    }




}
