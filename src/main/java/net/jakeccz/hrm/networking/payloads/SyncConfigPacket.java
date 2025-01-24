package net.jakeccz.hrm.networking.payloads;

import net.jakeccz.hrm.HardcoreReviveMod;
import net.jakeccz.hrm.util.HardcoreReviveModUtil;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record SyncConfigPacket(int actionType, String[] tagIds, int[][] blockIds, boolean[] conditionIds) implements CustomPayload {
    public static final Id<SyncConfigPacket> ID = new Id<>(HardcoreReviveModUtil.createId("sync_config_packet"));
    public static final PacketCodec<RegistryByteBuf, SyncConfigPacket> PACKET_CODEC = new PacketCodec<>() {

        @Override
        public void encode(RegistryByteBuf buf, SyncConfigPacket value) {
            String[] tagIdValues = value.tagIds;
            int[][] blockIdValues = value.blockIds;
            boolean[] conditionIdValues = value.conditionIds;
            buf.writeInt(value.actionType);
            switch (value.actionType) {
                case 0 -> {
                    buf.writeInt(blockIdValues.length);
                    for (int i=0; i<blockIdValues.length; i++)
                    {
                        buf.writeString(tagIdValues[i]);
                        buf.writeIntArray(blockIdValues[i]);
                    }
                }
                case 1 -> {
                    buf.writeInt(conditionIdValues.length);
                    for (int i=0; i<conditionIdValues.length; i++)
                    {
                        buf.writeString(tagIdValues[i]);
                        buf.writeBoolean(conditionIdValues[i]);
                    }
                }
            }
        }

        @Override
        public SyncConfigPacket decode(RegistryByteBuf buf) {
            int actionType = buf.readInt();
            int actionArrayLength = buf.readInt();
            String[] newTagIds = new String[actionArrayLength];
            switch (actionType) {
                case 0:
                    int[][] newBlockIds = new int[actionArrayLength][];
                    for (int i=0; i<actionArrayLength; i++) {
                        newTagIds[i] = buf.readString();
                        newBlockIds[i] = buf.readIntArray();
                    }
                    buf.clear();
                    return new SyncConfigPacket(newTagIds, newBlockIds);
                case 1:
                    boolean[] newConditionIds = new boolean[actionArrayLength];
                    for (int i=0; i<actionArrayLength; i++) {
                        newTagIds[i] = buf.readString();
                        newConditionIds[i] = buf.readBoolean();
                    }
                    buf.clear();
                    return new SyncConfigPacket(newTagIds, newConditionIds);
            }
            return new SyncConfigPacket(new String[0], new boolean[0]);
        }
    };
    public SyncConfigPacket(String tagIds, int[] blockIds) {
        this(0, new String[]{tagIds}, new int[][]{blockIds}, new boolean[0]);
    }
    public SyncConfigPacket(String tagIds, boolean conditionIds) {
        this(1, new String[]{tagIds}, new int[0][0], new boolean[]{conditionIds});
    }
    public SyncConfigPacket(String[] tagIds, int[][] blockIds) {
        this(0, tagIds, blockIds, new boolean[0]);
    }
    public SyncConfigPacket(String[] tagIds, boolean[] conditionIds) {
        this(1, tagIds, new int[0][0], conditionIds);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
