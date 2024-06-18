package net.orifu.skin_overrides.screen;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class LibraryScreen extends Screen {
    private static final Text TITLE = Text.translatable("skin_overrides.library.title");

    @Nullable
    private final Screen parent;

    @Nullable
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

        this.addDrawableSelectableElement(this.libraryList);

        this.repositionElements();
    }

    @Override
    public void repositionElements() {
        this.libraryList.setPosition(0, 0);
        this.libraryList.setDimensions(this.width, this.height);
    }

    @Override
    public void closeScreen() {
        this.client.setScreen(this.parent);
    }
}
