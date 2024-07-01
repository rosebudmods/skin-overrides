package net.minecraft.client.gui.widget.layout;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.HeaderAndFooterWidget;

public class HeaderFooterLayoutWidget extends HeaderAndFooterWidget {
    public HeaderFooterLayoutWidget(Screen screen) {
        super(screen);
    }

    public HeaderFooterLayoutWidget(Screen screen, int outerSectionsHeight) {
        super(screen, outerSectionsHeight);
    }

    public HeaderFooterLayoutWidget(Screen screen, int headerHeight, int footerHeight) {
        super(screen, headerHeight, footerHeight);
    }
}
