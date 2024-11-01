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
    public static final String DEFAULT_TEXTURES_KEY = "skin_overrides-default_textures";

    public static void init() {
        // register the skin update payload packet
        PayloadTypeRegistry.playC2S().register(SkinUpdatePayload.ID, SkinUpdatePayload.PACKET_CODEC);

        // listen for the packet
        ServerPlayNetworking.registerGlobalReceiver(SkinUpdatePayload.ID, (payload, ctx) -> {
            Mod.LOGGER.debug("received packet; {} changed skin:\nval: {}\nsig: {}",
                    ctx.player().getProfileName(),
                    payload.skinValue(), payload.signature());

            ServerPlayerEntity player = ctx.player();
            PlayerManager playerManager = ctx.server().getPlayerManager();

            // store default textures
            var properties = player.getGameProfile().getProperties();
            if (!properties.containsKey(DEFAULT_TEXTURES_KEY)) {
                var defaultTextures = properties.get("textures").stream().findFirst().orElseThrow();
                properties.put(DEFAULT_TEXTURES_KEY, new Property(DEFAULT_TEXTURES_KEY,
                        defaultTextures.value(), defaultTextures.signature()));
            }

            // set skin
            properties.removeAll("textures");
            if (payload.skinValue().isPresent() && payload.signature().isPresent()) {
                Mod.LOGGER.debug("using new textures property");
                properties.put("textures", new Property("textures",
                        payload.skinValue().get(), payload.signature().get()));
            } else {
                // restore default textures
                properties.get(DEFAULT_TEXTURES_KEY).stream().findFirst().ifPresent(textures ->
                    properties.put("textures", new Property("textures",
                            textures.value(), textures.signature())));
            }

            Mod.LOGGER.debug("texture properties:\nprofile textures:                        {}\ndefault textures: {}",
                    properties.get("textures"), properties.get(DEFAULT_TEXTURES_KEY));

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
            Mod.SKINS.get(ProfileHelper.user()).ifPresent(ModNetworking::updateSkinOnServer);
        });
    }

    public static void updateSkinOnServer(String skinValue, String signature) {
        updateSkinOnServer(() -> Optional.of(new Skin.Signature(skinValue, signature)));
    }

    public static void updateSkinOnServer(Skin.Signature.Provider signatureProvider) {
        if (isOnSkinOverridesServer()) {
            Mod.LOGGER.debug("updating skin on server");

            signatureProvider.signature().ifPresent(sig ->
                ClientPlayNetworking.send(new SkinUpdatePayload(
                        Optional.ofNullable(sig.value()), Optional.ofNullable(sig.signature())))
            );
        }
    }

    public static void clearSkinOverrideOnServer() {
        updateSkinOnServer(null, null);
    }

    public static boolean isOnSkinOverridesServer() {
        return ClientPlayNetworking.canSend(SkinUpdatePayload.ID);
    }
}
