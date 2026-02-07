package org.research.api.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.research.api.gui.ClientScreenManager;
import org.research.api.gui.layer.ClientOverlayManager;
import org.research.api.init.PacketInit;
import org.research.api.inventory.ClientInventoryData;
import org.research.api.recipe.category.CatalystsRegistration;
import org.research.api.recipe.EmptyResearchPlugin;
import org.research.api.recipe.ResearchPlugin;
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

    //实际用于显示的配方
    public static final CatalystsRegistration recipeCategories = new CatalystsRegistration();

    private static ClientOverlayManager overlayManager;

    public static ClientOverlayManager getOverlayManager() {
        if (overlayManager == null) {
            overlayManager = new ClientOverlayManager();
        }
        return overlayManager;
    }

    public static ClientInventoryData clientInventoryData;

    public static ClientInventoryData getClientInventoryData() {
        if (clientInventoryData == null) {
            clientInventoryData = new ClientInventoryData();
        }
        return clientInventoryData;
    }

    private static ClientScreenManager screenManager;

    /**
     * 获取客户端屏幕管理器
     *
     * @return ClientScreenManager 实例（保证不为 null）
     */
    public static ClientScreenManager getScreenManager() {
        if (screenManager == null) {
            screenManager = new ClientScreenManager();
        }
        return screenManager;
    }
}
