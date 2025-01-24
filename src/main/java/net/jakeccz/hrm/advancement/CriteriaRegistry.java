package net.jakeccz.hrm.advancement;

import net.jakeccz.hrm.HardcoreReviveMod;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.util.Identifier;

public class CriteriaRegistry {
    public static final RevivedPlayerCriterion REVIVE_PLAYER = new RevivedPlayerCriterion();

    public static void registerCriteria() {
        Criteria.register(REVIVE_PLAYER.getId().toString(), REVIVE_PLAYER);
        HardcoreReviveMod.LOGGER.info("Registering Criteria for " + HardcoreReviveMod.MOD_ID);
    }
}
