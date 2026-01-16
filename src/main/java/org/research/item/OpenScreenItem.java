package org.research.item;

import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.editor.data.UIProject;
import com.lowdragmc.lowdraglib.gui.factory.HeldItemUIFactory;
import com.lowdragmc.lowdraglib.gui.modular.IUIHolder;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.ResourceBorderTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraftforge.client.gui.widget.ScrollPanel;
import org.research.Research;
//var button = new DraggableScrollableWidgetGroup()
public class OpenScreenItem extends Item implements IUIHolder.ItemUI {
    public OpenScreenItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getPlayer() instanceof ServerPlayer serverPlayer) {
            HeldItemUIFactory.INSTANCE.openUI(serverPlayer, context.getHand());
        }
        return InteractionResult.SUCCESS;
    }

    private WidgetGroup createUI() {

        var creator = UIProject.loadUIFromFile(ResourceLocation.fromNamespaceAndPath(Research.LDLIB,"tech_screen"));



        return creator.get();


    }


    @Override
    public ModularUI createUI(Player player, HeldItemUIFactory.HeldItemHolder heldItemHolder) {
        return new ModularUI(createUI(), heldItemHolder, player);
    }
}
