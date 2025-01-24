package net.jakeccz.hrm.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.jakeccz.hrm.util.HardcoreReviveModConfig.CustomBlockTags;
import net.jakeccz.hrm.util.HardcoreReviveModConfig.CustomBlockTags.*;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConfigTagArgumentType implements ArgumentType<TagType> {
    private static final Collection<String> EXAMPLES;
    private static final TagType[] VALUES;
    private static final DynamicCommandExceptionType INVALID_CONDITION_EXCEPTION;

    public static ConfigTagArgumentType tags() {
        return new ConfigTagArgumentType();
    }

    public static TagType getTag(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        return (TagType)context.getArgument(name, TagType.class);
    }

    @Override
    public TagType parse(StringReader stringReader) throws CommandSyntaxException {
        String string = stringReader.readUnquotedString();
        TagType condition = TagType.byName(string, (TagType)null);
        if (condition == null) {
            throw INVALID_CONDITION_EXCEPTION.createWithContext(stringReader, string);
        } else {
            return condition;
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return context.getSource() instanceof CommandSource ? CommandSource.suggestMatching(Arrays.stream(VALUES).map(TagType::getName), builder) : Suggestions.empty();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    static {
        EXAMPLES = (Collection) Stream.of(TagType.ORE_BLOCKS_TAG, TagType.FENCE_BLOCKS_TAG).map(TagType::getName).collect(Collectors.toList());
        VALUES = TagType.values();
        INVALID_CONDITION_EXCEPTION = new DynamicCommandExceptionType((gameMode) -> {
            return Text.translatable("argument.gamemode.invalid", new Object[]{gameMode});
        });
    }
}
