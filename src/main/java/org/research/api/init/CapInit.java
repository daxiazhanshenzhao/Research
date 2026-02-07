package org.research.api.init;


import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.research.Research;
import org.research.api.recipe.capability.IRecipeTransferManager;
import org.research.api.recipe.capability.RecipeTransferProvider;
import org.research.api.tech.capability.ITechTreeManager;
import org.research.api.tech.capability.TechTreeDataProvider;

@Mod.EventBusSubscriber
public class CapInit {

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.register(ITechTreeManager.class);
    }

    @SubscribeEvent
    public static void attachCap(AttachCapabilitiesEvent<Entity> event){

        if(event.getObject() instanceof ServerPlayer player){
            event.addCapability(
                    Research.asResource("tech_tree"),
                    new TechTreeDataProvider(player)
            );

            event.addCapability(
                    Research.asResource("recipe_transfer"),
                    new RecipeTransferProvider(player)
            );


        }else {
            
        }

    }

    public static final Capability<ITechTreeManager> ResearchData;
    public static final Capability<IRecipeTransferManager> RecipeTransferData;

    static {
        ResearchData = CapabilityManager.get(new CapabilityToken<>() {});
        RecipeTransferData = CapabilityManager.get(new CapabilityToken<>() {});
    }
}
