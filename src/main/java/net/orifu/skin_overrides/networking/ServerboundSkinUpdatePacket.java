package net.orifu.skin_overrides.networking;

//? if >=1.20.6 {
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.orifu.skin_overrides.Mod;

import java.util.Optional;

public record ServerboundSkinUpdatePacket(Optional<String> skinValue, Optional<String> signature) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ServerboundSkinUpdatePacket> TYPE = new CustomPacketPayload.Type<>(Mod.res("update_skin"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundSkinUpdatePacket> PACKET_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), ServerboundSkinUpdatePacket::skinValue,
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), ServerboundSkinUpdatePacket::signature,
            ServerboundSkinUpdatePacket::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
//?} else {
/*import net.minecraft.resources.ResourceLocation;
import net.orifu.skin_overrides.Mod;

public class ServerboundSkinUpdatePacket {
    public static final ResourceLocation TYPE = Mod.res("update_skin");
}
*///?}
