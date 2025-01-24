package net.jakeccz.hrm.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.jakeccz.hrm.networking.payloads.ForceSavePacket;
import net.jakeccz.hrm.networking.payloads.SyncConfigPacket;
import net.jakeccz.hrm.util.HardcoreReviveModConfig;
import net.jakeccz.hrm.util.HardcoreReviveModConfig.*;
import net.jakeccz.hrm.util.HardcoreReviveModUtil;
import net.minecraft.block.Block;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.*;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Collectors;
import java.util.*;
import java.util.stream.Stream;

public class HardcoreReviveConfigCommand {

    private static final SuggestionProvider<ServerCommandSource> SUGGESTION_PROVIDER = (context, builder) -> {
        Stream<Identifier> tagValue = CustomBlockTags.getTagValueIds(ConfigTagArgumentType.getTag(context, "tag"));
        return CommandSource.suggestIdentifiers(tagValue, builder);
    };

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        LiteralCommandNode register = dispatcher.register((LiteralArgumentBuilder) ((LiteralArgumentBuilder) CommandManager.literal("hardcoreconfig").requires((source) -> {
            return source.hasPermissionLevel(2);
        })).then(CommandManager.literal("MODIFY").then(CommandManager.literal("revivalStructre").then(CommandManager.argument("tag", ConfigTagArgumentType.tags()).executes((context) -> {
            return getBlockTagValue((ServerCommandSource) context.getSource(), ConfigTagArgumentType.getTag(context, "tag"));
        }
        ).then(CommandManager.literal("add").then((RequiredArgumentBuilder) CommandManager.argument("block", BlockStateArgumentType.blockState(commandRegistryAccess)).executes((context) -> {
            return executeBlockTagsCommand((ServerCommandSource) context.getSource(), ConfigTagArgumentType.getTag(context, "tag"), BlockStateArgumentType.getBlockState(context, "block").getBlockState().getBlock(), ModifyAction.ADD);
        }))
        ).then(CommandManager.literal("remove").then(CommandManager.argument("block", IdentifierArgumentType.identifier()).suggests(SUGGESTION_PROVIDER).executes((context) -> {
            return executeBlockTagsCommand((ServerCommandSource) context.getSource(), ConfigTagArgumentType.getTag(context, "tag"), HardcoreReviveModUtil.getBlockFromId(IdentifierArgumentType.getIdentifier(context, "block")), ModifyAction.REMOVE);
        }))
        ).then(CommandManager.literal("reset").executes((context) -> {
            return executeBlockTagsCommand((ServerCommandSource) context.getSource(), ConfigTagArgumentType.getTag(context, "tag"), null, ModifyAction.RESET);
        }))
        ))).then(CommandManager.literal("SET").then(CommandManager.argument("gamerule", ConfigConditionArgumentType.settings()).executes((context) -> {
            return getConditionState((ServerCommandSource) context.getSource(), ConfigConditionArgumentType.getSetting(context, "gamerule"));
        }
        ).then(CommandManager.argument("true/false", BoolArgumentType.bool()).executes((context) -> {
            return executeCondition((ServerCommandSource) context.getSource(), ConfigConditionArgumentType.getSetting(context, "gamerule"), BoolArgumentType.getBool(context, "true/false"));
        })))));
    }
    //BoolArgumentType.bool()
    // /hardcorereviveconfig ritualblocktags witherRoseBlocks add minecraft:potted_wither_rose

    private enum ModifyAction {
        ADD,
        REMOVE,
        RESET;
    }

    private static int executeBlockTagsCommand(ServerCommandSource source, CustomBlockTags.TagType tag, @Nullable Block block, ModifyAction action) throws CommandSyntaxException {
        switch (action) {
            case ADD -> {
                if (block == null)
                    return 0;
                CustomBlockTags.add(block, CustomBlockTags.getTagValue(tag));
                source.sendMessage(Text.literal("Successfully added: " + HardcoreReviveModUtil.getIdFromBlock(block) + " to " + tag)); //Translate
            }
            case REMOVE -> {
                if (block == null)
                    return 0;
                CustomBlockTags.remove(block, CustomBlockTags.getTagValue(tag));
                source.sendMessage(Text.literal("Successfully removed: " + HardcoreReviveModUtil.getIdFromBlock(block) + " from " + tag)); //Translate
            }
            case RESET -> {
                CustomBlockTags.reset(tag);
                source.sendMessage(Text.literal("Successfully reset tag: " + tag)); //Translate
            }
        }
        HardcoreReviveModConfig.save();

        for (ServerPlayerEntity player : source.getServer().getPlayerManager().getPlayerList()) {
            Block[] blockSet = CustomBlockTags.getTagValue(tag).toArray(new Block[0]);
            int[] blockIds = Arrays.stream(blockSet).mapToInt(Registries.BLOCK::getRawId).toArray();

            ServerPlayNetworking.send(player, new SyncConfigPacket(tag.getName(), blockIds));
            ServerPlayNetworking.send(player, new ForceSavePacket());
        }
        return 1;
    }
    private static int getBlockTagValue(ServerCommandSource source, CustomBlockTags.TagType tag) throws CommandSyntaxException {
        source.sendMessage(Text.literal(tag + ": " + CustomBlockTags.getTagValueIds(tag).collect(Collectors.toSet()))); //Translate
        return 1;
    }
    private static int executeCondition(ServerCommandSource source, ConditionType condition, boolean state) throws CommandSyntaxException {
        source.sendMessage(Text.literal("Successfully updated: " + condition.getName() + " to " + state)); //Translate
        HardcoreReviveModConfig.setConditionState(condition, state);
        HardcoreReviveModConfig.save();

        for (ServerPlayerEntity player : source.getServer().getPlayerManager().getPlayerList()) {
            ServerPlayNetworking.send(player, new SyncConfigPacket(condition.getName(), state));
            ServerPlayNetworking.send(player, new ForceSavePacket());
        }
        return 1;
    }
    private static int getConditionState(ServerCommandSource source, ConditionType condition) throws CommandSyntaxException {
        source.sendMessage(Text.literal(condition + ": " + HardcoreReviveModConfig.getConditionState(condition))); //Translate
        return 1;
    }
}
