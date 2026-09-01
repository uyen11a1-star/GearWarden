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
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class GearWardenClient implements ClientModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("gearwarden");
    public static GearWardenConfig CONFIG;
    private final Set<ItemStack> warnedThisSession = new HashSet<>();
    private int debugTick = 0;

    @Override
    public void onInitializeClient() {
        CONFIG = GearWardenConfig.load();
        LOGGER.info("[GearWarden] Client initialized, config loaded");

        HudRenderCallback.EVENT.register((DrawContext ctx, RenderTickCounter tickCounter) -> {
            debugTick++;
            if (debugTick % 100 == 0) {
                LOGGER.info("[GearWarden] HUD render callback firing, tick={}", debugTick);
            }

            MinecraftClient client = MinecraftClient.getInstance();
            PlayerEntity player = client.player;
            if (player == null) return;

            // DEBUG: ve o do choe goc tren trai de test callback co chay + ve duoc khong
            ctx.fill(10, 10, 60, 60, 0xFFFF0000);
            ctx.drawText(client.textRenderer, Text.literal("GW TEST"), 10, 65, 0xFFFFFFFF, true);

            if (client.options.hudHidden) return;
            renderArmorHud(ctx, client, player);
            renderHeldToolDurability(ctx, client, player);
        });
    }

    private void renderArmorHud(DrawContext ctx, MinecraftClient client, PlayerEntity player) {
        EquipmentSlot[] slots = {
                EquipmentSlot.HEAD, EquipmentSlot.CHEST,
                EquipmentSlot.LEGS, EquipmentSlot.FEET
        };
        int screenH = client.getWindow().getScaledHeight();
        int baseX = CONFIG.armorSide.equals("left")
                ? CONFIG.offsetX
                : client.getWindow().getScaledWidth() - 70 - CONFIG.offsetX;
        int y = screenH / 2 - 40;

        for (EquipmentSlot slot : slots) {
            ItemStack stack = player.getEquippedStack(slot);
            if (stack.isEmpty() || !stack.isDamageable()) {
                y += 18;
                continue;
            }
            drawDurabilityLine(ctx, client, stack, baseX, y);
            y += 18;
        }
    }

    private void renderHeldToolDurability(DrawContext ctx, MinecraftClient client, PlayerEntity player) {
        ItemStack main = player.getMainHandStack();
        if (main.isEmpty() || !main.isDamageable()) return;
        int screenH = client.getWindow().getScaledHeight();
        int x = CONFIG.toolSide.equals("right")
                ? client.getWindow().getScaledWidth() - 70 - CONFIG.offsetX
                : CONFIG.offsetX;
        int y = screenH / 2 + 30;
        drawDurabilityLine(ctx, client, main, x, y);
    }

    private void drawDurabilityLine(DrawContext ctx, MinecraftClient client, ItemStack stack, int x, int y) {
        int max = stack.getMaxDamage();
        int dmg = stack.getDamage();
        int remaining = max - dmg;
        float percent = max == 0 ? 100f : (remaining / (float) max) * 100f;
        int color = percent > 50 ? 0xFF55FF55 : percent > 20 ? 0xFFFFAA00 : 0xFFFF5555;
        String label = stack.getName().getString();
        String text = CONFIG.showPercent
                ? String.format("%s: %.0f%%", label, percent)
                : label + ": " + remaining + "/" + max;
        ctx.drawText(client.textRenderer, Text.literal(text), x, y, color, true);

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
