package com.max.lock.client.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

// Client-side rendering utilities
public final class LocksClientUtil {
    private LocksClientUtil() {
    }

    // Draw textured quad with alpha
    public static void texture(PoseStack mtx, float x, float y, int u, int v, int width, int height, int texWidth,
            int texHeight, float alpha) {
        Matrix4f last = mtx.last().pose();
        float f = 1f / texWidth;
        float f1 = 1f / texHeight;
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        BufferBuilder buf = Tesselator.getInstance().getBuilder();
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        buf.vertex(last, x, y + height, 0f).uv(u * f, (v + height) * f1).color(1f, 1f, 1f, alpha).endVertex();
        buf.vertex(last, x + width, y + height, 0f).uv((u + width) * f, (v + height) * f1).color(1f, 1f, 1f, alpha)
                .endVertex();
        buf.vertex(last, x + width, y, 0f).uv((u + width) * f, v * f1).color(1f, 1f, 1f, alpha).endVertex();
        buf.vertex(last, x, y, 0f).uv(u * f, v * f1).color(1f, 1f, 1f, alpha).endVertex();
        BufferUploader.drawWithShader(buf.end());
    }

    // Draw line segment as rectangle
    public static void line(BufferBuilder buf, PoseStack mtx, float x1, float y1, float x2, float y2, float width,
            float r, float g, float b, float a) {
        Matrix4f last = mtx.last().pose();
        float pX = y2 - y1;
        float pY = x1 - x2;
        float pL = Mth.sqrt(pX * pX + pY * pY);
        pX *= width / 2f / pL;
        pY *= width / 2f / pL;
        buf.vertex(last, x1 + pX, y1 + pY, 0f).color(r, g, b, a).endVertex();
        buf.vertex(last, x1 - pX, y1 - pY, 0f).color(r, g, b, a).endVertex();
        buf.vertex(last, x2 - pX, y2 - pY, 0f).color(r, g, b, a).endVertex();
        buf.vertex(last, x2 + pX, y2 + pY, 0f).color(r, g, b, a).endVertex();
    }

    // Draw square
    public static void square(BufferBuilder buf, PoseStack mtx, float x, float y, float length, float r, float g,
            float b, float a) {
        Matrix4f last = mtx.last().pose();
        length /= 2f;
        buf.vertex(last, x - length, y - length, 0f).color(r, g, b, a).endVertex();
        buf.vertex(last, x - length, y + length, 0f).color(r, g, b, a).endVertex();
        buf.vertex(last, x + length, y + length, 0f).color(r, g, b, a).endVertex();
        buf.vertex(last, x + length, y - length, 0f).color(r, g, b, a).endVertex();
    }

    public static float lerp(float start, float end, float progress) {
        return start + (end - start) * progress;
    }

    public static double lerp(double start, double end, double progress) {
        return start + (end - start) * progress;
    }

    public static net.minecraft.client.Camera getCamera() {
        return net.minecraft.client.Minecraft.getInstance().gameRenderer.getMainCamera();
    }

    public static double distanceToLineSq(net.minecraft.world.phys.Vec3 p, net.minecraft.world.phys.Vec3 l1,
            net.minecraft.world.phys.Vec3 l2) {
        net.minecraft.world.phys.Vec3 l = l2.subtract(l1);
        return l.cross(p.subtract(l1)).lengthSqr() / l.lengthSqr();
    }

    // World position -> screen position
    public static org.joml.Vector3f worldToScreen(net.minecraft.world.phys.Vec3 pos, float partialTicks) {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        net.minecraft.client.Camera cam = getCamera();
        net.minecraft.world.phys.Vec3 o = cam.getPosition();
        org.joml.Vector3f pos1 = new org.joml.Vector3f((float) (o.x - pos.x), (float) (o.y - pos.y),
                (float) (o.z - pos.z));
        org.joml.Quaternionf rot = new org.joml.Quaternionf(cam.rotation());
        rot.conjugate();
        rot.transform(pos1);

        if (mc.options.bobView().get()
                && mc.getCameraEntity() instanceof net.minecraft.world.entity.player.Player player) {
            float f = player.walkDist - player.walkDistO;
            float f1 = -(player.walkDist + f * partialTicks);
            float f2 = Mth.lerp(partialTicks, player.oBob, player.bob);
            org.joml.Quaternionf rot1 = new org.joml.Quaternionf()
                    .rotateX((float) Math.toRadians(Math.abs(Mth.cos(f1 * (float) Math.PI - 0.2f) * f2) * 5f));
            org.joml.Quaternionf rot2 = new org.joml.Quaternionf()
                    .rotateZ((float) Math.toRadians(Mth.sin(f1 * (float) Math.PI) * f2 * 3f));
            rot1.conjugate();
            rot2.conjugate();
            rot1.transform(pos1);
            rot2.transform(pos1);
            pos1.add(Mth.sin(f1 * (float) Math.PI) * f2 * 0.5f, Math.abs(Mth.cos(f1 * (float) Math.PI) * f2), 0f);
        }
        com.mojang.blaze3d.platform.Window w = mc.getWindow();
        float sc = w.getGuiScaledHeight() / 2f / pos1.z()
                / (float) Math.tan(Math.toRadians(mc.options.fov().get() / 2.0));
        pos1.mul(-sc, -sc, 1f);
        pos1.add(w.getGuiScaledWidth() / 2f, w.getGuiScaledHeight() / 2f, 0f);
        return pos1;
    }

    // Vertical gradient rectangle
    public static void vGradient(BufferBuilder bld, PoseStack mtx, int x1, int y1, int x2, int y2, float r1, float g1,
            float b1, float a1, float r2, float g2, float b2, float a2) {
        Matrix4f last = mtx.last().pose();
        bld.vertex(last, x2, y1, 0f).color(r1, g1, b1, a1).endVertex();
        bld.vertex(last, x1, y1, 0f).color(r1, g1, b1, a1).endVertex();
        bld.vertex(last, x1, y2, 0f).color(r2, g2, b2, a2).endVertex();
        bld.vertex(last, x2, y2, 0f).color(r2, g2, b2, a2).endVertex();
    }

    // 1D cubic bezier for animation easing
    public static float cubicBezier1d(float anchor1, float anchor2, float progress) {
        float omp = 1f - progress;
        return 3f * omp * omp * progress * anchor1 + 3f * omp * progress * progress * anchor2
                + progress * progress * progress;
    }
}
