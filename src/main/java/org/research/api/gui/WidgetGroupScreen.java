package org.research.api.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;


@Deprecated
public abstract class WidgetGroupScreen extends Screen {

    private ResourceLocation identifier;
    private int x,y;



    public ResourceLocation getBgTexture(){
        return ResourceLocation.fromNamespaceAndPath(identifier.getNamespace().intern(), "textures/gui/group/" + identifier + "png");
    }

    private boolean activeGroup = true;

    protected WidgetGroupScreen() {
        super(Component.empty());
    }

    protected abstract WidgetGroupBuilder getWidgetGroupBuilder();

    @Override
    public void tick() {
        handleWindow();
    }

    private void handleWindow() {
        WidgetGroupBuilder builder = getWidgetGroupBuilder();
        if (builder != null) {
            this.x = (this.width - builder.getWight()) / 2;
            this.y = (this.height - builder.getHeight()) / 2;
        } else {
            this.x = (this.width - 256) / 2;
            this.y = (this.height - 256) / 2;
        }
    }




    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {


    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        super.mouseMoved(mouseX, mouseY);
    }
    
}
