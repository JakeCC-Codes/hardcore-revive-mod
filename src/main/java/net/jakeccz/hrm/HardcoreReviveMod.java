package net.jakeccz.hrm;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.jakeccz.hrm.advancement.CriteriaRegistry;
import net.jakeccz.hrm.commands.CommandRegistry;
import net.jakeccz.hrm.item.ItemRegistry;
import net.jakeccz.hrm.networking.NetworkingPacketRegistry;
import net.jakeccz.hrm.networking.payloads.ForceSavePacket;
import net.jakeccz.hrm.networking.payloads.SyncConfigPacket;
import net.jakeccz.hrm.screen.ScreenHandlerRegistry;
import net.jakeccz.hrm.util.HardcoreReviveModConfig;
import net.jakeccz.hrm.util.HardcoreReviveModConfig.*;
import net.minecraft.block.Block;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class HardcoreReviveMod implements ModInitializer {
	public static final String MOD_ID = "hardcorerevivemod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final String CONFIG_FILE_ID = "defaultconfig";

	@Override
	public void onInitialize() {
		ItemRegistry.registerItems();
		ScreenHandlerRegistry.registerScreenHandlers();
		CriteriaRegistry.registerCriteria();
		NetworkingPacketRegistry.registerC2SPackets();
		CommandRegistry.registerCommands();

		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			HardcoreReviveModConfig.useFile(CONFIG_FILE_ID);
			HardcoreReviveModConfig.load();
		});

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			CustomBlockTags.TagType[] customTagValues = CustomBlockTags.TagType.values();
			String[] tagIds = new String[customTagValues.length];
			int[][] blockIds = new int[customTagValues.length][];
			for (int i=0; i<customTagValues.length; i++)
			{
				tagIds[i] = customTagValues[i].getName();
				Block[] blockSet = CustomBlockTags.getTagValue(customTagValues[i]).toArray(new Block[0]);
				blockIds[i] = Arrays.stream(blockSet).mapToInt(Registries.BLOCK::getRawId).toArray();
			}

			ConditionType[] conditionTypeValues = ConditionType.values();
			String[] tagIds2 = new String[conditionTypeValues.length];
			boolean[] conditionIds = new boolean[conditionTypeValues.length];
			for (int i=0; i<conditionTypeValues.length; i++)
			{
				tagIds2[i] = conditionTypeValues[i].getName();
				conditionIds[i] = HardcoreReviveModConfig.getConditionState(conditionTypeValues[i]);
			}

			AtomicInteger ticks = new AtomicInteger();
			ServerTickEvents.START_SERVER_TICK.register(client -> {
				ticks.getAndIncrement();
				if (ticks.get() == 480) {
					LOGGER.info(MOD_ID + ": Syncing Client Data...");
					ServerPlayNetworking.send(handler.player, new SyncConfigPacket(tagIds, blockIds));
					ServerPlayNetworking.send(handler.player, new SyncConfigPacket(tagIds2, conditionIds));
					ServerPlayNetworking.send(handler.player, new ForceSavePacket());
				}
			});

		});

		ServerEntityEvents.EQUIPMENT_CHANGE.register((livingEntity, equipmentSlot, previousItem, nextItem) -> {
			if (livingEntity.getType() != EntityType.PLAYER)
				return;
			if (equipmentSlot != EquipmentSlot.HEAD)
				return;
			ItemStack playerHead = nextItem.getItem() != Items.PLAYER_HEAD ? previousItem : nextItem;
			if (playerHead.getItem() != Items.PLAYER_HEAD)
				return;

			ProfileComponent profileComponent = playerHead.get(DataComponentTypes.PROFILE);

			boolean toggleState = nextItem.getItem() == Items.PLAYER_HEAD;
			if (profileComponent != null && toggleState) {
				livingEntity.setStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 200), livingEntity);
				livingEntity.setStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, StatusEffectInstance.INFINITE, 0), livingEntity);
				livingEntity.setStatusEffect(new StatusEffectInstance(StatusEffects.HEALTH_BOOST, StatusEffectInstance.INFINITE, 4), livingEntity);
				livingEntity.setStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, StatusEffectInstance.INFINITE, 0), livingEntity);
			} else {
				livingEntity.removeStatusEffect(StatusEffects.SLOWNESS);
				livingEntity.removeStatusEffect(StatusEffects.HEALTH_BOOST);
				livingEntity.removeStatusEffect(StatusEffects.RESISTANCE);
			}
		});
	}

}