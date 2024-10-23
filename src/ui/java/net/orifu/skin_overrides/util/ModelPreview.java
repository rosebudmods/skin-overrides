package net.orifu.skin_overrides.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.model.ElytraEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Axis;
import net.minecraft.util.math.MathHelper;
import net.orifu.skin_overrides.Mod;
import net.orifu.skin_overrides.Skin;
import net.orifu.xplat.DiffuseLighting;
import net.orifu.xplat.gui.GuiGraphics;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ModelPreview {
    @Nullable
    protected Skin skin;

    protected final int scale;

    protected float pitch = -5;
    protected float yaw = -30;

    protected boolean showSkin = true;
    protected boolean showCape = true;
    protected boolean showElytra = false;

    //? if >=1.21.3 {
    protected final PlayerEntityModel wide;
    protected final PlayerEntityModel slim;
    protected final PlayerCapeModel cape;
    protected final ElytraEntityModel elytra;
    //?} else {
    /*protected final PlayerEntityModel<?> wide;
    protected final PlayerEntityModel<?> slim;
    protected final PlayerCapeModel<?> cape;
    protected final ElytraEntityModel<?> elytra;
    *///?}

    // see PlayerSkinModelWidget
    protected static final float MODEL_HEIGHT = 2.125f;
    protected static final float MODEL_Y_OFFSET = 0.0625f;
    protected static final float MAX_PITCH = 50;

    public ModelPreview(@Nullable Skin skin, int scale, MinecraftClient client) {
        this.skin = skin;
        this.scale = scale;

        var modelLoader = client.getEntityModelLoader();

        //? if >=1.21.3 {
        this.wide = new PlayerEntityModel(modelLoader.getModelPart(EntityModelLayers.PLAYER), false);
        this.slim = new PlayerEntityModel(modelLoader.getModelPart(EntityModelLayers.PLAYER_SLIM), true);
        this.cape = new PlayerCapeModel(modelLoader.getModelPart(EntityModelLayers.field_52980));
        this.elytra = new ElytraEntityModel(modelLoader.getModelPart(EntityModelLayers.ELYTRA));
        //?} else {
        /*this.wide = new PlayerEntityModel<>(modelLoader.getModelPart(EntityModelLayers.PLAYER), false);
        this.slim = new PlayerEntityModel<>(modelLoader.getModelPart(EntityModelLayers.PLAYER_SLIM), true);
        this.cape = new PlayerCapeModel<>(PlayerCapeModel.getTexturedModelData().createModel());
        this.elytra = new ElytraEntityModel<>(modelLoader.getModelPart(EntityModelLayers.ELYTRA));

        // why is this the default??
        this.wide.child = false;
        this.slim.child = false;
        this.cape.child = false;
        this.elytra.child = false;
        *///?}
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

    public void showSkin(boolean showSkin) {
        this.showSkin = showSkin;
    }

    public void showCape(boolean showCape) {
        this.showCape = showCape;
    }

    public void showElytra(boolean showElytra) {
        this.showElytra = showElytra;
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
        //? if >=1.20.6 {
        graphics.getMatrices().rotate(Axis.Y_POSITIVE.rotationDegrees(this.yaw));
        //?} else
        /*graphics.getMatrices().multiply(Axis.Y_POSITIVE.rotationDegrees(this.yaw));*/
        graphics.draw();

        DiffuseLighting.setupInventoryShaderLighting(Axis.X_POSITIVE.rotationDegrees(this.pitch));
        graphics.getMatrices().push();
        //? if >=1.20.6 {
        graphics.getMatrices().scale(1, 1, -1);
        //?} else
        /*graphics.getMatrices().multiplyMatrix(new org.joml.Matrix4f().scale(1, 1, -1));*/
        graphics.getMatrices().translate(0, -1.5, 0);
        this.render(graphics);
        graphics.getMatrices().pop();
        graphics.draw();

        DiffuseLighting.setup3DGuiLighting();
        graphics.getMatrices().pop();
    }

    protected void render(GuiGraphics graphics) {
        var model = this.skin.model().equals(Skin.Model.WIDE) ? this.wide : this.slim;

        if (this.showSkin) {
            RenderLayer layer = model.getLayer(this.skin.texture());
            renderModel(model, layer, graphics);
        }

        Identifier cape = this.skin.capeTexture();
        if (cape != null && this.showCape && !this.showElytra) {
            RenderLayer capeLayer = this.cape.getLayer(cape);
            renderModel(this.cape, capeLayer, graphics);
        } else if (this.showElytra) {
            Identifier elytra = Optional.ofNullable(this.skin.elytraTexture())
                    .or(() -> Optional.ofNullable(this.skin.capeTexture()))
                    .orElse(Mod.defaultId("textures/entity/elytra.png"));
            RenderLayer elytraLayer = model.getLayer(elytra);
            renderModel(this.elytra, elytraLayer, graphics);
        }
    }

    private static void renderModel(EntityModel<?> model, RenderLayer layer, GuiGraphics graphics) {
        //? if >=1.21.3 {
        graphics.method_64039(vertexConsumers ->
            model.method_60879(graphics.getMatrices(), vertexConsumers.getBuffer(layer), 0xf000f0, OverlayTexture.DEFAULT_UV));
        //?} else if >=1.21 {
        /*model.method_60879(graphics.getMatrices(), graphics.getVertexConsumers().getBuffer(layer), 0xf000f0, OverlayTexture.DEFAULT_UV);
        *///?} else
        /*model.render(graphics.getMatrices(), graphics.getVertexConsumers().getBuffer(layer), 0xf000f0, OverlayTexture.DEFAULT_UV, 1, 1, 1, 1);*/
    }
}
