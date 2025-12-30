package org.research.api.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.research.api.init.PacketInit;
import org.research.api.tech.SyncData;
import org.research.network.research.SendPacketPacket;

import java.util.HashMap;
import java.util.Locale;


public class ClientResearchData {

    public static final HashMap<Integer, SyncData> playerSyncedDataLookup = new HashMap<>();
    public static final SyncData emptySyncedData = new SyncData(-999);

    public static SyncData getSyncData() {
        LocalPlayer localPlayer = Minecraft.getInstance().player;
        return playerSyncedDataLookup.getOrDefault(localPlayer.getId(),emptySyncedData);
    }

    public static void syncFromServer(){
        PacketInit.sendToServer(new SendPacketPacket());
    }


}
