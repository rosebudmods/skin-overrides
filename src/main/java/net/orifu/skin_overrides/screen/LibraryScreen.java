package net.orifu.skin_overrides.screen;

import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.ButtonWidget;
import net.minecraft.client.gui.widget.layout.FrameWidget;
import net.minecraft.client.gui.widget.layout.LayoutSettings;
import net.minecraft.client.gui.widget.layout.LinearLayoutWidget;
import net.minecraft.client.gui.widget.text.TextWidget;
import net.minecraft.text.Text;
import net.orifu.skin_overrides.Library.LibraryEntry;
import net.orifu.skin_overrides.util.PlayerSkinRenderer;

public class LibraryScreen extends Screen {
    private static final Text TITLE = Text.translatable("skin_overrides.library.title");
    private static final int HEADER_HEIGHT = 40;
    private static final int PREVIEW_SCALE = 4;

    @Nullable
    private final Screen parent;
    @Nullable
    private final Consumer<LibraryEntry> callback;

    private FrameWidget header;
    private LibraryListWidget libraryList;
    @Nullable
    private FrameWidget entryPreviewFrame;

    @Nullable
    protected LibraryListEntry selectedEntry;

    public LibraryScreen(@Nullable Screen parent, @Nullable Consumer<LibraryEntry> callback) {
        super(TITLE);

        this.parent = parent;
        this.callback = callback;
    }

    public LibraryScreen(@Nullable Screen parent) {
        this(parent, null);
    }

    @Override
    protected void init() {
        if (this.libraryList == null) {
            this.libraryList = new LibraryListWidget(this);
        }

        int optionsWidth = Math.min(this.width * 2 / 5, 200);

        this.libraryList.setPosition(0, HEADER_HEIGHT);
        this.libraryList.setDimensions(this.selectedEntry == null
                ? this.width
                : this.width - optionsWidth,
                this.height - HEADER_HEIGHT);

        var root = LinearLayoutWidget.createVertical();

        this.header = root.add(new FrameWidget(this.width, HEADER_HEIGHT));
        this.header.add(new TextWidget(TITLE, this.textRenderer));

        var body = root.add(LinearLayoutWidget.createHorizontal());
        body.add(this.libraryList);

        if (this.selectedEntry != null) {
            var controlsFrame = body.add(new FrameWidget(optionsWidth, 0));
            var controls = controlsFrame.add(LinearLayoutWidget.createVertical()).setSpacing(8);

            // name input
            controls.add(new TextFieldWidget(this.textRenderer, Math.min(optionsWidth - 16, 150), 20,
                    Text.translatable("skin_overrides.library.input.name")),
                    LayoutSettings.create().alignHorizontallyCenter())
                    .setText(this.selectedEntry.entry.getName());

            // library entry preview
            this.entryPreviewFrame = controls.add(new FrameWidget(
                    PlayerSkinRenderer.WIDTH * PREVIEW_SCALE,
                    PlayerSkinRenderer.HEIGHT * PREVIEW_SCALE),
                    LayoutSettings.create().alignHorizontallyCenter());

            var smallControls = controls.add(LinearLayoutWidget.createHorizontal());

            // previous entry
            boolean isFirst = this.selectedEntry.index == 0;
            smallControls.add(ButtonWidget.builder(Text.literal("<"), btn -> this.previousEntry()).width(20)
                    .tooltip(Tooltip.create(Text.translatable("skin_overrides.library.input.back")))
                    .build()).active = !isFirst;
            // swap this and previous entry
            smallControls.add(ButtonWidget.builder(Text.literal("<<"), btn -> {
            }).width(30).tooltip(Tooltip.create(Text.translatable("skin_overrides.library.input.move_back")))
                    .build()).active = !isFirst;

            // use this entry
            smallControls.add(ButtonWidget.builder(Text.translatable("skin_overrides.library.input.use"), btn -> {
                this.callback.accept(this.selectedEntry.entry);
                this.client.setScreen(this.parent);
            }).width(optionsWidth - 40 - 60 - 20).build()).active = this.callback != null;

            // swap this and next entry
            boolean isLast = this.selectedEntry.index == this.libraryList.children().size() - 1;
            smallControls.add(ButtonWidget.builder(Text.literal(">>"), btn -> {
            }).tooltip(Tooltip.create(Text.translatable("skin_overrides.library.input.move_next"))).width(30)
                    .build()).active = !isLast;
            // next entry
            smallControls.add(ButtonWidget.builder(Text.literal(">"), btn -> this.nextEntry())
                    .tooltip(Tooltip.create(Text.translatable("skin_overrides.library.input.next"))).width(20)
                    .build()).active = !isLast;
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

    public void previousEntry() {
        if (this.selectedEntry == null || this.selectedEntry.index == 0) {
            return;
        }

        this.libraryList.moveSelection(-1);
    }

    public void nextEntry() {
        if (this.selectedEntry == null || this.selectedEntry.index == this.libraryList.children().size() - 1) {
            return;
        }

        this.libraryList.moveSelection(1);
    }
}
