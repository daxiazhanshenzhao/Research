package org.research.api.init;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;
import org.research.Research;
import org.research.api.tech.AbstractTech;
import org.research.api.tech.EmptyTech;
import org.research.tech.IronTech;
import org.research.tech.RedStoneTech;

import java.util.List;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = Research.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class TechInit {

    public static final ResourceKey<Registry<AbstractTech>> TECH_REGISTRY_KEY = 
        ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(Research.MODID, "tech"));
    
    private static final DeferredRegister<AbstractTech> TECHS = 
        DeferredRegister.create(TECH_REGISTRY_KEY, Research.MODID);
    
    public static Supplier<IForgeRegistry<AbstractTech>> REGISTRY;

    public static final AbstractTech EMPTY = new EmptyTech();

    public static void register(IEventBus eventBus) {
        TECHS.register(eventBus);
    }

    @SubscribeEvent
    public static void registerRegistry(NewRegistryEvent event) {
        REGISTRY = event.create(
            new RegistryBuilder<AbstractTech>()
                .setName(TECH_REGISTRY_KEY.location())
                .disableSaving()
                .disableOverrides()
        );
    }

    private static RegistryObject<AbstractTech> registerTech(AbstractTech tech) {
        String id = tech.getId(); // 使用路径部分，而不是完整的ResourceLocation字符串
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Tech ID cannot be null or empty");
        }
        return TECHS.register(id, () -> tech);
    }

    public static List<AbstractTech> getAllTech(){
        return TechInit.REGISTRY.get()
                .getValues()
                .stream()
                .toList();
    }
    public static AbstractTech getTech(ResourceLocation id){
        var tech = REGISTRY.get().getValue(id);
        if (tech == null) {
            return EMPTY;
        }
        return tech;
    }

    public static RegistryObject<AbstractTech> FIRST_TECH = registerTech(EMPTY);
    public static RegistryObject<AbstractTech> APP_TECH = registerTech(new RedStoneTech());
    public static RegistryObject<AbstractTech> IRON_TECH = registerTech(new IronTech());

}
