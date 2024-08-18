package net.orifu.skin_overrides.util;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class PlayerCapeModel<T extends LivingEntity> extends BipedEntityModel<T> {
    private final ModelPart cape = this.body.getChild("cape");

    public PlayerCapeModel(ModelPart root) {
        super(root);

        this.animate(0);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData data = BipedEntityModel.getModelData(Dilation.NONE, 0.0f);
        ModelPartData root = data.getRoot();

        // remove unneeded parts
        var clear1 = ModelPartBuilder.create();
        var clear2 = ModelTransform.NONE;
        root.addChild("head", clear1, clear2);
        root.addChild("hat", clear1, clear2);
        var body = root.addChild("body", clear1, clear2);
        root.addChild("left_arm", clear1, clear2);
        root.addChild("right_arm", clear1, clear2);
        root.addChild("left_leg", clear1, clear2);
        root.addChild("right_leg", clear1, clear2);

        body.addChild("cape", ModelPartBuilder.create().uv(0, 0).cuboid(-5f, 0f, 0f, 10f, 16f, 1f, Dilation.NONE, 1f, 0.5f), ModelTransform.of(0f, 0f, 3f, 0f, (float) Math.PI, 0f));
        return TexturedModelData.of(data, 64, 64);
    }

    protected void animate(float delta) {
        float capeLean = 0f;
        float capeLean2 = 0f;
        float capeFlap = 0f;
        float pi = (float) Math.PI;

        var rotation = new Quaternionf()
                .rotateX((6 + capeLean / 2 + capeFlap) * pi / 180)
                .rotateZ(capeLean2 / 2 * pi / 180)
                .rotateY(-capeLean2 / 2 * pi / 180);

        var existing = new Quaternionf().rotateZYX(this.cape.roll, this.cape.yaw, this.cape.pitch);
        var newRotation = existing.mul(rotation);
        var newVector = newRotation.getEulerAnglesXYZ(new Vector3f());
        this.cape.setAngles(newVector.x, newVector.y, newVector.z);
    }
}
