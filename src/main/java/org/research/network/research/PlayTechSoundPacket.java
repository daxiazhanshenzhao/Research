package org.research.network.research;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 服务端发送到客户端，播放科技完成音效
 */
public class PlayTechSoundPacket {

    public PlayTechSoundPacket() {
    }

    public PlayTechSoundPacket(FriendlyByteBuf buf) {
        // 暂时不需要读取额外数据
    }

    public void toBytes(FriendlyByteBuf buf) {
        // 暂时不需要写入额外数据
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().setPacketHandled(true);

        ctx.get().enqueueWork(() -> {
            // 在客户端播放音效
            var player = Minecraft.getInstance().player;
            if (player != null) {
                player.level().playSound(
                    player,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    SoundEvents.PLAYER_LEVELUP,
                    SoundSource.PLAYERS,
                    1.0F,
                    1.0F
                );
            }
        });

        return true;
    }
}
