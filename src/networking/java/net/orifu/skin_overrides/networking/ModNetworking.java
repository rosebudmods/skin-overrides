package net.orifu.skin_overrides.networking;

import com.mojang.authlib.properties.Property;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.s2c.PlayerRemovalS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.orifu.skin_overrides.Mod;
import net.orifu.skin_overrides.Skin;
import net.orifu.skin_overrides.util.ProfileHelper;

import java.util.Collections;
import java.util.Optional;

public class ModNetworking {
    public static void init() {
        // register the skin update payload packet
        PayloadTypeRegistry.playC2S().register(SkinUpdatePayload.ID, SkinUpdatePayload.PACKET_CODEC);

        // listen for the packet
        ServerPlayNetworking.registerGlobalReceiver(SkinUpdatePayload.ID, (payload, ctx) -> {
            System.out.println("player " + ctx.player().getProfileName() + " changed skin:");
            System.out.println(" - value: " + payload.skinValue());
            System.out.println(" - signt: " + payload.signature());

            ServerPlayerEntity player = ctx.player();
            PlayerManager playerManager = ctx.server().getPlayerManager();

            // set skin
            if (payload.skinValue().isPresent() && payload.signature().isPresent()) {
                player.getGameProfile().getProperties().put("textures", new Property(
                        "textures",
                        payload.skinValue().get(),
                        payload.signature().get()));
            } else {
                player.getGameProfile().getProperties().removeAll("textures");
            }

            // remove and re-add player (updates skin in tab list)
            playerManager.sendToAll(new PlayerRemovalS2CPacket(Collections.singletonList(ctx.player().getUuid())));
            playerManager.sendToAll(PlayerListS2CPacket.create(Collections.singleton(player)));

            // update skin for players that are tracking this player
            var tracker = player.getServerWorld().getChunkManager().delegate.entityTrackers.get(player.getId());
            tracker.listeners.forEach(listener -> tracker.entry.startTracking(listener.getPlayer()));
        });
    }

    public static void initClient() {
        // send packet when joining (if applicable)
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            var override = Mod.SKINS.get(ProfileHelper.user()).orElse(null);

            if (override != null) updateSkinOnServer(override);
        });
    }

    public static void updateSkinOnServer(String skinValue, String signature) {
        updateSkinOnServer(() -> Optional.of(new Skin.Signature(skinValue, signature)));
    }

    public static void updateSkinOnServer(Skin.Signature.Provider signatureProvider) {
        if (ClientPlayNetworking.canSend(SkinUpdatePayload.ID)) {
            signatureProvider.signature().ifPresent(sig ->
                ClientPlayNetworking.send(new SkinUpdatePayload(
                        Optional.ofNullable(sig.value()), Optional.ofNullable(sig.signature())))
            );
        }
    }

    public static void clearSkinOverrideOnServer() {
        var profile = ProfileHelper.user();
        var textures = MinecraftClient.getInstance().getSessionService().getPackedTextures(profile);
        if (textures != null) {
            updateSkinOnServer(textures.value(), textures.signature());
        } else {
            updateSkinOnServer(null, null);
        }
    }
}
