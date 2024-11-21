package net.orifu.xplat;

public class Lighting extends com.mojang.blaze3d.platform.Lighting {
    //? if <1.20.6 {
    /*private static final Vector3f INVENTORY_LIGHT_0 = new Vector3f(0.2f, -1, -1).normalize();
    private static final Vector3f INVENTORY_LIGHT_1 = new Vector3f(-0.2f, -1, 0).normalize();

    public static void setupInventoryShaderLighting(Quaternionf quaternion) {
        RenderSystem.setShaderLights(
                quaternion.transform(INVENTORY_LIGHT_0, new Vector3f()),
                quaternion.transform(INVENTORY_LIGHT_1, new Vector3f()));
    }
    *///?}
}
