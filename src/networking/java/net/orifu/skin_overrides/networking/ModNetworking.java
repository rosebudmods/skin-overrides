package net.orifu.skin_overrides.networking;

import com.mojang.authlib.properties.Property;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.packet.s2c.PlayerRemovalS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Collections;

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
            player.getGameProfile().getProperties().put(
                    "textures",
                    new Property("textures", payload.skinValue(), payload.signature()));

            // remove and re-add player (updates skin in tab list)
            playerManager.sendToAll(new PlayerRemovalS2CPacket(Collections.singletonList(ctx.player().getUuid())));
            playerManager.sendToAll(PlayerListS2CPacket.create(Collections.singleton(player)));

            // update skin for players that are tracking this player
            var tracker = player.getServerWorld().getChunkManager().delegate.entityTrackers.get(player.getId());
            tracker.listeners.forEach(listener -> tracker.entry.startTracking(listener.getPlayer()));
        });
    }
}
