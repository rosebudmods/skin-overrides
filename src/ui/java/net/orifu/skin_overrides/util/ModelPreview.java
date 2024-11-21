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

    protected float xRot = -5;
    protected float yRot = -30;

    protected boolean showSkin = true;
    protected boolean showCape = true;
    protected boolean showElytra = false;

    //? if >=1.21.3 {
    protected final PlayerModel wide;
    protected final PlayerModel slim;
    protected final PlayerCapeModel cape;
    protected final ElytraModel elytra;
    //?} else {
    /*protected final PlayerModel<?> wide;
    protected final PlayerModel<?> slim;
    protected final PlayerCapeModel<?> cape;
    protected final ElytraModel<?> elytra;
    *///?}

    // see PlayerSkinModelWidget
    protected static final float MODEL_HEIGHT = 2.125f;
    protected static final float MODEL_Y_OFFSET = 0.0625f;
    protected static final float MAX_X_ROT = 50;

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
        /*this.wide = new PlayerModel<>(models.bakeLayer(ModelLayers.PLAYER), false);
        this.slim = new PlayerModel<>(models.bakeLayer(ModelLayers.PLAYER_SLIM), true);
        this.cape = new PlayerCapeModel<>(PlayerCapeModel.getLayerDefinition().bakeRoot());
        this.elytra = new ElytraModel<>(models.bakeLayer(ModelLayers.ELYTRA));

        // why is this the default??
        this.wide.young = false;
        this.slim.young = false;
        this.cape.young = false;
        this.elytra.young = false;
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

    public void setXRot(float xRot) {
        this.xRot = Mth.clamp(xRot, -MAX_X_ROT, MAX_X_ROT);
    }

    public void setYRot(float yRot) {
        this.yRot = yRot;
    }

    public void turn(float angle) {
        this.setYRot(this.yRot + angle);
    }

    public void setRotation(float xRot, float yRot) {
        this.setXRot(xRot);
        this.setYRot(yRot);
    }

    public int width() {
        return PlayerSkinRenderer.WIDTH * this.scale;
    }

    public int height() {
        return PlayerSkinRenderer.HEIGHT * this.scale;
    }

    public void spin(float delta) {
        this.setYRot(this.yRot + delta * 4f);
    }

    public void drag(float deltaX, float deltaY) {
        this.setRotation(this.xRot - deltaY * 2.5f, this.yRot + deltaX * 2.5f);
    }

    public void draw(GuiGraphics graphics, int x, int y) {
        if (this.skin == null) return;

        graphics.pose().pushPose();
        graphics.pose().translate(x + this.width() / 2.0, y + this.height(), 100);
        float scale = this.height() / MODEL_HEIGHT;
        graphics.pose().scale(scale, scale, scale);
        graphics.pose().translate(0, -MODEL_Y_OFFSET, 0);
        graphics.pose().rotateAround(Axis.XP.rotationDegrees(this.xRot), 0, -MODEL_HEIGHT / 2, 0);
        //? if >=1.20.6 {
        graphics.pose().mulPose(Axis.YP.rotationDegrees(this.yRot));
        //?} else
        /*graphics.pose().mulPose(Axis.YP.rotationDegrees(this.yRot));*/
        graphics.flush();

        Lighting.setupForEntityInInventory(Axis.XP.rotationDegrees(this.xRot));
        graphics.pose().pushPose();
        //? if >=1.20.6 {
        graphics.pose().scale(1, 1, -1);
        //?} else
        /*graphics.pose().mulPoseMatrix(new org.joml.Matrix4f().scale(1, 1, -1));*/
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
        /*model.renderToBuffer(graphics.pose(), graphics.bufferSource().getBuffer(type), 0xf000f0, OverlayTexture.NO_OVERLAY);
        *///?} else
        /*model.renderToBuffer(graphics.pose(), graphics.bufferSource().getBuffer(type), 0xf000f0, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);*/
    }
}
