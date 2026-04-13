package dev.anvilcraft.lib.v2.wheel.api;

import net.minecraft.client.gui.GuiGraphics;
import org.joml.Matrix3x2fStack;

@FunctionalInterface
public interface WheelEntryRenderer {
    void render(GuiGraphics graphics, Matrix3x2fStack pose, int width, int height);
}

