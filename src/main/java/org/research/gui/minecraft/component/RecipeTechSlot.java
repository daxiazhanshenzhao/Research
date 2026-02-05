package org.research.gui.minecraft.component;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class RecipeTechSlot extends AbstractButton implements IOpenRenderable{

    private final List<ItemStack> items;

    public static final int Width = 20;
    public static final int Height = 20;

    public static final int ITEM_CHANGE_TICK = 20 * 4; // 每隔20tick切换一次物品显示

    public RecipeTechSlot(int x, int y, List<ItemStack> item) {
        super(x, y, Width, Height, Component.empty());
        this.items = item; // ✅ 修复：正确赋值 items 字段
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (items == null || items.isEmpty()) {
            return;
        }
        // 调用 super.render() 以确保 isHovered 状态正确更新
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (items == null || items.isEmpty()) {
            return;
        }

        // 获取玩家存在时间（tickCount）用于物品切换
        var player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        int playerTickCount = player.tickCount;

        // 计算当前应该显示的物品索引
        int currentIndex = (playerTickCount / ITEM_CHANGE_TICK) % items.size();

        //渲染背景

        //渲染物品
        guiGraphics.renderItem(items.get(currentIndex), this.getX(), this.getY());
        var count = items.get(currentIndex).getCount() > 1 ? items.get(currentIndex).getCount() : "";
        guiGraphics.drawString(Minecraft.getInstance().font, String.valueOf(count) ,getX()+10,getY()+10,0xFFFFFF);
        //渲染覆盖层
    }

    /**
     * 渲染物品的 tooltip
     * @param guiGraphics 绘制上下文
     * @param mouseX 鼠标X坐标
     * @param mouseY 鼠标Y坐标
     */
    public void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // 检查鼠标是否悬停在槽位上
        if (!this.isHoveredOrFocused()) {
            return;
        }

        // 检查物品列表是否为空
        if (items == null || items.isEmpty()) {
            return;
        }

        // 获取玩家存在时间（tickCount）用于物品切换
        var player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        int playerTickCount = player.tickCount;

        // 计算当前应该显示的物品索引（与 render 方法中的逻辑一致）
        int currentIndex = (playerTickCount / ITEM_CHANGE_TICK) % items.size();

        // 获取当前显示的物品
        ItemStack currentItem = items.get(currentIndex);
        if (currentItem.isEmpty()) {
            return;
        }

        // 渲染物品的 tooltip（使用 Minecraft 原生的 tooltip 系统）
        guiGraphics.renderTooltip(Minecraft.getInstance().font, currentItem, mouseX, mouseY);
    }

    @Override
    public void onPress() {

    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

    @Override
    public int getZLevel() {
        return 1600;
    }


}
