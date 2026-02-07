package org.research.api.gui;


import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import org.research.api.init.PacketInit;
import org.research.gui.minecraft.ResearchScreenV2;
import org.research.network.gui.OpenScreenPacket;


public class ScreenProvider {




    public static void serverOpenResearchScreen(ServerPlayer player) {
        PacketInit.sendToPlayer(new OpenScreenPacket(),player);
    }
    public static void clientOpenResearchScreen() {
        Minecraft.getInstance().setScreen(new ResearchScreenV2());
    }




}
