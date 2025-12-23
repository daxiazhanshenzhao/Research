package org.research.gui.component;

import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.research.api.tech.AbstractTech;
import org.research.api.tech.TechInstance;

public class TechSlot extends AbstractButton {

    public static final int Width = 26;
    public static final int Height = 26;

    private TechInstance tech;





    public TechSlot(int x, int y,TechInstance tech) {
        super(x, y, Width, Height,Component.empty());
        this.tech = tech;
    }

    @Override
    public void onPress() {

    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }


}
