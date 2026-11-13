package com.nivek.eclipse.transformation;

import com.nivek.eclipse.NivekEclipse;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Cliente -> servidor: alternar a forma (indice do enum Form). Repetir a forma ativa a desativa. */
public record FormPayload(int form) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<FormPayload> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(NivekEclipse.MOD_ID, "form_toggle"));

    public static final StreamCodec<RegistryFriendlyByteBuf, FormPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, FormPayload::form, FormPayload::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
