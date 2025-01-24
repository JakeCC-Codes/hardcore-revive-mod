package net.jakeccz.hrm.networking.payloads;

import net.jakeccz.hrm.util.HardcoreReviveModUtil;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.UUID;

public record SkullGiverPacket(String skullOwnerName, UUID skullOwnerUUID, int handID) implements CustomPayload {
    public static final Id<SkullGiverPacket> ID = new Id<>(HardcoreReviveModUtil.createId("skull_giver_packet"));
    public static final PacketCodec<RegistryByteBuf, SkullGiverPacket> PACKET_CODEC = new PacketCodec<>() {

        @Override
        public void encode(RegistryByteBuf buf, SkullGiverPacket value) {
            buf.writeString(value.skullOwnerName);
            buf.writeUuid(value.skullOwnerUUID);
            buf.writeInt(value.handID);
        }

        @Override
        public SkullGiverPacket decode(RegistryByteBuf buf) {
            return new SkullGiverPacket(buf.readString(), buf.readUuid(), buf.readInt());
        }
    };

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public List<Text> getItemStackLore() {
        return HardcoreReviveModUtil.LORE;
    }
}
