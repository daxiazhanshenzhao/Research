package org.research.api.gui;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScreenData {

    private float scale;
    private boolean openRecipe;
    private int guiLeft,guiTop;
    private int insideX,insideY;

    public ScreenData() {
        this(1.0f, false, 0, 0);
    }

    public ScreenData(float scale, boolean openRecipe, int guiLeft, int guiTop) {
        this.scale = scale;
        this.openRecipe = openRecipe;
        this.guiLeft = guiLeft;
        this.guiTop = guiTop;
    }
}
