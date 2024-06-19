package net.orifu.skin_overrides.screen;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.layout.FrameWidget;
import net.minecraft.client.gui.widget.layout.LayoutSettings;
import net.minecraft.client.gui.widget.layout.LinearLayoutWidget;
import net.minecraft.client.gui.widget.text.TextWidget;
import net.minecraft.text.Text;
import net.orifu.skin_overrides.util.PlayerSkinRenderer;

public class LibraryScreen extends Screen {
    private static final Text TITLE = Text.translatable("skin_overrides.library.title");
    private static final int HEADER_HEIGHT = 40;
    private static final int PREVIEW_SCALE = 4;

    @Nullable
    private final Screen parent;

    private FrameWidget header;
    private LibraryListWidget libraryList;
    @Nullable
    private FrameWidget entryPreviewFrame;

    @Nullable
    protected LibraryListEntry selectedEntry;

    public LibraryScreen(@Nullable Screen parent) {
        super(TITLE);

        this.parent = parent;
    }

    @Override
    protected void init() {
        if (this.libraryList == null) {
            this.libraryList = new LibraryListWidget(this);
        }

        this.libraryList.setPosition(0, HEADER_HEIGHT);
        this.libraryList.setDimensions(this.selectedEntry == null
                ? this.width
                : this.width * 2 / 3,
                this.height - HEADER_HEIGHT);

        var root = LinearLayoutWidget.createVertical();

        this.header = root.add(new FrameWidget(this.width, HEADER_HEIGHT));
        this.header.add(new TextWidget(TITLE, this.textRenderer));

        var body = root.add(LinearLayoutWidget.createHorizontal());
        body.add(this.libraryList);

        if (this.selectedEntry != null) {
            int width = this.width / 3;
            var controlsFrame = body.add(new FrameWidget(width, 0));
            var controls = controlsFrame.add(LinearLayoutWidget.createVertical()).setSpacing(8);

            // name input
            controls.add(new TextFieldWidget(this.textRenderer, Math.min(width - 16, 200), 20,
                    Text.translatable("skin_overrides.library.input.name")))
                    .setText(this.selectedEntry.entry.getName());

            // library entry preview
            this.entryPreviewFrame = controls.add(new FrameWidget(
                    PlayerSkinRenderer.WIDTH * PREVIEW_SCALE,
                    PlayerSkinRenderer.HEIGHT * PREVIEW_SCALE),
                    LayoutSettings.create().alignHorizontallyCenter());
        }

        root.visitWidgets(this::addDrawableSelectableElement);
        root.arrangeElements();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);

        if (this.selectedEntry != null) {
            var texture = this.selectedEntry.entry.getTexture();
            var model = this.selectedEntry.entry.getModel();
            PlayerSkinRenderer.draw(graphics, texture, model,
                    this.entryPreviewFrame.getX(), this.entryPreviewFrame.getY(), PREVIEW_SCALE);
        }
    }

    @Override
    public void closeScreen() {
        this.client.setScreen(this.parent);
    }

    public void selectEntry(LibraryListEntry entry) {
        this.selectedEntry = entry;
        this.clearAndInit();
    }
}
