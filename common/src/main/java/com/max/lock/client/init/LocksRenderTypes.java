package com.max.lock.client.init;

import java.util.OptionalDouble;
import com.max.lock.common.Lock;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

// Custom RenderType for selection box overlay
public final class LocksRenderTypes extends RenderType {
    public static final RenderType OVERLAY_LINES = RenderType.create(
            Lock.MOD_ID + ".overlay_lines",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.LINES,
            256,
            RenderType.CompositeState.builder()
                    .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.empty()))
                    .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(NO_DEPTH_TEST)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false));

    private LocksRenderTypes(String name, VertexFormat format, VertexFormat.Mode mode, int bufSize, boolean useDelegate,
            boolean sorting, Runnable setup, Runnable clear) {
        super(name, format, mode, bufSize, useDelegate, sorting, setup, clear);
    }
}
