package org.research.gui.minecraft.component.overlay;

import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class RecipeInputWidget extends AbstractButton {
    private boolean open = false;
    private ItemStack input;
    private List<ItemStack> inputParents = new ArrayList<>();

    public RecipeInputWidget(int x, int y, int width, int height, ItemStack input, List<ItemStack> inputParents) {
        super(x, y, width, height, Component.empty());
        this.input = input;
        this.inputParents = inputParents;
    }

    @Override
    public void onPress() {

    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }
}
