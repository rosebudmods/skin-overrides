package net.orifu.skin_overrides.networking;

import com.mojang.authlib.properties.Property;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.players.PlayerList;
import net.orifu.skin_overrides.Mod;
import net.orifu.skin_overrides.Skin;
import net.orifu.skin_overrides.util.ProfileHelper;

//? if >=1.20.6 {
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
//?} else {
/*import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;
*///?}

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ModNetworking {
    public static final String DEFAULT_TEXTURES_KEY = "skin_overrides-default_textures";

    public static void init() {
        // register the skin update payload packet
        //? if >=1.20.6
        PayloadTypeRegistry.playC2S().register(SkinUpdatePayload.TYPE, SkinUpdatePayload.PACKET_CODEC);

        // listen for the packet
        //? if >=1.20.6 {
        ServerPlayNetworking.registerGlobalReceiver(SkinUpdatePayload.TYPE, (payload, ctx) -> {
            var player = ctx.player();
            var skinValue = payload.skinValue();
            var signature = payload.signature();

            PlayerList playerList = ctx.server().getPlayerList();
        //?} else {
        /*ServerPlayNetworking.registerGlobalReceiver(Mod.res("update_skin"), (srv, player, lstn, buf, sender) -> {
            var skinValue = buf.readOptional(FriendlyByteBuf::readUtf);
            var signature = buf.readOptional(FriendlyByteBuf::readUtf);

            PlayerList playerList = srv.getPlayerList();
        *///?}

            Mod.LOGGER.debug("received packet; {} changed skin:\nval: {}\nsig: {}",
                    player.getGameProfile().getName(), skinValue, signature);

            // store default textures
            var properties = player.getGameProfile().getProperties();
            if (!properties.containsKey(DEFAULT_TEXTURES_KEY) && properties.containsKey("textures")) {
                var defaultTextures = properties.get("textures").stream().findFirst().orElseThrow();
                properties.put(DEFAULT_TEXTURES_KEY, new Property(DEFAULT_TEXTURES_KEY,
                        propValue(defaultTextures), propSig(defaultTextures)));
            }

            // set skin
            properties.removeAll("textures");
            if (skinValue.isPresent() && signature.isPresent()) {
                Mod.LOGGER.debug("using new textures property");
                properties.put("textures", new Property("textures",
                        skinValue.get(), signature.get()));
            } else {
                // restore default textures
                properties.get(DEFAULT_TEXTURES_KEY).stream().findFirst().ifPresent(textures ->
                    properties.put("textures", new Property("textures",
                            propValue(textures), propSig(textures))));
            }

            Mod.LOGGER.debug("texture properties:\nprofile textures:                        {}\ndefault textures: {}",
                    properties.get("textures"), properties.get(DEFAULT_TEXTURES_KEY));

            // remove and re-add player (updates skin in tab list)
            for (var notifyPlayer : playerList.getPlayers()) {
                // do not notify the player of their own skin update
                if (notifyPlayer.equals(player)) {
                    continue;
                }

                notifyPlayer.connection.send(new ClientboundPlayerInfoRemovePacket(Collections.singletonList(player.getUUID())));
                notifyPlayer.connection.send(ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(Collections.singleton(player)));
            }

            // update skin for players that are tracking this player
            var level = /*? if >=1.20.1 {*/ player.serverLevel() /*?} else >>*/ /*player.level*/ ;
            var tracker = ((ServerChunkCache) level.getChunkSource()).chunkMap.entityMap.get(player.getId());
            tracker.seenBy.forEach(listener -> tracker.serverEntity.addPairing(listener.getPlayer()));
        });
    }

    private static String propValue(Property property) {
        return /*? if >=1.20.2 {*/ property.value() /*?} else >>*/ /*property.getValue()*/ ;
    }

    private static String propSig(Property property) {
        return /*? if >=1.20.2 {*/ property.signature() /*?} else >>*/ /*property.getSignature()*/ ;
    }

    public static void initClient() {
        // send packet when joining (if applicable)
        ClientPlayConnectionEvents.INIT.register((handler, client) -> {
            Mod.SKINS.get(ProfileHelper.user()).ifPresent(ModNetworking::updateSkinOnServer);
        });
    }

    public static CompletableFuture<Optional<Skin.Signature>> updateSkinOnServer(String skinValue, String signature) {
        return updateSkinOnServer(() -> CompletableFuture.completedFuture(Optional.of(new Skin.Signature(skinValue, signature))));
    }

    public static CompletableFuture<Optional<Skin.Signature>> updateSkinOnServer(Skin.Signature.Provider signatureProvider) {
        if (isOnSkinOverridesServer()) {
            Mod.LOGGER.debug("updating skin on server");

            return signatureProvider.signature().thenApply(sig -> {
                Mod.LOGGER.debug("received signature\n{}", sig);
                sig.ifPresent(ModNetworking::updateSkinOnServer);
                return sig;
            });
        }

        return CompletableFuture.completedFuture(Optional.empty());
    }

    private static void updateSkinOnServer(Skin.Signature sig) {
        Mod.LOGGER.debug("updating skin on server with signature:\n{}\n{}", sig.value(), sig.signature());

        //? if >=1.20.6 {
        ClientPlayNetworking.send(new SkinUpdatePayload(
                Optional.ofNullable(sig.value()), Optional.ofNullable(sig.signature())));
        //?} else {
        /*var buf = PacketByteBufs.create();
        buf.writeOptional(Optional.ofNullable(sig.value()), FriendlyByteBuf::writeUtf);
        buf.writeOptional(Optional.ofNullable(sig.signature()), FriendlyByteBuf::writeUtf);
        ClientPlayNetworking.send(Mod.res("update_skin"), buf);
        *///?}
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
                    propValue(textures), propSig(textures)));
        });
    }

    public static boolean isOnSkinOverridesServer() {
        return ClientPlayNetworking.canSend(SkinUpdatePayload.TYPE);
    }
}
