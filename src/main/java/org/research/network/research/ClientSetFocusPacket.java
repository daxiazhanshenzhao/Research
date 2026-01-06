package org.research.network.research;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import org.research.api.init.TechInit;
import org.research.api.util.ResearchApi;

import java.util.function.Supplier;

/**
 * 客户端到服务器的网络包，用于设置玩家当前聚焦的科技
 */
public class ClientSetFocusPacket {
    private ResourceLocation focusTechId;

    public ClientSetFocusPacket(FriendlyByteBuf buf){
        this.focusTechId = buf.readResourceLocation();
    }
    public ClientSetFocusPacket(ResourceLocation focusTechId){
        this.focusTechId = focusTechId;
    }

    public void toBytes(FriendlyByteBuf buf){
        buf.writeResourceLocation(this.focusTechId);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier){
        NetworkEvent.Context context = supplier.get();
        context.setPacketHandled(true);

        context.enqueueWork(() -> {
            // 确保是从客户端发送到服务器的包
            var sender = context.getSender();
            if (sender != null) {
                // 获取要聚焦的科技
                var tech = TechInit.getTech(focusTechId);
                if (tech != null) {
                    // 获取玩家的科技树数据并设置聚焦
                    ResearchApi.getTechTreeData(sender).ifPresent(data -> {
                        data.focus(tech);
                    });
                }
            }
        });

        return true;
    }
}
