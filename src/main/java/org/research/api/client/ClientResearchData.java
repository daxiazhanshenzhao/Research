package org.research.api.client;

import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.research.api.gui.ClientScreenManager;
import org.research.api.gui.MouseHandleBgData;
import org.research.api.init.PacketInit;
import org.research.api.recipe.category.CatalystsRegistration;
import org.research.api.recipe.helper.EmptyResearchPlugin;
import org.research.api.recipe.helper.ResearchPlugin;
import org.research.api.tech.SyncData;
import org.research.network.research.SendPacketPacket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;


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

    //GUI============================

    //mouseHandleBgData
    public static final HashMap<Integer, MouseHandleBgData> mouseData = new HashMap<>();
    public static final MouseHandleBgData emptyMouseData = MouseHandleBgData.EMPTY;

    public static MouseHandleBgData getMouseData() {
        LocalPlayer localPlayer = Minecraft.getInstance().player;
        return mouseData.getOrDefault(localPlayer.getId(),emptyMouseData);
    }
    //tech slots cache


    //recipeManager================================
    public static final List<ResearchPlugin> recipePluginData = new ArrayList<>();
    public static final ResearchPlugin emptyRecipeData = new EmptyResearchPlugin();

    public static final List<ResearchPlugin> getRecipePluginData() {
        if (recipePluginData.isEmpty()) {
            return List.of(emptyRecipeData);
        }else {
            return recipePluginData;
        }
    }

    @Getter
    public static final CatalystsRegistration recipeCategories = new CatalystsRegistration();

    private static ClientScreenManager manager;

    public static Optional<ClientScreenManager> getManager() {
        if (manager == null) {
            try {
                manager = new ClientScreenManager();
            } catch (Exception e) {
                e.printStackTrace();
                return Optional.empty();
            }
        }
        return Optional.of(manager);
    }
}
