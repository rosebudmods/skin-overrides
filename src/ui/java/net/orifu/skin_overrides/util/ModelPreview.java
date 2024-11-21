package net.orifu.skin_overrides.util;

import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.orifu.skin_overrides.Mod;
import net.orifu.skin_overrides.Skin;
import net.orifu.xplat.Lighting;
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
    protected final PlayerModel wide;
    protected final PlayerModel slim;
    protected final PlayerCapeModel cape;
    protected final ElytraModel elytra;
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

    public ModelPreview(@Nullable Skin skin, int scale, Minecraft client) {
        this.skin = skin;
        this.scale = scale;

        var models = client.getEntityModels();

        //? if >=1.21.3 {
        this.wide = new PlayerModel(models.bakeLayer(ModelLayers.PLAYER), false);
        this.slim = new PlayerModel(models.bakeLayer(ModelLayers.PLAYER_SLIM), true);
        this.cape = new PlayerCapeModel(models.bakeLayer(ModelLayers.PLAYER_CAPE));
        this.elytra = new ElytraModel(models.bakeLayer(ModelLayers.ELYTRA));
        //?} else {
        /*this.wide = new PlayerEntityModel<>(models.getModelPart(EntityModelLayers.PLAYER), false);
        this.slim = new PlayerEntityModel<>(models.getModelPart(EntityModelLayers.PLAYER_SLIM), true);
        this.cape = new PlayerCapeModel<>(PlayerCapeModel.getTexturedModelData().createModel());
        this.elytra = new ElytraEntityModel<>(models.getModelPart(EntityModelLayers.ELYTRA));

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

    public void setCape(@Nullable ResourceLocation texture) {
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
        this.pitch = Mth.clamp(pitch, -MAX_PITCH, MAX_PITCH);
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

        graphics.pose().pushPose();
        graphics.pose().translate(x + this.width() / 2.0, y + this.height(), 100);
        float scale = this.height() / MODEL_HEIGHT;
        graphics.pose().scale(scale, scale, scale);
        graphics.pose().translate(0, -MODEL_Y_OFFSET, 0);
        graphics.pose().rotateAround(Axis.XP.rotationDegrees(this.pitch), 0, -MODEL_HEIGHT / 2, 0);
        //? if >=1.20.6 {
        graphics.pose().mulPose(Axis.YP.rotationDegrees(this.yaw));
        //?} else
        /*graphics.getMatrices().multiply(Axis.Y_POSITIVE.rotationDegrees(this.yaw));*/
        graphics.flush();

        Lighting.setupForEntityInInventory(Axis.XP.rotationDegrees(this.pitch));
        graphics.pose().pushPose();
        //? if >=1.20.6 {
        graphics.pose().scale(1, 1, -1);
        //?} else
        /*graphics.getMatrices().multiplyMatrix(new org.joml.Matrix4f().scale(1, 1, -1));*/
        graphics.pose().translate(0, -1.5, 0);
        this.render(graphics);
        graphics.pose().popPose();
        graphics.flush();

        Lighting.setupFor3DItems();
        graphics.pose().popPose();
    }

    protected void render(GuiGraphics graphics) {
        var model = this.skin.model().equals(Skin.Model.WIDE) ? this.wide : this.slim;

        if (this.showSkin) {
            RenderType type = model.renderType(this.skin.texture());
            renderModel(model, type, graphics);
        }

        ResourceLocation cape = this.skin.capeTexture();
        if (cape != null && this.showCape && !this.showElytra) {
            RenderType capeType = this.cape.renderType(cape);
            renderModel(this.cape, capeType, graphics);
        } else if (this.showElytra) {
            ResourceLocation elytra = Optional.ofNullable(this.skin.elytraTexture())
                    .or(() -> Optional.ofNullable(this.skin.capeTexture()))
                    .orElse(Mod.defaultId("textures/entity/elytra.png"));
            RenderType elytraType = model.renderType(elytra);
            renderModel(this.elytra, elytraType, graphics);
        }
    }

    private static void renderModel(EntityModel<?> model, RenderType type, GuiGraphics graphics) {
        //? if >=1.21.3 {
        graphics.drawSpecial(bufferSource ->
            model.renderToBuffer(graphics.pose(), bufferSource.getBuffer(type), 0xf000f0, OverlayTexture.NO_OVERLAY));
        //?} else if >=1.21 {
        /*model.method_60879(graphics.getMatrices(), graphics.getVertexConsumers().getBuffer(layer), 0xf000f0, OverlayTexture.DEFAULT_UV);
        *///?} else
        /*model.render(graphics.getMatrices(), graphics.getVertexConsumers().getBuffer(layer), 0xf000f0, OverlayTexture.DEFAULT_UV, 1, 1, 1, 1);*/
    }
}
