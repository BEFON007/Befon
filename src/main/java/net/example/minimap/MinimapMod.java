package net.example.minimap;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;

public class MinimapMod implements ClientModInitializer {
    private static KeyBinding toggleKey;
    private static int mode = 1;
    private static final int MAP_SIZE = 80;

    @Override
    public void onInitializeClient() {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "Настройка миникарты", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_B, "Minimap"));

        HudRenderCallback.EVENT.register((context, tickDelta) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || client.options.hudHidden) return;
            while (toggleKey.wasPressed()) { mode = (mode + 1) % 5; }
            if (mode == 0) return;
            int screenW = client.getWindow().getScaledWidth();
            int screenH = client.getWindow().getScaledHeight();
            int x = (mode == 1 || mode == 3) ? screenW - MAP_SIZE - 10 : 10;
            int y = (mode == 3 || mode == 4) ? screenH - MAP_SIZE - 25 : 10;
            renderMinimap(context, client, x, y);
        });
    }

    private void renderMinimap(DrawContext context, MinecraftClient client, int x, int y) {
        BlockPos pos = client.player.getBlockPos();
        context.fill(x, y, x + MAP_SIZE, y + MAP_SIZE, 0x99000000);
        context.drawBorder(x - 1, y - 1, MAP_SIZE + 2, MAP_SIZE + 2, 0xFFFFFFFF);
        for (PlayerEntity other : client.world.getPlayers()) {
            if (other == client.player) continue;
            int dx = (other.getBlockX() - pos.getX()) + (MAP_SIZE / 2);
            int dz = (other.getBlockZ() - pos.getZ()) + (MAP_SIZE / 2);
            if (dx >= 0 && dx < MAP_SIZE && dz >= 0 && dz < MAP_SIZE) {
                int color = 0xFFFFFFFF;
                ItemStack helmet = other.getInventory().getArmorStack(3);
                if (helmet.getItem().toString().contains("diamond")) color = 0xFF00FFFF;
                context.fill(x + dx - 2, y + dz - 2, x + dx + 2, y + dz + 2, color);
                if (other.isSneaking() || other.isInvisible()) context.drawBorder(x + dx - 3, y + dz - 3, 6, 6, 0xFFFF0000);
            }
        }
        String coords = pos.getX() + ", " + pos.getY() + ", " + pos.getZ();
        context.drawText(client.textRenderer, coords, x, y + MAP_SIZE + 5, 0xFFFFFF, true);
    }
}
