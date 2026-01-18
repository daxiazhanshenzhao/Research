package org.research.gui.minecraft.component;

import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class SearchTechSlot extends AbstractButton {
    public SearchTechSlot(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }

    @Override
    public void onPress() {

    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }
}
