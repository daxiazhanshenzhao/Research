package org.research.player.keymapping;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;
import org.research.api.init.KeyMappingInit;
import org.research.api.provider.ScreenProvider;


@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class OpenScreen extends KeyMapping {

    private final Screen screen;


    public OpenScreen(Screen screen) {
        super("key.research.open_research_screen",  InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, KeyMappingInit.RESEARCH_CATEGORY);
        this.screen = screen;
    }

    public Screen getScreen() {
        return screen;
    }
    

    @SubscribeEvent
    public static void click(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            while (KeyMappingInit.OPEN_RESEARCH_SCREEN.get().consumeClick()) {

            }
        }
    }

}
