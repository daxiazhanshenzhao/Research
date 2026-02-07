package org.research.api.event.handle;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.research.Research;
import org.research.api.client.ClientResearchData;
import org.research.gui.minecraft.ResearchOverlay;

@Mod.EventBusSubscriber(value = Dist.CLIENT,modid = Research.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientEventHandle {

    @SubscribeEvent
    public static void screenActive(ScreenEvent.Render.Post event){
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> screen)) {
            return;
        }
        try {
            MenuType<?> menuType = screen.getMenu().getType();
            if (ClientResearchData.transferData.containsMenuType(menuType)) {
                //超高性能，绝对不卡，卡我吃
                ResearchOverlay.instance.render(event.getGuiGraphics());
            }
        } catch (UnsupportedOperationException e) {

        }
    }

    @SubscribeEvent
    public static void clientMouseEvent(ScreenEvent.MouseDragged.Pre event){

    }
    @SubscribeEvent
    public static void clientMouseEvent(ScreenEvent.MouseScrolled.Pre event){

    }

}
