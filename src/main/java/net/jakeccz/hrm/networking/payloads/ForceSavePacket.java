package net.jakeccz.hrm.networking.payloads;

import net.jakeccz.hrm.util.HardcoreReviveModUtil;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record ForceSavePacket() implements CustomPayload {
    public static final Id<ForceSavePacket> ID = new Id<>(HardcoreReviveModUtil.createId("force_save_packet"));
    public static final PacketCodec<RegistryByteBuf, ForceSavePacket> PACKET_CODEC = new PacketCodec<>() {

        @Override
        public void encode(RegistryByteBuf buf, ForceSavePacket value) {

        }

        @Override
        public ForceSavePacket decode(RegistryByteBuf buf) {
            return new ForceSavePacket();
        }
    };

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
