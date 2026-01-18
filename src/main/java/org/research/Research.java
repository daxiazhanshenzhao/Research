package org.research;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.research.api.init.ItemInit;
import org.research.api.init.PacketInit;
import org.research.api.init.TechInit;
import org.slf4j.Logger;


@Mod(Research.MODID)
public class Research {

    public static final String MODID = "research";
    public static final String LDLIB = "ldlib";
    public static final Logger LOGGER = LogUtils.getLogger();

    /**
     * 创建资源位置
     * @param path 路径（不能有大写字母）
     * @return 资源位置对象
     */
    public static ResourceLocation asResource(String path){
        return ResourceLocation.fromNamespaceAndPath(MODID,path.toLowerCase());
    }

    public Research(){

        IEventBus modEventBus = FMLJavaModLoadingContext
                .get().getModEventBus();

        ItemInit.register(modEventBus);
        TechInit.register(modEventBus);

        modEventBus.addListener(this::setupClient);
        modEventBus.addListener(this::commonSetup);
        
    }
    private void setupClient(FMLClientSetupEvent evt) {
        evt.enqueueWork(() -> {
            // 触发配方 GUI 管理器注册事件
            IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
            modEventBus.post(new RegisterRecipeGUIEvent());
        });
    }

    private void commonSetup(FMLCommonSetupEvent evt) {
        evt.enqueueWork(PacketInit::register);
    }

}
