package org.research.api.init;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.research.Research;
import org.research.gui.minecraft.ResearchOverlay;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD, modid = Research.MODID)
public class OverlayInit {

    @SubscribeEvent
    public static void onRegisterOverlays(RegisterGuiOverlaysEvent event) {
        // 注册在 CHAT_PANEL 之上，确保优先级高于大多数Screen和GUI元素
        // 这样可以确保研究覆盖层始终显示在最前面
        event.registerAbove(VanillaGuiOverlay.CHAT_PANEL.id(), "research_overlay", ResearchOverlay.instance);
    }

}
