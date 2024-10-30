package net.orifu.skin_overrides.mixin;

//? if >=1.19.4 {
import com.mojang.authlib.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.net.Proxy;

@Mixin(value = MinecraftClient.class, remap = false)
public interface YggdrasilServiceClientAccessor {
    @Accessor
    String getAccessToken();

    @Accessor
    Proxy getProxy();
}
//?} else
/*public interface YggdrasilServiceClientAccessor {}*/
