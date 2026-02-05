package org.research.gui.minecraft.component;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.research.api.gui.ClientScreenManager;
import org.research.api.init.PacketInit;
import org.research.api.util.BlitContext;
import org.research.api.util.InsideContext;
import org.research.api.util.Texture;
import org.research.network.research.ClientSetFocusPacket;

public class SearchTechSlot extends AbstractButton {

    public static final int WIGHT = 20;
    public static final int HEIGHT = 20;

    // 锁的纹理定义（与 TechSlot 保持一致）
    private static final BlitContext LOCK = BlitContext.of(Texture.TEXTURE, 0, 24, 10, 15);




    public final int id;

    private final ClientScreenManager manager;

    public SearchTechSlot(int x, int y, ClientScreenManager manager,int id) {
        super(x, y, WIGHT, HEIGHT,Component.empty());
        this.manager = manager;
        this.id = id;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        var context = InsideContext.RECIPE_SEARCH_TECH_BUTTON;
        var context_active = InsideContext.RECIPE_SEARCH_TECH_BUTTON_ACTIVE;

        //渲染背景
        if (isHoveredOrFocused()){
            guiGraphics.blit(context_active.texture(),getX()-1,getY()-1,
                    context_active.u(),context_active.v(),
                    context_active.width(),context_active.height(),
                    context_active.textureWidth(),context_active.textureHeight()
            );
        }else {
            guiGraphics.blit(context.texture(),getX(),getY(),
                    context.u(),context.v(),
                    context.width(),context.height(),
                    context.textureWidth(),context.textureHeight());

        }

        //渲染输出图标
        TechSlot techSlot = manager.getTechIconById(id);
        if (techSlot != null && techSlot != TechSlot.EMPTY && !techSlot.getTechInstance().isEmpty()) {
            var recipe = techSlot.getRecipe();
            if (recipe != null) {
                var minecraft = net.minecraft.client.Minecraft.getInstance();
                if (minecraft.getConnection() != null) {
                    var resultItem = recipe.getResultItem(minecraft.getConnection().registryAccess());
                    if (!resultItem.isEmpty()) {
                        // 渲染物品图标
                        guiGraphics.pose().pushPose();
                        guiGraphics.pose().translate(0, 0, -100);
                        guiGraphics.renderItem(resultItem, getX()+2, getY()+2);
                        guiGraphics.pose().popPose();

                    }
                }
            }

            // 渲染锁（如果科技被锁定）
            renderLock(guiGraphics, techSlot);
        }

    }

    /**
     * 渲染锁覆盖层（如果科技被锁定）
     * @param guiGraphics 绘制上下文
     * @param techSlot 科技槽位
     */
    private void renderLock(GuiGraphics guiGraphics, TechSlot techSlot) {
        if (techSlot == null || techSlot.getTechInstance().isEmpty()) {
            return;
        }

        // 检查科技状态是否为锁定
        if (!techSlot.getTechInstance().getState().isLocked()) {
            return;
        }

        // 渲染锁图标（z轴提升以显示在物品上方）
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, +100);
        guiGraphics.blit(LOCK.texture(), getX() + 5, getY() + 3,
                LOCK.u(), LOCK.v(), LOCK.width(), LOCK.height(), 512, 512);
        guiGraphics.pose().popPose();
    }

    /**
     * 渲染 tooltip
     */
    public void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (!isHovered()) {
            return;
        }

        TechSlot techSlot = manager.getTechIconById(id);
        if (techSlot != null && techSlot != TechSlot.EMPTY && !techSlot.getTechInstance().isEmpty()) {
            var recipe = techSlot.getRecipe();
            if (recipe != null) {
                var minecraft = net.minecraft.client.Minecraft.getInstance();
                if (minecraft.getConnection() != null) {
                    var resultItem = recipe.getResultItem(minecraft.getConnection().registryAccess());
                    if (!resultItem.isEmpty()) {
                        // 渲染物品的 tooltip（自动显示本地化名称）
                        guiGraphics.renderTooltip(minecraft.font, resultItem, mouseX, mouseY);
                    }
                }
            }
        }
    }

    @Override
    public void onPress() {
        // 点击后定位到对应的科技并设置焦点
        if (manager.getScreenData().isOpenRecipe()){
            TechSlot techSlot = manager.getTechIconById(id);
            if (techSlot != null && techSlot != TechSlot.EMPTY && !techSlot.getTechInstance().isEmpty()) {
                // 发送焦点数据包到服务器
                PacketInit.sendToServer(
                        new ClientSetFocusPacket(techSlot.getTechInstance().getIdentifier())
                );
                // 设置客户端焦点
                manager.getTechSlotData().setFocusTechSlot(techSlot);

                // 将视图中心移动到目标科技位置
                manager.centerOnTechSlot(techSlot);
            }
        }

    }



    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }
}
