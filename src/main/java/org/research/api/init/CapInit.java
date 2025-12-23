package org.research.api.init;


import dev.architectury.event.events.common.TickEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.research.Research;
import org.research.api.tech.capability.ITechTreeCapability;
import org.research.api.tech.capability.TechTreeDataProvider;

@Mod.EventBusSubscriber
public class CapInit {

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.register(ITechTreeCapability.class);
    }

    @SubscribeEvent
    public static void attachCap(AttachCapabilitiesEvent<Entity> event){

        if(event.getObject() instanceof ServerPlayer player){
            event.addCapability(
                    Research.asResource("tech_tree"),
                    new TechTreeDataProvider(player));
        }
    }

    public static final Capability<ITechTreeCapability> BODY_DATA;


    static {
        BODY_DATA = CapabilityManager.get(new CapabilityToken<>() {});
    }
}
