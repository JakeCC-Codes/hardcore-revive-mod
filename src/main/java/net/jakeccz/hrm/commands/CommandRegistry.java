package net.jakeccz.hrm.commands;

import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.jakeccz.hrm.HardcoreReviveMod;
import net.jakeccz.hrm.util.HardcoreReviveModUtil;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;

public class CommandRegistry {

    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register(HardcoreReviveConfigCommand::register);
        ArgumentTypeRegistry.registerArgumentType(HardcoreReviveModUtil.createId("config_condition"), ConfigConditionArgumentType.class, ConstantArgumentSerializer.of(ConfigConditionArgumentType::settings));
        ArgumentTypeRegistry.registerArgumentType(HardcoreReviveModUtil.createId("config_tag"), ConfigTagArgumentType.class, ConstantArgumentSerializer.of(ConfigTagArgumentType::tags));

        HardcoreReviveMod.LOGGER.info("Registering Commands for " + HardcoreReviveMod.MOD_ID);
    }
}
