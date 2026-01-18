package org.research.gui.minecraft.component;

import mezz.jei.api.recipe.IRecipeManager;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.research.gui.minecraft.ResearchContainerScreen;

import java.util.List;

public class RecipeTechSlot extends AbstractButton implements IOpenRenderable{

    private ResearchContainerScreen screen;
    private List<ItemStack> items;

    public static final int Width = 20;
    public static final int Height = 20;

    public RecipeTechSlot(int x, int y, List<ItemStack> item, ResearchContainerScreen screen) {
        super(x, y, Width, Height, Component.empty());
        this.screen = screen;
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
