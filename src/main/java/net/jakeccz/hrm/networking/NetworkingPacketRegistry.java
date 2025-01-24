package net.jakeccz.hrm.networking;

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.jakeccz.hrm.HardcoreReviveMod;
import net.jakeccz.hrm.item.ItemRegistry;
import net.jakeccz.hrm.networking.payloads.ForceSavePacket;
import net.jakeccz.hrm.networking.payloads.SkullGiverPacket;
import net.jakeccz.hrm.networking.payloads.SyncConfigPacket;
import net.jakeccz.hrm.util.HardcoreReviveModConfig;
import net.jakeccz.hrm.util.HardcoreReviveModConfig.*;
import net.jakeccz.hrm.util.HardcoreReviveModUtil;
import net.minecraft.block.Block;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;

import java.util.Set;

public class NetworkingPacketRegistry {

    public static void registerC2SPackets() {
        HardcoreReviveMod.LOGGER.info("Registering Packets for " + HardcoreReviveMod.MOD_ID);

        PayloadTypeRegistry.playC2S().register(SkullGiverPacket.ID, SkullGiverPacket.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(SyncConfigPacket.ID, SyncConfigPacket.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(ForceSavePacket.ID, ForceSavePacket.PACKET_CODEC);

        PayloadTypeRegistry.playS2C().register(SyncConfigPacket.ID, SyncConfigPacket.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(ForceSavePacket.ID, ForceSavePacket.PACKET_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(SkullGiverPacket.ID, NetworkingPacketRegistry.ServerReceiverPackets::SkullGiver); //SkullGiver Packet
        ServerPlayNetworking.registerGlobalReceiver(SyncConfigPacket.ID, NetworkingPacketRegistry.ServerReceiverPackets::SyncConfig); // SyncToConfig Packet
        ServerPlayNetworking.registerGlobalReceiver(ForceSavePacket.ID, NetworkingPacketRegistry.ServerReceiverPackets::ForceSave); // ForceSave Packet
    }

    public static class ServerReceiverPackets {
        public static void SkullGiver(SkullGiverPacket packet, ServerPlayNetworking.Context context) {
            ServerPlayerEntity skullOwnerPlayer = context.server().getPlayerManager().getPlayer(packet.skullOwnerUUID());
            GameProfile skullOwnerProfile = new GameProfile(packet.skullOwnerUUID(), packet.skullOwnerName());
            if (skullOwnerPlayer != null) {
                skullOwnerProfile = skullOwnerPlayer.getGameProfile();
            }
            Hand handWithItem = packet.handID() == 1 ? Hand.MAIN_HAND : Hand.OFF_HAND;

            ServerPlayerEntity player = context.player();
            ItemStack skullItemStack = Items.PLAYER_HEAD.getDefaultStack();
            skullItemStack.set(DataComponentTypes.PROFILE, new ProfileComponent(skullOwnerProfile));
            skullItemStack.set(DataComponentTypes.LORE, new LoreComponent(packet.getItemStackLore()));

            if (handWithItem == Hand.MAIN_HAND && player.getMainHandStack().getItem() != ItemRegistry.CRAFTABLE_HEAD) {
                player.getInventory().removeStack(player.getInventory().getSlotWithStack(ItemRegistry.CRAFTABLE_HEAD.getDefaultStack()));
                player.giveItemStack(skullItemStack);
            } else {
                player.setStackInHand(handWithItem, skullItemStack);
            }
        }

        public static void SyncConfig(SyncConfigPacket payload, ServerPlayNetworking.Context context) {
            HardcoreReviveModConfig.useFile(HardcoreReviveMod.CONFIG_FILE_ID);
            int action = payload.actionType();
            String[] tagIds = payload.tagIds();
            switch (action) {
                case 0 -> {
                    int[][] blockIds = payload.blockIds();
                    for (int i=0; i<blockIds.length; i++)
                    {
                        CustomBlockTags.TagType tag = HardcoreReviveModConfig.CustomBlockTags.TagType.valueOf(tagIds[i]);
                        Set<Block> blockSet = HardcoreReviveModUtil.readBlockSet(blockIds[i]);
                        HardcoreReviveModConfig.CustomBlockTags.setTagValue(tag, blockSet);
                    }
                }
                case 1 -> {
                    boolean[] conditionIds = payload.conditionIds();
                    for (int i=0; i<conditionIds.length; i++)
                    {
                        ConditionType condition = HardcoreReviveModConfig.ConditionType.valueOf(tagIds[i]);
                        boolean conditionState = conditionIds[i];
                        HardcoreReviveModConfig.setConditionState(condition, conditionState);
                    }
                }
            }

            HardcoreReviveMod.LOGGER.info(HardcoreReviveMod.MOD_ID + ": Successfully Synced Config File On Server!");
        }

        public static void ForceSave(ForceSavePacket payload, ServerPlayNetworking.Context context) {
            HardcoreReviveModConfig.useFile(HardcoreReviveMod.CONFIG_FILE_ID);
            HardcoreReviveModConfig.save();
        }
    }

    public static class ClientReceiverPackets {
        public static void SyncConfig(SyncConfigPacket payload, ClientPlayNetworking.Context context) {
            ClientPlayNetworkHandler networkHandler = context.player().networkHandler;
            HardcoreReviveModConfig.useFile(networkHandler.getServerInfo() != null ? networkHandler.getServerInfo().address : HardcoreReviveMod.CONFIG_FILE_ID);
            int action = payload.actionType();
            String[] tagIds = payload.tagIds();
            switch (action) {
                case 0 -> {
                    int[][] blockIds = payload.blockIds();
                    for (int i=0; i<blockIds.length; i++)
                    {
                        CustomBlockTags.TagType tag = HardcoreReviveModConfig.CustomBlockTags.TagType.byName(tagIds[i], null);
                        Set<Block> blockSet = HardcoreReviveModUtil.readBlockSet(blockIds[i]);
                        if (tag != null) {
                            HardcoreReviveModConfig.CustomBlockTags.setTagValue(tag, blockSet);
                        }
                    }
                }
                case 1 -> {
                    boolean[] conditionIds = payload.conditionIds();
                    for (int i=0; i<conditionIds.length; i++)
                    {
                        ConditionType condition = HardcoreReviveModConfig.ConditionType.byName(tagIds[i], null);
                        boolean conditionState = conditionIds[i];
                        if (condition != null) {
                            HardcoreReviveModConfig.setConditionState(condition, conditionState);
                        }
                    }
                }
            }

            HardcoreReviveMod.LOGGER.info(HardcoreReviveMod.MOD_ID + ": Successfully Synced Config File On Client!");
        }

        public static void ForceSave(ForceSavePacket payload, ClientPlayNetworking.Context context) {
            ClientPlayNetworkHandler networkHandler = context.player().networkHandler;
            HardcoreReviveModConfig.useFile(networkHandler.getServerInfo() != null ? networkHandler.getServerInfo().address : HardcoreReviveMod.CONFIG_FILE_ID);
            HardcoreReviveModConfig.save();
        }
    }
}
