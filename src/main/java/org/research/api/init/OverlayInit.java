package org.research.api.init;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.research.Research;
import org.research.api.provider.ScreenProvider;
import org.research.gui.minecraft.ResearchOverlay;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD, modid = Research.MODID)
public class OverlayInit {
    @SubscribeEvent
    public static void onRegisterOverlays(RegisterGuiOverlaysEvent event) {

        event.registerAbove(VanillaGuiOverlay.EXPERIENCE_BAR.id(), "spell_selection_new", ResearchOverlay.instance);
    }

}
