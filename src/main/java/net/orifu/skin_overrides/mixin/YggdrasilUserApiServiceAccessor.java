package net.orifu.skin_overrides.mixin;

//? if >=1.19.4 {
import com.mojang.authlib.Environment;
import com.mojang.authlib.minecraft.client.MinecraftClient;
import com.mojang.authlib.yggdrasil.YggdrasilUserApiService;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = YggdrasilUserApiService.class, remap = false)
public interface YggdrasilUserApiServiceAccessor {
    @Accessor
    MinecraftClient getMinecraftClient();

    @Accessor
    Environment getEnvironment();
}
//?} else {
/*public interface YggdrasilUserApiServiceAccessor {}
*///?}
