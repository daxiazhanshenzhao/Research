package org.research.api.provider;


import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.research.gui.ResearchScreen;

public class ScreenProvider {



    public static Screen getResearchScreen() {
        return new ResearchScreen();
    }

    public static void openResearchScreen() {
        Minecraft.getInstance().setScreen(getResearchScreen());
    }



}
