package net.jakeccz.hrm.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.jakeccz.hrm.util.HardcoreReviveModConfig.*;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.GameModeArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConfigConditionArgumentType implements ArgumentType<ConditionType> {
    private static final Collection<String> EXAMPLES;
    private static final ConditionType[] VALUES;
    private static final DynamicCommandExceptionType INVALID_CONDITION_EXCEPTION;

    public static ConfigConditionArgumentType settings() {
        return new ConfigConditionArgumentType();
    }

    public static ConditionType getSetting(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        return (ConditionType)context.getArgument(name, ConditionType.class);
    }

    @Override
    public ConditionType parse(StringReader stringReader) throws CommandSyntaxException {
        String string = stringReader.readUnquotedString();
        ConditionType condition = ConditionType.byName(string, (ConditionType)null);
        if (condition == null) {
            throw INVALID_CONDITION_EXCEPTION.createWithContext(stringReader, string);
        } else {
            return condition;
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return context.getSource() instanceof CommandSource ? CommandSource.suggestMatching(Arrays.stream(VALUES).map(ConditionType::getName), builder) : Suggestions.empty();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    static {
        EXAMPLES = (Collection) Stream.of(ConditionType.LOSE_INVENTORY_CONDITION, ConditionType.RESTRICT_MENU_ACCESS_CONDITION).map(ConditionType::getName).collect(Collectors.toList());
        VALUES = ConditionType.values();
        INVALID_CONDITION_EXCEPTION = new DynamicCommandExceptionType((gameMode) -> {
            return Text.translatable("argument.gamemode.invalid", new Object[]{gameMode});
        });
    }
}
