package org.research.api.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.research.api.gui.MouseHandleBgData;
import org.research.api.init.PacketInit;
import org.research.api.recipe.helper.EmptyResearchPlugin;
import org.research.api.recipe.helper.ResearchPlugin;
import org.research.api.tech.SyncData;
import org.research.network.research.SendPacketPacket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class ClientResearchData {


    //========= sync data =============//
    public static final HashMap<Integer, SyncData> playerSyncedDataLookup = new HashMap<>();
    public static final SyncData emptySyncedData = new SyncData(-999);

    public static SyncData getSyncData() {
        LocalPlayer localPlayer = Minecraft.getInstance().player;
        return playerSyncedDataLookup.getOrDefault(localPlayer.getId(),emptySyncedData);
    }

    public static void syncFromServer(){
        PacketInit.sendToServer(new SendPacketPacket());
    }



    //mouseHandleBgData
    public static final HashMap<Integer, MouseHandleBgData> mouseData = new HashMap<>();
    public static final MouseHandleBgData emptyMouseData = MouseHandleBgData.EMPTY;

    public static MouseHandleBgData getMouseData() {
        LocalPlayer localPlayer = Minecraft.getInstance().player;
        return mouseData.getOrDefault(localPlayer.getId(),emptyMouseData);
    }



    //recipeManager
    public static final List<ResearchPlugin> recipePluginData = new ArrayList<>();
    public static final ResearchPlugin emptyRecipeData = new EmptyResearchPlugin();

    public static final List<ResearchPlugin> getRecipePluginData() {
        LocalPlayer localPlayer = Minecraft.getInstance().player;
        if (recipePluginData.isEmpty()) {
            return List.of(emptyRecipeData);
        }else {
            return recipePluginData;
        }

    }

}
