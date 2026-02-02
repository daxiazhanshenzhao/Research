package org.research.api.gui;


import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class MouseData{

    private double transformedMouseX;
    private double transformedMouseY;
    private double offsetX;
    private double offsetY;

    // 缩放相关字段
    private float scale = 1.0f;  // 当前缩放比例
    private double dragStartX;
    private double dragStartY;
    private double dragTotal;
    private boolean canDrag;

    public MouseData() {
    }

}
