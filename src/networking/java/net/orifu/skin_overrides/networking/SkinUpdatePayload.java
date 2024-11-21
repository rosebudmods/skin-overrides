package net.orifu.skin_overrides.networking;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.payload.CustomPayload;
import net.orifu.skin_overrides.Mod;

import java.util.Optional;

public record SkinUpdatePayload(Optional<String> skinValue, Optional<String> signature) implements CustomPayload {
    public static final CustomPayload.Id<SkinUpdatePayload> ID = new CustomPayload.Id<>(Mod.res("update_skin"));
    public static final PacketCodec<RegistryByteBuf, SkinUpdatePayload> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.optional(PacketCodecs.STRING), SkinUpdatePayload::skinValue,
            PacketCodecs.optional(PacketCodecs.STRING), SkinUpdatePayload::signature,
            SkinUpdatePayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
