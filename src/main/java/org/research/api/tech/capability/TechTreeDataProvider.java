package org.research.api.tech.capability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.antlr.v4.runtime.misc.NotNull;
import org.research.Research;
import org.research.api.init.CapInit;
import org.research.api.tech.PlayerTechTreeData;
import org.research.api.tech.TechInstance;
import org.research.api.util.ResearchApi;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber
public class TechTreeDataProvider implements ICapabilitySerializable<CompoundTag> {


    private LazyOptional<ITechTreeCapability> lazyOptional;

    //techData
    public static final String TREE_DATA = Research.MODID + "tree_data";

    //techInstance
    public static final String ID = "tech_id";
    public static final String STATE = "tech_state";
    public static final String FOCUS = "tech_focus";

    //techTree
    public static final String CACHEDs = "tech_cached_map";
    public static final String VEC = "tech_vec_map";
    public static final String STAGE = "tech_stage";

    public TechTreeDataProvider(ServerPlayer player) {
        this.lazyOptional = LazyOptional.of(() ->  new PlayerTechTreeData(player));
    }


    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction direction) {
        return CapInit.ResearchData.orEmpty(capability, lazyOptional);
    }

    //将数据解析为nbt
    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        lazyOptional.ifPresent(data -> tag.put(TREE_DATA, data.serializeNBT()));
        return tag;
    }


    //将nbt解析回数据
    @Override
    public void deserializeNBT(CompoundTag compoundTag) {
        lazyOptional.ifPresent(data->{
            data.deserializeNBT(compoundTag);
        });
    }

    @SubscribeEvent
    public static void clone(PlayerEvent.Clone event){
        if (event.isWasDeath()) {
            // 从原始玩家获取NBT数据
            event.getOriginal().getCapability(CapInit.ResearchData).ifPresent(oldCap -> {
                CompoundTag nbtData = oldCap.serializeNBT();
                
                // 将数据迁移到新玩家
                event.getEntity().getCapability(CapInit.ResearchData).ifPresent(newCap -> {
                    newCap.deserializeNBT(nbtData);
                });
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            ResearchApi.getTechTreeData(serverPlayer).ifPresent(data -> {
                data.setPlayer(serverPlayer);
            });
        }

    }


}
