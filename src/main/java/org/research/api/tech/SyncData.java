package org.research.api.tech;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.research.api.init.PacketInit;
import org.research.api.util.Vec2i;
import org.research.network.research.ClientboundSyncPlayerData;

import java.util.HashMap;
import java.util.Map;

import static org.research.api.tech.capability.TechTreeDataProvider.*;

public class SyncData {

    private ServerPlayer player;
    private int playerId;

    private Map<ResourceLocation, TechInstance> cacheds = new HashMap<>();
    private Map<ResourceLocation, Vec2i> vecMap = new HashMap<>();
    private int stage = 0;


    public SyncData(Map<ResourceLocation, TechInstance> cacheds, Map<ResourceLocation, Vec2i> vecMap, int stage) {
        this.cacheds = cacheds;
        this.vecMap = vecMap;
        this.stage = stage;
    }

    //客户端发过来
    public SyncData(int serverPlayerId) {
        this.player = null;
        this.playerId = serverPlayerId;

    }

    public SyncData(PlayerTechTreeData treeData) {
        this(treeData.getPlayer());
        // 直接使用 techMap 创建缓存的副本，避免引用同一个对象
        this.cacheds = new HashMap<>(treeData.getTechMap());
        this.vecMap = treeData.getVecMap();
        this.stage = treeData.getStage();
    }

    //服务端发送
    public SyncData(ServerPlayer player) {
        this(player == null ? -1 : player.getId());
        this.player = player;
    }

    public static final EntityDataSerializer<SyncData> SYNCED_SPELL_DATA = new EntityDataSerializer.ForValueType<SyncData>() {

        @Override
        public void write(FriendlyByteBuf buf, SyncData syncData) {
            buf.writeInt(syncData.playerId);

            buf.writeMap(syncData.cacheds,
                    FriendlyByteBuf::writeResourceLocation,
                    (friendlyByteBuf, techInstance) -> {
                        friendlyByteBuf.writeResourceLocation(techInstance.getIdentifier());
                        friendlyByteBuf.writeInt(techInstance.getStateValue());
                        friendlyByteBuf.writeBoolean(techInstance.isFocused());
                    });
            buf.writeMap(syncData.vecMap, FriendlyByteBuf::writeResourceLocation,
                    (friendlyByteBuf, vec2i) -> {
                        friendlyByteBuf.writeInt(vec2i.x);
                        friendlyByteBuf.writeInt(vec2i.y);
                    });
            buf.writeInt(syncData.stage);

        }

        @Override
        public SyncData read(FriendlyByteBuf buf) {
            int playerId = buf.readInt();
            SyncData data = new SyncData(playerId);

            // 读取 cacheds 映射
            data.cacheds = buf.readMap(
                    FriendlyByteBuf::readResourceLocation,
                    friendlyByteBuf -> {
                        ResourceLocation techId = friendlyByteBuf.readResourceLocation();
                        int stateValue = friendlyByteBuf.readInt();
                        boolean focused = friendlyByteBuf.readBoolean();
                        return new TechInstance(techId, stateValue, focused);
                    }
            );

            // 读取 vecMap 映射
            data.vecMap = buf.readMap(
                    FriendlyByteBuf::readResourceLocation,
                    friendlyByteBuf -> new Vec2i(friendlyByteBuf.readInt(), friendlyByteBuf.readInt())
            );

            // 读取 stage
            data.stage = buf.readInt();

            return data;
        }


    };


    public void saveNbt(CompoundTag compoundTag){
        var dataResult = CODEC.encodeStart(NbtOps.INSTANCE, this);
        dataResult.result().ifPresent(nbt->compoundTag.put(TREE_DATA, nbt));
    }

    public void loadNbt(CompoundTag compoundTag){
        Tag dataResult = compoundTag.get(TREE_DATA);
        var a = CODEC.parse(NbtOps.INSTANCE,dataResult);
        a.result().ifPresent(syncData ->{
                    this.cacheds = syncData.cacheds;
                    this.vecMap = syncData.getVecMap();
                }
        );
    }

    public static final Codec<SyncData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.unboundedMap(ResourceLocation.CODEC, TechInstance.CODEC).fieldOf(CACHEDs).forGetter(SyncData::getCacheds),
                    Codec.unboundedMap(ResourceLocation.CODEC, Vec2i.CODEC).fieldOf(VEC).forGetter(SyncData::getVecMap),
                    Codec.INT.fieldOf(STAGE).forGetter(SyncData::getStage)
            ).apply(instance,SyncData::new));




    public Map<ResourceLocation, TechInstance> getCacheds() {
        return cacheds;
    }

    public int getStage() {
        return stage;
    }

    public int getPlayerId() {
        return playerId;
    }

    public Map<ResourceLocation, Vec2i> getVecMap() {
        return vecMap;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public void syncToClient(){
        PacketInit.sendToPlayer(new ClientboundSyncPlayerData(this),player);
    }

    public ResourceLocation getFocusTech(){
        for (TechInstance tech : cacheds.values()) {
            if (tech.isFocused()){
                return tech.getIdentifier();
            }
        }
        return TechInstance.EMPTY.getIdentifier();
    }

    /**
     * 计算数据的哈希值，用于检测数据是否发生变化
     * @return 数据的哈希值
     */
    public int getDataHash() {
        int hash = 17;
        hash = 31 * hash + playerId;
        hash = 31 * hash + stage;

        // 包含 cacheds 的哈希
        for (var entry : cacheds.entrySet()) {
            hash = 31 * hash + entry.getKey().hashCode();
            TechInstance instance = entry.getValue();
            hash = 31 * hash + instance.getStateValue();
            hash = 31 * hash + (instance.isFocused() ? 1 : 0);
        }

        // 包含 vecMap 的哈希（通常不变，但为了完整性还是包含）
        hash = 31 * hash + vecMap.size();

        return hash;
    }

}
