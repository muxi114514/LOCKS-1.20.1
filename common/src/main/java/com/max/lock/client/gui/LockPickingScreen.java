package com.max.lock.client.gui;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;

import com.max.lock.common.Lock;
import com.max.lock.client.gui.sprite.SpringSprite;
import com.max.lock.client.gui.sprite.Sprite;
import com.max.lock.client.gui.sprite.TextureInfo;
import com.max.lock.client.gui.sprite.action.AccelerateAction;
import com.max.lock.client.gui.sprite.action.FadeAction;
import com.max.lock.client.gui.sprite.action.IAction;
import com.max.lock.client.gui.sprite.action.MoveAction;
import com.max.lock.client.gui.sprite.action.WaitAction;
import com.max.lock.common.menu.LockPickingMenu;
import com.max.lock.common.network.TryPinPacket;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

// Lock picking mini-game GUI
public class LockPickingScreen extends AbstractContainerScreen<LockPickingMenu> {
    public static final Component HINT = Component.translatable(Lock.MOD_ID + ".gui.lockpicking.open");

    public static final TextureInfo FRONT_WALL_TEX = new TextureInfo(6, 0, 4, 60, 48, 80),
            COLUMN_TEX = new TextureInfo(10, 0, 8, 60, 48, 80),
            INNER_WALL_TEX = new TextureInfo(18, 0, 4, 60, 48, 80),
            BACK_WALL_TEX = new TextureInfo(22, 0, 4, 60, 48, 80),
            HANDLE_TEX = new TextureInfo(26, 0, 19, 73, 48, 80),
            UPPER_PIN_TEX = new TextureInfo(0, 0, 6, 8, 48, 80),
            LOCK_PICK_TEX = new TextureInfo(0, 0, 160, 12, 160, 16);
    public static final TextureInfo[] PIN_TUMBLER_TEX = {
            new TextureInfo(0, 8, 6, 11, 48, 80),
            new TextureInfo(0, 19, 6, 13, 48, 80),
            new TextureInfo(0, 32, 6, 15, 48, 80) };
    public static final TextureInfo[] SPRING_TEX = {
            new TextureInfo(0, 57, 6, 23, 48, 80),
            new TextureInfo(6, 60, 6, 20, 48, 80),
            new TextureInfo(12, 64, 6, 16, 48, 80),
            new TextureInfo(18, 69, 6, 11, 48, 80),
            new TextureInfo(24, 73, 6, 7, 48, 80) };

    public final ResourceLocation lockTex;
    protected ResourceLocation pickTex;
    protected Collection<Sprite> sprites;
    protected Sprite lockPick, leftPickPart, rightPickPart;
    protected Sprite[] pinTumblers, upperPins, springs;

    public final BiConsumer<IAction<Sprite>, Sprite> unfreezeCb = (action, sprite) -> this.frozen = false;
    public final BiConsumer<IAction<Sprite>, Sprite> resetPickCb = (action, sprite) -> this.resetPick();

    public final int length;
    public final boolean[] pins;
    /** 记录解锁顺序（栈：最后解开的在顶部） */
    protected final ArrayDeque<Integer> solveOrder;
    public final InteractionHand hand;
    protected int currPin;
    protected boolean frozen = true;

    public LockPickingScreen(LockPickingMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.length = menu.lockable.lock.getLength();
        this.pins = new boolean[this.length];
        this.solveOrder = new ArrayDeque<>(this.length);
        this.hand = menu.hand;
        this.lockTex = getTextureFor(menu.lockable.stack);
        this.imageWidth = (FRONT_WALL_TEX.width + this.length * (COLUMN_TEX.width + INNER_WALL_TEX.width)) * 2;
        this.imageHeight = HANDLE_TEX.height * 2;
        this.sprites = new ArrayDeque<>(this.length * 3 + 4);
        this.pinTumblers = new Sprite[this.length];
        this.upperPins = new Sprite[this.length];
        this.springs = new Sprite[this.length];
        for (int a = 0; a < this.pinTumblers.length; ++a) {
            int r = ThreadLocalRandom.current().nextInt(3);
            this.pinTumblers[a] = this.addSprite(new Sprite(PIN_TUMBLER_TEX[r]).position(
                    FRONT_WALL_TEX.width + 1 + a * (COLUMN_TEX.width + INNER_WALL_TEX.width),
                    43 - PIN_TUMBLER_TEX[r].height));
            this.upperPins[a] = new Sprite(UPPER_PIN_TEX).position(
                    FRONT_WALL_TEX.width + 1 + a * (COLUMN_TEX.width + INNER_WALL_TEX.width),
                    43 - PIN_TUMBLER_TEX[r].height - UPPER_PIN_TEX.height);
            this.springs[a] = this.addSprite(new SpringSprite(SPRING_TEX, this.upperPins[a])
                    .position(FRONT_WALL_TEX.width + 1 + a * (COLUMN_TEX.width + INNER_WALL_TEX.width), 3));
            this.addSprite(this.upperPins[a]);
        }
        this.lockPick = this
                .addSprite(new Sprite(LOCK_PICK_TEX).position(0f, -4 + COLUMN_TEX.height - LOCK_PICK_TEX.height));
        this.resetPick();
        this.rightPickPart = this.addSprite(
                new Sprite(new TextureInfo(0, 0, 0, 12, 160, 16)).position(-10f, this.lockPick.posY).alpha(0f));
        this.leftPickPart = this.addSprite(new Sprite(new TextureInfo(0, 0, 0, 12, 160, 16))
                .position(0f, this.lockPick.posY).rotation(-30f, -10f, this.lockPick.posY + 13f).alpha(0f));
    }

    public static ResourceLocation getTextureFor(ItemStack stack) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return new ResourceLocation(Lock.MOD_ID, "textures/gui/" + id.getPath() + ".png");
    }

    public Sprite addSprite(Sprite sprite) {
        this.sprites.add(sprite);
        return sprite;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gfx);
        super.render(gfx, mouseX, mouseY, partialTick);
    }

    @Override
    protected void renderBg(GuiGraphics gfx, float partialTick, int mouseX, int mouseY) {
        float pt = this.minecraft.getFrameTime();
        int cornerX = (this.width - this.imageWidth) / 2;
        int cornerY = (this.height - this.imageHeight) / 2;
        RenderSystem.setShaderTexture(0, this.lockTex);
        var mtx = gfx.pose();
        mtx.pushPose();
        mtx.translate(cornerX, cornerY, 0f);
        mtx.scale(2f, 2f, 2f);
        FRONT_WALL_TEX.draw(mtx, 0f, 0f, 1f);
        for (int a = 0; a < this.length; ++a) {
            COLUMN_TEX.draw(mtx, FRONT_WALL_TEX.width + a * (COLUMN_TEX.width + INNER_WALL_TEX.width), 0f, 1f);
            if (a != this.length - 1)
                INNER_WALL_TEX.draw(mtx,
                        FRONT_WALL_TEX.width + COLUMN_TEX.width + a * (COLUMN_TEX.width + INNER_WALL_TEX.width), 0f,
                        1f);
        }
        BACK_WALL_TEX.draw(mtx, this.length * (COLUMN_TEX.width + INNER_WALL_TEX.width), 0f, 1f);
        HANDLE_TEX.draw(mtx, BACK_WALL_TEX.width + this.length * (COLUMN_TEX.width + INNER_WALL_TEX.width), 2f, 1f);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        for (Sprite sprite : this.sprites) {
            if (sprite == this.lockPick)
                RenderSystem.setShaderTexture(0, this.pickTex);
            sprite.draw(mtx, pt);
        }
        mtx.popPose();
    }

    @Override
    protected void renderLabels(GuiGraphics gfx, int mouseX, int mouseY) {
        gfx.drawString(this.font, this.title, 0, -this.font.lineHeight, 0xffffff, false);
        if (this.getMenu().isOpen())
            gfx.drawString(this.font, HINT, (this.imageWidth - this.font.width(HINT)) / 2, this.imageHeight + 10,
                    0xffffff, false);
    }

    @Override
    public void containerTick() {
        super.containerTick();
        for (Sprite sprite : this.sprites)
            sprite.update();
        if (!this.frozen)
            this.boundLockPick();
        this.updatePickParts();
    }

    protected void updatePickParts() {
        this.rightPickPart.posY = this.lockPick.posY;
        this.rightPickPart.tex.width = 10 + (int) this.lockPick.posX + this.lockPick.tex.width;
        this.rightPickPart.tex.startX = this.rightPickPart.tex.canvasWidth - this.rightPickPart.tex.width;
        this.leftPickPart.posY = this.lockPick.posY;
        this.leftPickPart.tex.width = this.rightPickPart.tex.startX;
        this.leftPickPart.posX = -10 - this.leftPickPart.tex.width;
    }

    protected void boundLockPick() {
        this.lockPick.posX = 10 - LOCK_PICK_TEX.width + Mth.clamp(this.lockPick.posX - 10 + LOCK_PICK_TEX.width, 0,
                (this.length - 1) * (COLUMN_TEX.width + INNER_WALL_TEX.width));
    }

    @Override
    public boolean keyPressed(int key, int scan, int modifier) {
        if (this.frozen)
            return super.keyPressed(key, scan, modifier);
        if (this.minecraft.options.keyLeft.matches(key, scan))
            this.lockPick.speedX = -4;
        else if (this.minecraft.options.keyRight.matches(key, scan))
            this.lockPick.speedX = 4;
        else if (this.minecraft.options.keyUp.matches(key, scan) && !this.lockPick.isExecuting()
                && this.pullPin(this.getSelectedPin()))
            this.lockPick.execute(MoveAction.at(0f, -2.5f).time(3), MoveAction.at(0f, 2.5f).time(3));
        return super.keyPressed(key, scan, modifier);
    }

    @Override
    public boolean keyReleased(int key, int scan, int modifier) {
        if (this.frozen)
            return super.keyReleased(key, scan, modifier);
        if (this.minecraft.options.keyLeft.matches(key, scan)
                || this.minecraft.options.keyRight.matches(key, scan))
            this.lockPick.speedX = 0;
        return super.keyReleased(key, scan, modifier);
    }

    protected int getSelectedPin() {
        return (int) ((this.lockPick.posX - 10 + LOCK_PICK_TEX.width) / (COLUMN_TEX.width + INNER_WALL_TEX.width)
                + 0.5f);
    }

    protected boolean pullPin(int pin) {
        if (this.pins[pin])
            return false;
        this.currPin = pin;
        TryPinPacket.send((byte) pin);
        return true;
    }

    public void handlePin(boolean correct, int resetCount) {
        this.pinTumblers[this.currPin].execute(MoveAction.at(0f, -6f).time(2), MoveAction.at(0f, 6f).time(2));
        this.upperPins[this.currPin].execute(MoveAction.at(0f, -6f).time(2));
        if (correct) {
            this.pins[this.currPin] = true;
            this.solveOrder.push(this.currPin);
            this.upperPins[this.currPin]
                    .execute(MoveAction.to(this.upperPins[this.currPin], this.upperPins[this.currPin].posX, 29, 2));
        } else {
            this.upperPins[this.currPin].execute(MoveAction.at(0f, 6f).time(2));
        }
        if (resetCount > 0)
            this.reset(resetCount);
    }

    /** 从解锁栈顶弹出 count 个销子并复位（最近解开的先复位） */
    public void reset(int count) {
        for (int i = 0; i < count && !this.solveOrder.isEmpty(); ++i) {
            int a = this.solveOrder.pop();
            this.pins[a] = false;
            this.upperPins[a].execute(MoveAction.to(this.upperPins[a], this.upperPins[a].posX,
                    this.pinTumblers[a].posY - UPPER_PIN_TEX.height, 2));
        }
        this.lockPick.alpha(0f);
        this.rightPickPart.alpha(1f).execute(WaitAction.ticks(10), FadeAction.to(this.rightPickPart, 0f, 4));
        this.leftPickPart.alpha(1f).execute(WaitAction.ticks(10),
                FadeAction.to(this.rightPickPart, 0f, 4).then(resetPickCb));
        this.frozen = true;
    }

    public void resetPick() {
        this.pickTex = getTextureFor(Minecraft.getInstance().player.getItemInHand(this.hand));
        this.lockPick.position(-22 - LOCK_PICK_TEX.width, this.lockPick.posY).alpha(1f)
                .execute(AccelerateAction.to(32f, 0f, 4, false).then(unfreezeCb));
    }
}
