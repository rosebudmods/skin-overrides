package net.orifu.skin_overrides.screen;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.io.FilenameUtils;
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
import net.orifu.skin_overrides.override.Overridden;
import net.orifu.skin_overrides.override.LibraryCapeOverride.CapeEntry;
import net.orifu.skin_overrides.override.LibrarySkinOverride.SkinEntry;
import net.orifu.skin_overrides.util.PlayerCapeRenderer;
import net.orifu.skin_overrides.util.PlayerSkinRenderer;

public class LibraryScreen extends Screen {
    private static final Text TITLE = Text.translatable("skin_overrides.library.title");
    private static final int HEADER_HEIGHT = 40;
    private static final int SKIN_SCALE = 4;
    private static final int CAPE_SCALE = 8;

    private static final int OPTIONS_PAD = 24;

    public final Overridden ov;
    @Nullable
    private final Screen parent;
    @Nullable
    private final Consumer<LibraryEntry> callback;

    private FrameWidget header;
    private LibraryListWidget libraryList;
    @Nullable
    private FrameWidget entryPreviewFrame;
    @Nullable
    private TextFieldWidget nameField;

    @Nullable
    protected LibraryListEntry selectedEntry;

    public LibraryScreen(Overridden ov, @Nullable Screen parent, @Nullable Consumer<LibraryEntry> callback) {
        super(TITLE);

        this.ov = ov;
        this.parent = parent;
        this.callback = callback;
    }

    public LibraryScreen(Overridden ov, @Nullable Screen parent) {
        this(ov, parent, null);
    }

    @Override
    protected void init() {
        if (this.libraryList == null) {
            this.libraryList = new LibraryListWidget(this, this.ov);
        }

        int optionsWidth = Math.min(this.width * 2 / 5 - OPTIONS_PAD, 150);

        this.libraryList.setPosition(0, HEADER_HEIGHT);
        this.libraryList.setDimensions(this.selectedEntry == null
                ? this.width
                : this.width - optionsWidth - OPTIONS_PAD,
                this.height - HEADER_HEIGHT);

        var root = LinearLayoutWidget.createVertical();

        this.header = root.add(new FrameWidget(this.width, HEADER_HEIGHT));
        this.header.add(new TextWidget(TITLE, this.textRenderer));

        var body = root.add(LinearLayoutWidget.createHorizontal());
        body.add(this.libraryList);

        if (this.selectedEntry != null) {
            var controlsFrame = body.add(new FrameWidget(optionsWidth + OPTIONS_PAD, 0));
            var controls = controlsFrame.add(LinearLayoutWidget.createVertical().setSpacing(2));

            // library entry preview
            this.entryPreviewFrame = controls.add(new FrameWidget(
                    this.ov.skin() ? PlayerSkinRenderer.WIDTH * SKIN_SCALE : PlayerCapeRenderer.WIDTH * CAPE_SCALE,
                    this.ov.skin() ? PlayerSkinRenderer.HEIGHT * SKIN_SCALE : PlayerCapeRenderer.HEIGHT * CAPE_SCALE),
                    LayoutSettings.create().alignHorizontallyCenter());

            controls.add(new FrameWidget(0, 16));

            // name input
            if (this.nameField == null) {
                this.nameField = new TextFieldWidget(this.textRenderer, optionsWidth, 20,
                        Text.translatable("skin_overrides.library.input.name"));
                this.nameField.setMaxLength(32);
                this.nameField.setChangedListener(this::renameEntry);
                this.nameField.setText(this.selectedEntry.entry.getName());
            }
            controls.add(this.nameField, LayoutSettings.create().alignHorizontallyCenter());

            var smallControls = controls.add(LinearLayoutWidget.createHorizontal());

            // previous entry
            boolean isFirst = this.selectedEntry.index == 0;
            smallControls.add(ButtonWidget.builder(Text.literal("<"),
                    btn -> this.libraryList.moveSelection(-1)).width(20)
                    .tooltip(Tooltip.create(Text.translatable("skin_overrides.library.input.back")))
                    .build()).active = !isFirst;
            // swap this and previous entry
            smallControls.add(ButtonWidget.builder(Text.literal("<<"), btn -> {
                this.libraryList.swap(this.selectedEntry.index, this.selectedEntry.index - 1);
                this.libraryList.ensureVisible(this.selectedEntry);
                this.clearAndInit();
            }).width(25).tooltip(Tooltip.create(Text.translatable("skin_overrides.library.input.move_back")))
                    .build()).active = !isFirst;

            // use this entry
            int mainControlWidth = (optionsWidth - 40 - 50) / 2;
            smallControls.add(ButtonWidget.builder(Text.literal("+"), btn -> {
                this.callback.accept(this.selectedEntry.entry);
                this.client.setScreen(this.parent);
            }).width(mainControlWidth).tooltip(Tooltip.create(Text.translatable("skin_overrides.library.input.use")))
                    .build()).active = this.callback != null;

            // remove this entry
            smallControls.add(ButtonWidget.builder(Text.literal("-"), btn -> {
                this.libraryList.removeFromLibrary();
            }).width(mainControlWidth).tooltip(Tooltip.create(Text.translatable("skin_overrides.library.input.remove")))
                    .build()).active = this.callback != null;

            // swap this and next entry
            boolean isLast = this.selectedEntry.index == this.libraryList.children().size() - 1;
            smallControls.add(ButtonWidget.builder(Text.literal(">>"), btn -> {
                this.libraryList.swap(this.selectedEntry.index, this.selectedEntry.index + 1);
                this.libraryList.ensureVisible(this.selectedEntry);
                this.clearAndInit();
            }).tooltip(Tooltip.create(Text.translatable("skin_overrides.library.input.move_next"))).width(25)
                    .build()).active = !isLast;
            // next entry
            smallControls.add(ButtonWidget.builder(Text.literal(">"),
                    btn -> this.libraryList.moveSelection(1))
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
            if (this.selectedEntry.entry instanceof SkinEntry entry) {
                var texture = entry.getTexture();
                var model = entry.getModel();
                PlayerSkinRenderer.draw(graphics, texture, model,
                        this.entryPreviewFrame.getX(), this.entryPreviewFrame.getY(), SKIN_SCALE);
            } else {
                var texture = this.selectedEntry.entry.getTexture();
                PlayerCapeRenderer.draw(graphics, texture, this.entryPreviewFrame.getX(), this.entryPreviewFrame.getY(),
                        CAPE_SCALE);
            }
        }

        // empty list text
        if (this.libraryList.children().isEmpty()) {
            graphics.drawCenteredShadowedText(this.textRenderer, Text.translatable("skin_overrides.library.empty"),
                    this.libraryList.getX() + this.libraryList.getWidth() / 2,
                    this.libraryList.getY() + this.libraryList.getHeight() / 2 - 4, 0xaaaaaa);
        }
    }

    @Override
    public void closeScreen() {
        this.client.setScreen(this.parent);
    }

    public void selectEntry(LibraryListEntry entry) {
        this.selectedEntry = entry;
        this.nameField = null;
        this.clearAndInit();
        this.libraryList.ensureVisible(entry);
    }

    public void renameEntry(String newName) {
        if (!newName.equals(this.selectedEntry.entry.getName())) {
            this.ov.library().rename(this.selectedEntry.entry, newName);
            this.clearAndInit();
            this.focusOn(this.nameField);
        }
    }

    @Override
    public void filesDragged(List<Path> paths) {
        if (paths.size() == 0) {
            return;
        }
        Path path = paths.get(0);
        if (!path.toFile().isFile() || !FilenameUtils.isExtension(path.toFile().getName(), "png")) {
            return;
        }
        String guessedName = path.toFile().getName().replace(".png", "").replace("_", " ");

        if (this.ov.skin()) {
            // open name and model input screen
            this.client.setScreen(OverrideInfoEntryScreen.getNameAndModel(this, path, guessedName,
                    (name, model) -> {
                        // add skin
                        SkinEntry.create(name, path, model);
                        this.libraryList.reload();
                        this.clearAndInit();
                    }));
        } else {
            // open name input screen
            this.client.setScreen(OverrideInfoEntryScreen.getName(this, path, guessedName,
                    name -> {
                        // add cape
                        CapeEntry.create(name, path);
                        this.libraryList.reload();
                        this.clearAndInit();
                    }));
        }
    }
}
