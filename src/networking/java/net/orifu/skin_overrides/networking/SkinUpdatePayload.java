package net.orifu.skin_overrides.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.orifu.skin_overrides.Mod;

import java.util.Optional;

public record SkinUpdatePayload(Optional<String> skinValue, Optional<String> signature) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SkinUpdatePayload> TYPE = new CustomPacketPayload.Type<>(Mod.res("update_skin"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SkinUpdatePayload> PACKET_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), SkinUpdatePayload::skinValue,
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), SkinUpdatePayload::signature,
            SkinUpdatePayload::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
