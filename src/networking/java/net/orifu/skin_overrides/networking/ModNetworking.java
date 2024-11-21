package net.orifu.skin_overrides.networking;

import com.mojang.authlib.properties.Property;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.orifu.skin_overrides.Mod;
import net.orifu.skin_overrides.Skin;
import net.orifu.skin_overrides.util.ProfileHelper;

import java.util.Collections;
import java.util.Optional;

public class ModNetworking {
    public static final String DEFAULT_TEXTURES_KEY = "skin_overrides-default_textures";

    public static void init() {
        // register the skin update payload packet
        PayloadTypeRegistry.playC2S().register(SkinUpdatePayload.TYPE, SkinUpdatePayload.PACKET_CODEC);

        // listen for the packet
        ServerPlayNetworking.registerGlobalReceiver(SkinUpdatePayload.TYPE, (payload, ctx) -> {
            Mod.LOGGER.debug("received packet; {} changed skin:\nval: {}\nsig: {}",
                    ctx.player().getGameProfile().getName(),
                    payload.skinValue(), payload.signature());

            ServerPlayer player = ctx.player();
            PlayerList playerList = ctx.server().getPlayerList();

            // store default textures
            var properties = player.getGameProfile().getProperties();
            if (!properties.containsKey(DEFAULT_TEXTURES_KEY) && properties.containsKey("textures")) {
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
            playerList.broadcastAll(new ClientboundPlayerInfoRemovePacket(Collections.singletonList(ctx.player().getUUID())));
            playerList.broadcastAll(ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(Collections.singleton(player)));

            // update skin for players that are tracking this player
            var tracker = player.serverLevel().getChunkSource().chunkMap.entityMap.get(player.getId());
            tracker.seenBy.forEach(listener -> tracker.serverEntity.addPairing(listener.getPlayer()));
        });
    }

    public static void initClient() {
        // send packet when joining (if applicable)
        ClientPlayConnectionEvents.INIT.register((handler, client) -> {
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
        removeUserNetworkOverride();
    }

    public static void removeUserNetworkOverride() {
        // if a player leaves a local server with an override, they will keep
        // their modified GameProfile. we can reset it to the default when
        // overrides are cleared.
        var properties = ProfileHelper.user().getProperties();
        properties.get(DEFAULT_TEXTURES_KEY).stream().findFirst().ifPresent(textures -> {
            properties.removeAll("textures");
            properties.put("textures", new Property("textures",
                    textures.value(), textures.signature()));
        });
    }

    public static boolean isOnSkinOverridesServer() {
        return ClientPlayNetworking.canSend(SkinUpdatePayload.TYPE);
    }
}
