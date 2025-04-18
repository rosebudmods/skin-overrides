package net.orifu.skin_overrides.util;

//? if >=1.21.3 {
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;

public class PlayerCapeModel extends net.minecraft.client.model.PlayerCapeModel<PlayerRenderState> {
    public PlayerCapeModel(ModelPart modelPart) {
        super(modelPart);

        this.setupAnim(new PlayerRenderState());
    }
}
//?} else {
/*import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class PlayerCapeModel<T extends LivingEntity> extends HumanoidModel<T> {
    private final ModelPart cape = this.body.getChild("cape");

    public PlayerCapeModel(ModelPart root) {
        super(root);

        this.animate(0);
    }

    public static LayerDefinition getLayerDefinition() {
        MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0f);
        PartDefinition root = mesh.getRoot();

        // remove unneeded parts
        var clear1 = CubeListBuilder.create();
        var clear2 = PartPose.ZERO;
        root.addOrReplaceChild("head", clear1, clear2);
        root.addOrReplaceChild("hat", clear1, clear2);
        var body = root.addOrReplaceChild("body", clear1, clear2);
        root.addOrReplaceChild("left_arm", clear1, clear2);
        root.addOrReplaceChild("right_arm", clear1, clear2);
        root.addOrReplaceChild("left_leg", clear1, clear2);
        root.addOrReplaceChild("right_leg", clear1, clear2);

        body.addOrReplaceChild(
                "cape",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-5f, 0f, -1f, 10f, 16f, 1f, CubeDeformation.NONE, 1f, 0.5f),
                PartPose.offsetAndRotation(0f, 0f, 2f, 0f, (float) Math.PI, 0f));
        return LayerDefinition.create(mesh, 64, 64);
    }

    protected void animate(float delta) {
        float capeLean = 0f;
        float capeLean2 = 0f;
        float capeFlap = 0f;
        float pi = (float) Math.PI;

        var rotation = new Quaternionf()
                .rotateY(-pi)
                .rotateX((6 + capeLean / 2 + capeFlap) * pi / 180)
                .rotateZ(capeLean2 / 2 * pi / 180)
                .rotateY((180 - capeLean2 / 2) * pi / 180);

        var existing = new Matrix3f().rotateZYX(this.cape.zRot, this.cape.yRot, this.cape.xRot);
        var newRotation = existing.rotate(rotation);
        var newVector = newRotation.getEulerAnglesZYX(new Vector3f());
        this.cape.setRotation(newVector.x, newVector.y, newVector.z);
    }
}
*///?}
