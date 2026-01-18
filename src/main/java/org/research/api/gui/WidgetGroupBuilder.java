package org.research.api.gui;


import lombok.Getter;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;


@Deprecated
public class WidgetGroupBuilder {

    /**
     * 相对256 * 256 图片的位置
     */
    @Getter
    private int uOffset;
    @Getter
    private int vOffset;
    @Getter
    private int wight;
    @Getter
    private int height;
    @Getter
    private List<AbstractWidget> childWidgets;

    private WidgetGroupBuilder(){
        this.childWidgets = new ArrayList<>();
    }

    public static WidgetGroupBuilder Builder() {
        return new WidgetGroupBuilder();
    }


    public WidgetGroupBuilder uOffset(int uOffset) {
        this.uOffset = uOffset;
        return this;
    }

    public WidgetGroupBuilder vOffset(int vOffset) {
        this.vOffset = vOffset;
        return this;
    }

    public WidgetGroupBuilder wight(int wight) {
        this.wight = wight;
        return this;
    }

    public WidgetGroupBuilder height(int height) {
        this.height = height;
        return this;
    }

    public WidgetGroupBuilder addChildWidget(AbstractWidget widget) {
        this.childWidgets.add(widget);
        return this;
    }

    public WidgetGroupBuilder builder(){
        if (!validate()){
            throw new RuntimeException("You didn't define all config attributes!");
        }
        return this;
    }

    private boolean validate(){
        return uOffset >= 0 && vOffset >= 0 && wight >= 0 && height >= 0;
    }
}
