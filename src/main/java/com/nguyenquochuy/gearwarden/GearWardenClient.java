package com.nguyenquochuy.gearwarden;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;

import java.util.HashSet;
import java.util.Set;

public class GearWardenClient implements ClientModInitializer {

    public static GearWardenConfig CONFIG;
    private final Set<ItemStack> warnedThisSession = new HashSet<>();

    private static final int ICON_SIZE = 16;
    private static final int SLOT_GAP = 20;
    private static final int BAR_HEIGHT = 2;

    @Override
    public void onInitializeClient() {
        CONFIG = GearWardenConfig.load();

        HudRenderCallback.EVENT.register((DrawContext ctx, RenderTickCounter tickCounter) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            PlayerEntity player = client.player;
            if (player == null || client.options.hudHidden) return;

            renderArmorColumn(ctx, client, player);
            renderToolColumn(ctx, client, player);
        });
    }

    // Cot giap: dat ngay canh o totem/offhand, ben trai hotbar (hoac phai neu config doi)
    private void renderArmorColumn(DrawContext ctx, MinecraftClient client, PlayerEntity player) {
        int screenW = client.getWindow().getScaledWidth();
        int screenH = client.getWindow().getScaledHeight();
        int hotbarHalfWidth = 91; // vanilla hotbar half-width o do phan giai chuan
        boolean left = CONFIG.armorSide.equals("left");
        int x = left
                ? screenW / 2 - hotbarHalfWidth - ICON_SIZE - 8 + CONFIG.offsetX
                : screenW / 2 + hotbarHalfWidth + 8 + CONFIG.offsetX;
        int baseY = screenH - 22;

        EquipmentSlot[] slots = {
                EquipmentSlot.FEET, EquipmentSlot.LEGS,
                EquipmentSlot.CHEST, EquipmentSlot.HEAD
        };

        int y = baseY;
        for (EquipmentSlot slot : slots) {
            ItemStack stack = player.getEquippedStack(slot);
            if (!stack.isEmpty() && stack.isDamageable()) {
                drawIconWithBar(ctx, client, stack, x, y);
                y -= SLOT_GAP;
            }
        }
    }

    // Cot tool cam tay: doi dien voi cot giap
    private void renderToolColumn(DrawContext ctx, MinecraftClient client, PlayerEntity player) {
        ItemStack main = player.getMainHandStack();
        if (main.isEmpty() || !main.isDamageable()) return;

        int screenW = client.getWindow().getScaledWidth();
        int screenH = client.getWindow().getScaledHeight();
        int hotbarHalfWidth = 91;
        boolean right = CONFIG.toolSide.equals("right");
        int x = right
                ? screenW / 2 + hotbarHalfWidth + 8 + CONFIG.offsetX
                : screenW / 2 - hotbarHalfWidth - ICON_SIZE - 8 + CONFIG.offsetX;
        int y = screenH - 22;

        drawIconWithBar(ctx, client, main, x, y);
    }

    private void drawIconWithBar(DrawContext ctx, MinecraftClient client, ItemStack stack, int x, int y) {
        int max = stack.getMaxDamage();
        int dmg = stack.getDamage();
        int remaining = max - dmg;
        float percent = max == 0 ? 100f : (remaining / (float) max) * 100f;
        int color = percent > 50 ? 0xFF55FF55 : percent > 20 ? 0xFFFFAA00 : 0xFFFF5555;

        // Ve icon item (dung API item render chuan)
        ctx.drawItem(stack, x, y);

        // Ve thanh do ben tuy chinh (rong 13px giong style vanilla, cao 2px) ngay duoi icon
        int barWidth = 13;
        int barX = x + 1;
        int barY = y + ICON_SIZE - 2;
        ctx.fill(barX, barY, barX + barWidth, barY + BAR_HEIGHT, 0xFF000000); // nen den
        int filled = Math.round(barWidth * (percent / 100f));
        ctx.fill(barX, barY, barX + filled, barY + BAR_HEIGHT - 1, color);

        if (percent <= CONFIG.warnThresholdPercent) {
            if (CONFIG.soundEnabled && !warnedThisSession.contains(stack)) {
                client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.ENTITY_ITEM_BREAK, 1.0f));
                warnedThisSession.add(stack);
            }
        } else {
            warnedThisSession.remove(stack);
        }
    }
}
