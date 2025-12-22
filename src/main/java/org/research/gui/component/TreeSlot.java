package org.research.gui.component;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.research.api.tech.AbstractTech;

public class TreeSlot extends Button {

    protected TreeSlot(int x, int y, int width, int height, Component message, OnPress onPress, CreateNarration createNarration, AbstractTech tech) {
        super(x, y, width, height, message, onPress, createNarration);
    }
}
