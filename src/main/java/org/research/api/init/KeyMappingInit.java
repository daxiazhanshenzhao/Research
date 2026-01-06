package org.research.api.init;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;
import org.research.Research;
import org.research.api.provider.ScreenProvider;
import org.research.player.keymapping.OpenScreen;


@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Research.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class KeyMappingInit {

    public static final String RESEARCH_CATEGORY = "key.categories.research";
    public static final Lazy<KeyMapping> OPEN_RESEARCH_SCREEN;


    static {
        OPEN_RESEARCH_SCREEN = Lazy.of(OpenScreen::new);
    }


    @SubscribeEvent
    public static void registerBindings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_RESEARCH_SCREEN.get());
    }



}
