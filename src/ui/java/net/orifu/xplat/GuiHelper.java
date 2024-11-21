package net.orifu.xplat;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

public class GuiHelper {
    public static EditBox editBox(Font font, int width, int height, Component message) {
        return new EditBox(font, 0, 0, width, height, message);
    }
}
