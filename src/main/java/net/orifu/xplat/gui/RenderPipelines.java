package net.orifu.xplat.gui;

//? if >=1.21.6 {
import com.mojang.blaze3d.pipeline.RenderPipeline;
//?} else if >=1.21.3 {
/*import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;
*///?}

public class RenderPipelines {
    //? if >=1.21.6 {
    public static final RenderPipeline GUI_TEXTURED = net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED;
    //?} else if >=1.21.3
    /*public static final Function<ResourceLocation, RenderType> GUI_TEXTURED = RenderType::guiTextured;*/
}
