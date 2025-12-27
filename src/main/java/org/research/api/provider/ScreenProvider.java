package org.research.api.provider;


import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.PacketDistributor;
import org.research.api.init.CapInit;
import org.research.api.init.PacketInit;
import org.research.api.tech.SyncData;
import org.research.gui.ResearchScreen;
import org.research.network.OpenScreenPacket;
import org.research.player.inventory.ResearchContainer;



public class ScreenProvider {


    

    @OnlyIn(Dist.CLIENT)
    public static void OpenResearchScreen(SyncData syncData) {



    }

    @OnlyIn(Dist.DEDICATED_SERVER)
    public static void openResearchScreen(SyncData syncData) {

    }




}
