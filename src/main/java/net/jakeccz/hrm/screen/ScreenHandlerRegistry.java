package net.jakeccz.hrm.screen;

import net.jakeccz.hrm.HardcoreReviveMod;
import net.jakeccz.hrm.util.HardcoreReviveModUtil;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;

public class ScreenHandlerRegistry {
    public static final ScreenHandlerType<HeadSelectorScreenHandler> CRAFTABLE_HEAD_SCREEN_HANDLER =
            Registry.register(Registries.SCREEN_HANDLER, HardcoreReviveModUtil.createId("head_selection"),
                    new ScreenHandlerType<>(HeadSelectorScreenHandler::new, FeatureFlags.VANILLA_FEATURES));

    public static void registerScreenHandlers() {
        HardcoreReviveMod.LOGGER.info("Registering Screen Handlers for " + HardcoreReviveMod.MOD_ID);
    }
}
