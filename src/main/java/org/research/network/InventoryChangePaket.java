package org.research.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.NetworkEvent;
import org.research.api.event.custom.InventoryChangeEvent;

import java.util.function.Supplier;

/**
 * 背包变化网络包
 * 服务端 -> 客户端：通知客户端玩家背包发生了变化
 */
public class InventoryChangePaket {

    private final ItemStack changedItem;
    private final int slotIndex;

    /**
     * 构造函数
     * @param changedItem 变化的物品
     * @param slotIndex 变化的槽位索引
     */
    public InventoryChangePaket(ItemStack changedItem, int slotIndex) {
        this.changedItem = changedItem;
        this.slotIndex = slotIndex;
    }

    /**
     * 编码：将数据写入缓冲区
     * @param buffer 网络缓冲区
     */
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeItem(changedItem);
        buffer.writeInt(slotIndex);
    }

    /**
     * 解码：从缓冲区读取数据
     * @param buffer 网络缓冲区
     * @return 解码后的包实例
     */
    public static InventoryChangePaket decode(FriendlyByteBuf buffer) {
        ItemStack item = buffer.readItem();
        int slot = buffer.readInt();
        return new InventoryChangePaket(item, slot);
    }

    /**
     * 处理：客户端接收到包后的处理逻辑
     * @param context 网络上下文
     */
    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            // ✅ 在客户端主线程执行
            handleClientSide();
        });
        context.get().setPacketHandled(true);
    }

    /**
     * 客户端处理逻辑
     * ✅ 在客户端重新发布 InventoryChangeEvent 事件，使其成为双端事件
     */
    private void handleClientSide() {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        // ✅ 在客户端发布 InventoryChangeEvent 事件
        // 这样事件监听器可以在客户端和服务端都响应
        MinecraftForge.EVENT_BUS.post(new InventoryChangeEvent(player, changedItem, slotIndex));

        // 注意：具体的客户端逻辑（更新缓存）现在在 PlayerEventHandle.inventoryChange() 中处理
    }

    /**
     * 获取变化的物品
     * @return 物品堆栈
     */
    public ItemStack getChangedItem() {
        return changedItem;
    }

    /**
     * 获取变化的槽位索引
     * @return 槽位索引
     */
    public int getSlotIndex() {
        return slotIndex;
    }
}
