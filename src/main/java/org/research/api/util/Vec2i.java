package org.research.api.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.joml.Vector2i;


public class Vec2i extends Vector2i {
    public Vec2i(int x, int y) {
        super(x, y);
    }

    public static final String X = "x";
    public static final String Y = "y";

    public static final Vec2i EMPTY = new Vec2i(0, 0);

    public static final Codec<Vec2i> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.INT.fieldOf(X).forGetter(Vec2i::x),
                Codec.INT.fieldOf(Y).forGetter(Vec2i::y)
        ).apply(instance,Vec2i::new));


}
