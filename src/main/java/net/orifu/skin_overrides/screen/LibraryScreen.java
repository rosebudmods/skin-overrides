package net.orifu.skin_overrides.screen;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.layout.FrameWidget;
import net.minecraft.client.gui.widget.layout.LinearLayoutWidget;
import net.minecraft.client.gui.widget.text.TextWidget;
import net.minecraft.text.Text;

public class LibraryScreen extends Screen {
    private static final Text TITLE = Text.translatable("skin_overrides.library.title");
    private static final int HEADER_HEIGHT = 40;

    @Nullable
    private final Screen parent;

    private FrameWidget header;
    private LibraryListWidget libraryList;

    public LibraryScreen(@Nullable Screen parent) {
        super(TITLE);

        this.parent = parent;
    }

    @Override
    protected void init() {
        if (this.libraryList == null) {
            this.libraryList = new LibraryListWidget();
        }

        this.libraryList.setPosition(0, HEADER_HEIGHT);
        this.libraryList.setDimensions(this.width * 2 / 3, this.height - HEADER_HEIGHT);

        var root = LinearLayoutWidget.createVertical();

        this.header = root.add(new FrameWidget(this.width, HEADER_HEIGHT));
        this.header.add(new TextWidget(TITLE, this.textRenderer));

        var body = root.add(LinearLayoutWidget.createHorizontal());
        body.add(this.libraryList);

        root.visitWidgets(this::addDrawableSelectableElement);
        root.arrangeElements();
    }

    @Override
    public void closeScreen() {
        this.client.setScreen(this.parent);
    }
}
