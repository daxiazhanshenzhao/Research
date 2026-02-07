package org.research.gui.minecraft.component.overlay;

import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class RecipeOutputWidget extends AbstractButton {



    private ItemStack output;



    public RecipeOutputWidget(int x, int y, int width, int height, ItemStack output) {
        super(x, y, width, height, Component.empty());
        this.output = output;
    }

    @Override
    public void onPress() {

    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

}
