package net.orifu.skin_overrides.networking;

import com.mojang.authlib.properties.Property;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class ModNetworking {
    public static void init() {
        // register the skin update payload packet
        PayloadTypeRegistry.playC2S().register(SkinUpdatePayload.ID, SkinUpdatePayload.PACKET_CODEC);

        // listen for the packet
        ServerPlayNetworking.registerGlobalReceiver(SkinUpdatePayload.ID, (payload, ctx) -> {
            System.out.println("player " + ctx.player().getProfileName() + " changed skin:");
            System.out.println(" - value: " + payload.skinValue());
            System.out.println(" - signt: " + payload.signature());

            var profile = ctx.player().getGameProfile();
            profile.getProperties().put(
                    "textures",
                    new Property("textures", payload.skinValue(), payload.signature()));
        });
    }
}
