package com.nguyenquochuy.gearwarden.mixin;

import com.nguyenquochuy.gearwarden.GearWardenClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// RUI RO CAO NHAT: ten method "renderArm" va thu tu tham so co the KHAC
// tren Yarn 1.21.10 that. Neu build bao "cannot find symbol" hoac
// "method does not exist" ngay file nay, gui log + chay
// "./gradlew genSources --no-daemon" roi javap class HeldItemRenderer
// de lay dung signature, gui cho tao sua lai.
@Mixin(HeldItemRenderer.class)
public class HeldItemRendererMixin {

    @Inject(method = "renderArm", at = @At("HEAD"), cancellable = true)
    private void gearwarden$hideArmWhenHolding(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, Arm arm, CallbackInfo ci) {
        if (GearWardenClient.CONFIG == null || !GearWardenClient.CONFIG.hideHandWhenHolding) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        ItemStack stack = (arm == client.player.getMainArm())
                ? client.player.getMainHandStack()
                : client.player.getOffHandStack();

        if (!stack.isEmpty()) {
            ci.cancel();
        }
    }
}
