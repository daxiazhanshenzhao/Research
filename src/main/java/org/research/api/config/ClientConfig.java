package org.research.api.config;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import org.research.Research;
import org.research.api.util.BlitContextV2;
import org.research.api.util.UVContext;

import java.util.List;
import java.util.Objects;

public class ClientConfig {

    public static final ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.ConfigValue<String> BACKGROUND_TEXTURE;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> BACKGROUND_TEXTURE_SIZE;

    public static final ForgeConfigSpec.ConfigValue<String> WINDOW_TEXTURE;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> WINDOW_TEXTURE_SIZE;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> INSIDE_UV;

    public static final ForgeConfigSpec.DoubleValue MAX_SCALE;

    public static final ForgeConfigSpec.DoubleValue MIN_SCALE;

    public static final ForgeConfigSpec.DoubleValue MOVABLE_AREA_RATIO;

    public static final ForgeConfigSpec.IntValue OVERLAY_X_OFFSET;
    public static final ForgeConfigSpec.IntValue OVERLAY_Y_OFFSET;

    public static final ForgeConfigSpec SPEC;

    static {
        CLIENT_BUILDER.push("gui");

        // ========== 背景配置 ==========
        CLIENT_BUILDER.push("background");
        BACKGROUND_TEXTURE = CLIENT_BUILDER.comment("背景纹理资源路径，格式: namespace:path 或 namespace:assets/path，示例: research:textures/gui/background.png")
                .define("texture", Research.asResource("textures/gui/background.png").toString());
        BACKGROUND_TEXTURE_SIZE = CLIENT_BUILDER.comment("背景纹理的原始大小: [u, v, width, height, textureWidth, textureHeight]")
                .defineList("size", () -> List.of(0, 0, 1024, 1024,1024,1024), o -> o instanceof Integer);
        CLIENT_BUILDER.pop();

        // ========== 窗口配置 ==========
        CLIENT_BUILDER.push("window");
        WINDOW_TEXTURE = CLIENT_BUILDER.comment("窗口纹理资源路径，格式: namespace:path 或 namespace:assets/path，示例: research:textures/gui/window.png")
                .define("texture", Research.asResource("textures/gui/window.png").toString());
        WINDOW_TEXTURE_SIZE = CLIENT_BUILDER.comment("窗口纹理的原始大小: [u, v, width, height, textureWidth, textureHeight]")
                .defineList("size", () -> List.of(8, 21, 247, 221, 256, 256), o -> o instanceof Integer);
        INSIDE_UV = CLIENT_BUILDER.comment("窗口内部空白部分的 UV 坐标: [u, v, width, height]")
                .defineList("insideUV", () -> List.of(15, 28, 226, 186), o -> o instanceof Integer);
        CLIENT_BUILDER.pop();

        // ========== 缩放配置 ==========
        CLIENT_BUILDER.push("scale");
        MAX_SCALE = CLIENT_BUILDER.comment("界面最大缩放率")
                .defineInRange("max", 2d, 1d, 10d);
        MIN_SCALE = CLIENT_BUILDER.comment("界面最小缩放率")
                .defineInRange("min", 0.5d, 0.1d, 1d);
        CLIENT_BUILDER.pop();

        // ========== 背景移动限制配置 ==========
        CLIENT_BUILDER.push("movable");
        MOVABLE_AREA_RATIO = CLIENT_BUILDER.comment(
                "无效属性，已经弃用",
                "背景可移动区域比例 (0.0-1.0)",
                "控制背景实际可以平移的范围。",
                "例如: 0.8 表示 1024x1024 的背景只有 819x819 (1024*0.8) 的区域可以用于平移",
                "值越小，可移动范围越小；1.0 表示整个背景都可以移动"
        ).defineInRange("areaRatio", 0.8d, 0.0d, 1.0d);
        CLIENT_BUILDER.pop();

        // ========== 覆盖层偏移配置 ==========
        CLIENT_BUILDER.push("overlay");
        OVERLAY_X_OFFSET = CLIENT_BUILDER.comment(
                "ResearchOverlay 的水平偏移量（像素）",
                "正值向右偏移，负值向左偏移"
        ).defineInRange("xOffset", 0, -1000, 1000);
        OVERLAY_Y_OFFSET = CLIENT_BUILDER.comment(
                "ResearchOverlay 的垂直偏移量（像素）",
                "正值向下偏移，负值向上偏移"
        ).defineInRange("yOffset", 0, -1000, 1000);
        CLIENT_BUILDER.pop();

        CLIENT_BUILDER.pop();
        SPEC = CLIENT_BUILDER.build();
    }

    /**
     * 解析并返回背景纹理的 ResourceLocation（若解析失败返回 Research.asResource 的默认）
     */
    public static ResourceLocation getBackgroundTexture() {
        String s = BACKGROUND_TEXTURE.get();
        if (s == null || s.isBlank()) return Research.asResource("textures/gui/background.png");
        try {
            return new ResourceLocation(s);
        } catch (Exception e) {
            return Research.asResource("textures/gui/background.png");
        }
    }

    /**
     * 返回背景的 BlitContextV2
     */
    public static BlitContextV2 getBackgroundBlitContextV2() {
        List<? extends Integer> list = BACKGROUND_TEXTURE_SIZE.get();
        int u = 0, v = 0, width = 512, height = 512, textureWidth = 512, textureHeight = 512;

        if (list != null && list.size() >= 6) {
            try {
                u = Objects.requireNonNullElse(list.get(0), u);
                v = Objects.requireNonNullElse(list.get(1), v);
                width = Objects.requireNonNullElse(list.get(2), width);
                height = Objects.requireNonNullElse(list.get(3), height);
                textureWidth = Objects.requireNonNullElse(list.get(4), textureWidth);
                textureHeight = Objects.requireNonNullElse(list.get(5), textureHeight);
            } catch (Exception ignore) {
            }
        }

        return new BlitContextV2(getBackgroundTexture(), u, v, width, height, textureWidth, textureHeight);
    }

    /**
     * 解析并返回窗口纹理的 ResourceLocation（若解析失败返回 Research.asResource 的默认）
     */
    public static ResourceLocation getWindowTexture() {
        String s = WINDOW_TEXTURE.get();
        if (s == null || s.isBlank()) return Research.asResource("textures/gui/window.png");
        try {
            return new ResourceLocation(s);
        } catch (Exception e) {
            return Research.asResource("textures/gui/window.png");
        }
    }

    /**
     * 返回窗口的 BlitContextV2
     */
    public static BlitContextV2 getWindowBlitContextV2() {
        List<? extends Integer> list = WINDOW_TEXTURE_SIZE.get();
        int u = 0, v = 0, width = 256, height = 256, textureWidth = 256, textureHeight = 256;

        if (list != null && list.size() >= 6) {
            try {
                u = Objects.requireNonNullElse(list.get(0), u);
                v = Objects.requireNonNullElse(list.get(1), v);
                width = Objects.requireNonNullElse(list.get(2), width);
                height = Objects.requireNonNullElse(list.get(3), height);
                textureWidth = Objects.requireNonNullElse(list.get(4), textureWidth);
                textureHeight = Objects.requireNonNullElse(list.get(5), textureHeight);
            } catch (Exception ignore) {
            }
        }

        return new BlitContextV2(getWindowTexture(), u, v, width, height, textureWidth, textureHeight);
    }

    /**
     * 返回内部空白部分的 UVContext
     */
    public static UVContext getInsideUVContext() {
        List<? extends Integer> list = INSIDE_UV.get();
        int u = 15, v = 28, width = 226, height = 186;

        if (list != null && list.size() >= 4) {
            try {
                u = Objects.requireNonNullElse(list.get(0), u);
                v = Objects.requireNonNullElse(list.get(1), v);
                width = Objects.requireNonNullElse(list.get(2), width);
                height = Objects.requireNonNullElse(list.get(3), height);
            } catch (Exception ignore) {
            }
        }

        return new UVContext(u, v, width, height);
    }

    /**
     * 获取 ResearchOverlay 的水平偏移量
     */
    public static int getOverlayXOffset() {
        return OVERLAY_X_OFFSET.get();
    }

    /**
     * 获取 ResearchOverlay 的垂直偏移量
     */
    public static int getOverlayYOffset() {
        return OVERLAY_Y_OFFSET.get();
    }
}
