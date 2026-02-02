package org.research.api.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.research.api.util.Vec2i;

public class PoseStackData {

    @Getter
    private PoseStack poseStack;

    public PoseStackData(){

    }

    public void setPose(GuiGraphics context) {
        this.poseStack = context.pose();
    }

    public void translate(float x, float y, float z) {
        if (this.poseStack != null) {
            this.poseStack.translate(x, y, z);
        }
    }

    public void scale(float sx, float sy, float sz) {
        if (this.poseStack != null) {
            this.poseStack.scale(sx, sy, sz);
        }
    }

    public void pushPose() {
        if (this.poseStack != null) {
            this.poseStack.pushPose();
        }
    }

    public void popPose() {
        if (this.poseStack != null) {
            this.poseStack.popPose();
        }
    }

    public Vec2i transform(Vec2i vec) {
        // 检查 PoseStack 是否可用
        if (poseStack == null) {
            return new Vec2i(vec.x(), vec.y());
        }

        // 尝试获取栈顶元素，如果栈为空则返回原始坐标
        PoseStack.Pose pose;
        try {
            pose = poseStack.last();
        } catch (Exception e) {
            // 栈为空，直接返回原始坐标
            return new Vec2i(vec.x(), vec.y());
        }

        // 实时计算：将 2D 坐标转换为 4D 齐次坐标 (x, y, 0, 1)
        Vector4f vec4f = new Vector4f(vec.x(), vec.y(), 0, 1);

        // 使用矩阵变换向量（矩阵左乘向量）
        pose.pose().transform(vec4f);

        // 如果有透视变换，进行透视除法
        if (Math.abs(vec4f.w - 1.0F) > 0.0001F) {
            return new Vec2i(
                Math.round(vec4f.x / vec4f.w),
                Math.round(vec4f.y / vec4f.w)
            );
        } else {
            // 返回转换后的 2D 坐标
            return new Vec2i(
                Math.round(vec4f.x),
                Math.round(vec4f.y)
            );
        }
    }

    /**
     * 矩阵反变换，接收 double 坐标，返回 double 数组
     * @param x 屏幕空间 X 坐标
     * @param y 屏幕空间 Y 坐标
     * @return double[] {guiX, guiY} - GUI 空间坐标
     */
    public double[] inverseTransform(double x, double y) {
        // 检查 PoseStack 是否已初始化，并且栈不为空
        if (poseStack == null) {
            // 如果未初始化，直接返回原始坐标（无变换）
            return new double[]{x, y};
        }

        // 尝试获取栈顶元素，如果栈为空则返回原始坐标
        PoseStack.Pose pose;
        try {
            pose = poseStack.last();
        } catch (Exception e) {
            // 栈为空，直接返回原始坐标
            return new double[]{x, y};
        }

        // 实时计算：将 double 坐标转换为 4D 齐次坐标 (x, y, 0, 1)
        Vector4f vec4f = new Vector4f((float)x, (float)y, 0, 1);

        // 实时创建逆矩阵
        Matrix4f matrix = new Matrix4f(pose.pose());
        matrix.invert();

        // 使用逆矩阵变换向量（矩阵左乘向量）
        matrix.transform(vec4f);

        // 如果有透视变换，进行透视除法
        if (Math.abs(vec4f.w - 1.0F) > 0.0001F) {
            return new double[]{
                vec4f.x / vec4f.w,
                vec4f.y / vec4f.w
            };
        } else {
            // 返回反变换后的 2D 坐标
            return new double[]{vec4f.x, vec4f.y};
        }
    }

    /**
     * 矩阵反变换（Vec2i 版本，保持向后兼容）
     */
    public Vec2i inverseTransform(Vec2i vec) {
        double[] result = inverseTransform((double)vec.x(), (double)vec.y());
        return new Vec2i((int)Math.round(result[0]), (int)Math.round(result[1]));
    }

    /**
     * 矩阵反变换（int 版本，保持向后兼容）
     */
    public Vec2i inverseTransform(int x, int y) {
        double[] result = inverseTransform((double)x, (double)y);
        return new Vec2i((int)Math.round(result[0]), (int)Math.round(result[1]));
    }
}


