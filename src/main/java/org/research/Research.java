package org.research;

import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.research.api.init.TechInit;
import org.slf4j.Logger;

import java.util.Locale;


@Mod(Research.MODID)
public class Research {

    public static final String MODID = "research";

    public static final Logger LOGGER = LogUtils.getLogger();

    /**
     *
     * @param path 不能有大写字母
     * @return
     */
    public static ResourceLocation asResource(String path){
        return ResourceLocation.fromNamespaceAndPath(MODID,path.toLowerCase());
    }

    public Research(){

        IEventBus modEventBus = FMLJavaModLoadingContext
                .get().getModEventBus();

        TechInit.register(modEventBus);

        modEventBus.addListener(this::setupClient);
        
    }
    private void setupClient(FMLClientSetupEvent evt) {
        evt.enqueueWork(() -> {

        });
    }

}
