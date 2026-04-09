package com.max.lock.client.event;

import java.util.List;

import com.max.lock.client.init.LocksRenderTypes;
import com.max.lock.client.util.LocksClientUtil;
import com.max.lock.common.capability.ILockableHandler;
import com.max.lock.common.capability.ISelection;
import com.max.lock.common.capability.LockCapabilityAccess;
import com.max.lock.common.config.LocksServerConfig;
import com.max.lock.common.init.LockItemTags;
import com.max.lock.common.util.Lockable;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

// Client rendering event handler (cross-platform via Architectury)
public final class LocksClientEvents {
    public static Lockable tooltipLockable;

    private LocksClientEvents() {
    }

    public static boolean holdingPick(Player player) {
        for (InteractionHand hand : InteractionHand.values())
            if (player.getItemInHand(hand).is(LockItemTags.LOCK_PICKS))
                return true;
        return false;
    }

    // Client tick - update lockable animations
    public static void onClientTick(Minecraft mc) {
        if (mc.level == null || mc.isPaused())
            return;
        ILockableHandler handler = LockCapabilityAccess.getHandler(mc.level);
        if (handler != null)
            handler.getLoaded().values().forEach(Lockable::tick);
    }

    // Render 3D lock items in world
    public static void renderLocks(PoseStack mtx, MultiBufferSource.BufferSource buf, float pt) {
        Minecraft mc = Minecraft.getInstance();
        Camera cam = LocksClientUtil.getCamera();
        Vec3 o = cam.getPosition();
        BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();
        ILockableHandler handler = LockCapabilityAccess.getHandler(mc.level);
        if (handler == null)
            return;

        double dMin = 0d;
        for (Lockable lkb : handler.getLoaded().values()) {
            Lockable.State state = lkb.getLockState(mc.level);
            if (state == null || o.distanceToSqr(state.pos) > 4096d)
                continue;
            double d = o.subtract(state.pos).lengthSqr();
            if (d <= 25d) {
                Vec3 look = o.add(mc.player.getViewVector(pt));
                double d1 = LocksClientUtil.distanceToLineSq(state.pos, o, look);
                if (d1 <= 4d && (dMin == 0d || d1 < dMin)) {
                    tooltipLockable = lkb;
                    dMin = d1;
                }
            }
            mtx.pushPose();
            mtx.translate(state.pos.x - o.x, state.pos.y - o.y, state.pos.z - o.z);
            com.mojang.math.Axis yp = com.mojang.math.Axis.YP;
            com.mojang.math.Axis xp = com.mojang.math.Axis.XP;
            com.mojang.math.Axis zp = com.mojang.math.Axis.ZP;
            mtx.mulPose(yp.rotationDegrees(-state.tr.dir.toYRot() - 180f));
            if (state.tr.face != AttachFace.WALL)
                mtx.mulPose(xp.rotationDegrees(90f));
            mtx.translate(0d, 0.1d, 0d);
            mtx.mulPose(
                    zp.rotationDegrees(Mth.sin(LocksClientUtil.cubicBezier1d(1f, 1f,
                            LocksClientUtil.lerp(lkb.maxSwingTicks - lkb.oldSwingTicks,
                                    lkb.maxSwingTicks - lkb.swingTicks, pt) / lkb.maxSwingTicks)
                            * lkb.maxSwingTicks / 5f * 3.14f) * 10f));
            mtx.translate(0d, -0.1d, 0d);
            mtx.scale(0.5f, 0.5f, 0.5f);
            int light = LevelRenderer.getLightColor(mc.level, mut.set(state.pos.x, state.pos.y, state.pos.z));
            mc.getItemRenderer().renderStatic(lkb.stack, ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY,
                    mtx, buf, mc.level, 0);
            mtx.popPose();
        }
        buf.endBatch();
    }

    // Render selection box when placing locks
    public static void renderSelection(PoseStack mtx, MultiBufferSource.BufferSource buf) {
        Minecraft mc = Minecraft.getInstance();
        Vec3 o = LocksClientUtil.getCamera().getPosition();
        ISelection select = LockCapabilityAccess.getSelection(mc.player);
        if (select == null)
            return;
        BlockPos pos = select.get();
        if (pos == null)
            return;
        BlockPos pos1 = mc.hitResult instanceof BlockHitResult bhr ? bhr.getBlockPos() : pos;
        boolean allow = Math.abs(pos.getX() - pos1.getX()) * Math.abs(pos.getY() - pos1.getY())
                * Math.abs(pos.getZ() - pos1.getZ()) <= LocksServerConfig.maxLockableVolume
                && LocksServerConfig.canLock(mc.level, pos1);
        LevelRenderer.renderLineBox(mtx, buf.getBuffer(LocksRenderTypes.OVERLAY_LINES),
                Math.min(pos.getX(), pos1.getX()) - o.x, Math.min(pos.getY(), pos1.getY()) - o.y,
                Math.min(pos.getZ(), pos1.getZ()) - o.z,
                Math.max(pos.getX(), pos1.getX()) + 1d - o.x, Math.max(pos.getY(), pos1.getY()) + 1d - o.y,
                Math.max(pos.getZ(), pos1.getZ()) + 1d - o.z,
                allow ? 0f : 1f, allow ? 1f : 0f, 0f, 0.5f);
        RenderSystem.disableDepthTest();
        buf.endBatch();
    }

    // Render HUD tooltip near lock in world
    public static void renderHudTooltip(PoseStack mtx, List<? extends FormattedCharSequence> lines, Font font) {
        if (lines.isEmpty())
            return;
        int width = 0;
        for (FormattedCharSequence line : lines) {
            int j = font.width(line);
            if (j > width)
                width = j;
        }
        int x = 36;
        int y = -36;
        int height = 8;
        if (lines.size() > 1)
            height += 2 + (lines.size() - 1) * 10;

        mtx.pushPose();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder buf = Tesselator.getInstance().getBuilder();
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        LocksClientUtil.square(buf, mtx, 0f, 0f, 4f, 0.05f, 0f, 0.3f, 0.8f);
        LocksClientUtil.line(buf, mtx, 1f, -1f, x / 3f + 0.6f, y / 2f, 2f, 0.05f, 0f, 0.3f, 0.8f);
        LocksClientUtil.line(buf, mtx, x / 3f, y / 2f, x - 3f, y / 2f, 2f, 0.05f, 0f, 0.3f, 0.8f);
        LocksClientUtil.vGradient(buf, mtx, x - 3, y - 4, x + width + 3, y - 3, 0.0627451f, 0f, 0.0627451f, 0.9411765f,
                0.0627451f, 0f, 0.0627451f, 0.9411765f);
        LocksClientUtil.vGradient(buf, mtx, x - 3, y + height + 3, x + width + 3, y + height + 4, 0.0627451f, 0f,
                0.0627451f, 0.9411765f, 0.0627451f, 0f, 0.0627451f, 0.9411765f);
        LocksClientUtil.vGradient(buf, mtx, x - 3, y - 3, x + width + 3, y + height + 3, 0.0627451f, 0f, 0.0627451f,
                0.9411765f, 0.0627451f, 0f, 0.0627451f, 0.9411765f);
        LocksClientUtil.vGradient(buf, mtx, x - 4, y - 3, x - 3, y + height + 3, 0.0627451f, 0f, 0.0627451f, 0.9411765f,
                0.0627451f, 0f, 0.0627451f, 0.9411765f);
        LocksClientUtil.vGradient(buf, mtx, x + width + 3, y - 3, x + width + 4, y + height + 3, 0.0627451f, 0f,
                0.0627451f, 0.9411765f, 0.0627451f, 0f, 0.0627451f, 0.9411765f);
        LocksClientUtil.vGradient(buf, mtx, x - 3, y - 3 + 1, x - 3 + 1, y + height + 3 - 1, 0.3137255f, 0f, 1f,
                0.3137255f, 0.15686275f, 0f, 0.49803922f, 0.3137255f);
        LocksClientUtil.vGradient(buf, mtx, x + width + 2, y - 3 + 1, x + width + 3, y + height + 3 - 1, 0.3137255f, 0f,
                1f, 0.3137255f, 0.15686275f, 0f, 0.49803922f, 0.3137255f);
        LocksClientUtil.vGradient(buf, mtx, x - 3, y - 3, x + width + 3, y - 3 + 1, 0.3137255f, 0f, 1f, 0.3137255f,
                0.3137255f, 0f, 1f, 0.3137255f);
        LocksClientUtil.vGradient(buf, mtx, x - 3, y + height + 2, x + width + 3, y + height + 3, 0.15686275f, 0f,
                0.49803922f, 0.3137255f, 0.15686275f, 0f, 0.49803922f, 0.3137255f);
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        BufferUploader.drawWithShader(buf.end());
        RenderSystem.disableBlend();
        MultiBufferSource.BufferSource buf1 = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());

        Matrix4f last = mtx.last().pose();
        for (int a = 0; a < lines.size(); ++a) {
            FormattedCharSequence line = lines.get(a);
            if (line != null)
                font.drawInBatch(line, (float) x, (float) y, -1, true, last, buf1, Font.DisplayMode.NORMAL, 0,
                        15728880);
            if (a == 0)
                y += 2;
            y += 10;
        }
        buf1.endBatch();
        mtx.popPose();
    }
}
