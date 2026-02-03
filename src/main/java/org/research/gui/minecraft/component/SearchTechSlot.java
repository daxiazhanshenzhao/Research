package org.research.gui.minecraft.component;

import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.research.api.gui.ClientScreenManager;

public class SearchTechSlot extends AbstractButton {

    public static final int WIGHT = 20;
    public static final int HEIGHT = 20;

    private ClientScreenManager manager;

    public SearchTechSlot(int x, int y, ClientScreenManager manager) {
        super(x, y, WIGHT, HEIGHT,Component.empty());
        this.manager = manager;
    }

    @Override
    public void onPress() {

    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }
}
