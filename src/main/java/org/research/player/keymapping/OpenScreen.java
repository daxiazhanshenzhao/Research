package org.research.player.keymapping;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;
import org.research.api.init.KeyMappingInit;
import org.research.api.provider.ScreenProvider;


@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class OpenScreen extends KeyMapping {




    public OpenScreen() {
        super("key.research.open_research_screen",  InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, KeyMappingInit.RESEARCH_CATEGORY);

    }



    @SubscribeEvent
    public static void click(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            while (KeyMappingInit.OPEN_RESEARCH_SCREEN.get().consumeClick()) {
                ScreenProvider.serverOpenResearchScreen();
            }
        }
    }

}
