package net.orifu.skin_overrides.util;

import com.mojang.blaze3d.lighting.DiffuseLighting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.model.ElytraEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Axis;
import net.minecraft.util.math.MathHelper;
import net.orifu.skin_overrides.Skin;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ModelPreview {
    @Nullable
    protected Skin skin;

    protected final int scale;

    protected float pitch = -5;
    protected float yaw = -30;

    protected final PlayerEntityModel<?> wide;
    protected final PlayerEntityModel<?> slim;
    protected final PlayerCapeModel<?> cape;
    protected final ElytraEntityModel<?> elytra;

    // see PlayerSkinModelWidget
    protected static final float MODEL_HEIGHT = 2.125f;
    protected static final float MODEL_Y_OFFSET = 0.0625f;
    protected static final float MAX_PITCH = 50;

    public ModelPreview(@Nullable Skin skin, int scale, MinecraftClient client) {
        this.skin = skin;
        this.scale = scale;

        var modelLoader = client.getEntityModelLoader();
        this.wide = new PlayerEntityModel<>(modelLoader.getModelPart(EntityModelLayers.PLAYER), false);
        this.slim = new PlayerEntityModel<>(modelLoader.getModelPart(EntityModelLayers.PLAYER_SLIM), true);
        this.cape = new PlayerCapeModel<>(PlayerCapeModel.getTexturedModelData().createModel());
        this.elytra = new ElytraEntityModel<>(modelLoader.getModelPart(EntityModelLayers.ELYTRA));

        // why is this the default??
        this.wide.child = false;
        this.slim.child = false;
        this.cape.child = false;
        this.elytra.child = false;
    }

    public void setSkin(Skin skin) {
        this.skin = skin;
    }

    public void setCape(@Nullable Identifier texture) {
        if (this.skin == null) {
            this.skin = ProfileHelper.getDefaultSkins()[0];
        }

        this.skin = this.skin.withCape(texture);
    }

    public void setPitch(float pitch) {
        this.pitch = MathHelper.clamp(pitch, -MAX_PITCH, MAX_PITCH);
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public void turn(float angle) {
        this.setYaw(this.yaw + angle);
    }

    public void setPitchAndYaw(float pitch, float yaw) {
        this.setPitch(pitch);
        this.setYaw(yaw);
    }

    public int width() {
        return PlayerSkinRenderer.WIDTH * this.scale;
    }

    public int height() {
        return PlayerSkinRenderer.HEIGHT * this.scale;
    }

    public void spin(float delta) {
        this.setYaw(this.yaw + delta * 4f);
    }

    public void drag(float deltaX, float deltaY) {
        this.setPitchAndYaw(this.pitch - deltaY * 2.5f, this.yaw + deltaX * 2.5f);
    }

    public void draw(GuiGraphics graphics, int x, int y) {
        if (this.skin == null) return;

        graphics.getMatrices().push();
        graphics.getMatrices().translate(x + this.width() / 2.0, y + this.height(), 100);
        float scale = this.height() / MODEL_HEIGHT;
        graphics.getMatrices().scale(scale, scale, scale);
        graphics.getMatrices().translate(0, -MODEL_Y_OFFSET, 0);
        graphics.getMatrices().rotateAround(Axis.X_POSITIVE.rotationDegrees(this.pitch), 0, -MODEL_HEIGHT / 2, 0);
        graphics.getMatrices().rotate(Axis.Y_POSITIVE.rotationDegrees(this.yaw));
        graphics.draw();

        DiffuseLighting.setupInventoryShaderLighting(Axis.X_POSITIVE.rotationDegrees(this.pitch));
        graphics.getMatrices().push();
        graphics.getMatrices().scale(1, 1, -1);
        graphics.getMatrices().translate(0, -1.5, 0);
        this.render(graphics);
        graphics.getMatrices().pop();
        graphics.draw();

        DiffuseLighting.setup3DGuiLighting();
        graphics.getMatrices().pop();
    }

    protected void render(GuiGraphics graphics) {
        var model = this.skin.model().equals(Skin.Model.WIDE) ? this.wide : this.slim;
        RenderLayer layer = model.getLayer(this.skin.texture());
        model.method_60879(graphics.getMatrices(), graphics.getVertexConsumers().getBuffer(layer), 0xf000f0, OverlayTexture.DEFAULT_UV);

        Identifier cape = this.skin.capeTexture();
        if (cape != null) {
            RenderLayer capeLayer = this.cape.getLayer(cape);
            this.cape.method_60879(graphics.getMatrices(), graphics.getVertexConsumers().getBuffer(capeLayer), 0xf000f0, OverlayTexture.DEFAULT_UV);
        }

        // RenderLayer elytraLayer = model.getLayer(Optional.ofNullable(this.skin.elytraTexture()).orElse(Identifier.ofDefault("textures/entity/elytra.png")));
        // this.elytra.method_60879(graphics.getMatrices(), graphics.getVertexConsumers().getBuffer(elytraLayer), 0xf000f0, OverlayTexture.DEFAULT_UV);
    }
}
