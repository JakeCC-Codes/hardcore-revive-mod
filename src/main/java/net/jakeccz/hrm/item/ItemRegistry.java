package net.jakeccz.hrm.item;

import net.jakeccz.hrm.HardcoreReviveMod;
import net.jakeccz.hrm.util.HardcoreReviveModUtil;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Rarity;


public class ItemRegistry {
    public static Item CRAFTABLE_HEAD = registerItem("craftable_head_3d",
            new CraftableHeadItem(new CraftableHeadItem.Settings().maxCount(1).rarity(Rarity.UNCOMMON).registryKey(createRegistryKey("craftable_head_3d"))));

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, HardcoreReviveModUtil.createId(name), item);
    }
    private static RegistryKey<Item> createRegistryKey(String path) {
        return RegistryKey.of(RegistryKeys.ITEM, HardcoreReviveModUtil.createId(path));
    }

    public static void registerItems() {
        HardcoreReviveMod.LOGGER.info("Registering Items for " + HardcoreReviveMod.MOD_ID);
    }

}
