package net.jakeccz.hrm.util;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import net.jakeccz.hrm.HardcoreReviveMod;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

public class HardcoreReviveModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "hardcorerevivemod_cache/" + HardcoreReviveMod.CONFIG_FILE_ID + ".json");

    public static boolean LOSE_INVENTORY = false; // Default: false
    public static boolean RESTRICT_MENU_ACCESS = true; // Default: true
    public static boolean CREATIVE_PLAYERS_DROP_HEADS = false; // Default: false
    public static boolean LEAVE_STRUCTURE_BASE = true; // Default: true

    public static void useFile(String worldName) {
        CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "hardcorerevivemod_cache/" + worldName + ".json");
        CONFIG_FILE.getParentFile().mkdirs();
    }

    public static void load() {
        if (!CONFIG_FILE.exists()) {
            CONFIG_FILE.getParentFile().mkdirs();
            defaultConditionValues();
            CustomBlockTags.registerTags();
            save();
            return;
        }

        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            JsonObject json = GSON.fromJson(reader, JsonObject.class);
            for (ConditionType condition : ConditionType.values()) {
                setConditionState(condition, json.get(condition.getName()).getAsBoolean());
            }

            for (CustomBlockTags.TagType tag : CustomBlockTags.TagType.values()) {
                JsonArray jsonArray = json.get(tag.getName()).getAsJsonArray();
                Set<Block> blockSet = new HashSet<>();
                for (JsonElement blockId : jsonArray) {
                    blockSet.add(Registries.BLOCK.get(blockId.getAsInt()));
                }
                CustomBlockTags.setTagValue(tag, blockSet);
            }
        } catch (IOException e) {
            e.printStackTrace();
            defaultConditionValues();
            CustomBlockTags.defaultTagTypes();
        }
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            JsonObject json = new JsonObject();
            for (ConditionType condition : ConditionType.values()) {
                json.addProperty(condition.getName(), getConditionState(condition));
            }
            for (CustomBlockTags.TagType tag : CustomBlockTags.TagType.values()) {
                json.add(tag.getName(), convertToJsonObject(CustomBlockTags.getTagValue(tag)));
            }
            GSON.toJson(json, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static JsonArray convertToJsonObject(Set<Block> list) {
        JsonArray jsonArray = new JsonArray();
        for (Block block : list) {
            jsonArray.add(Registries.BLOCK.getRawId(block));
        }

        return jsonArray;
    }

    public enum ConditionType implements StringIdentifiable {
        LOSE_INVENTORY_CONDITION(LOSE_INVENTORY, "loseInventory"),
        RESTRICT_MENU_ACCESS_CONDITION(RESTRICT_MENU_ACCESS, "restrictMenuAccess"),
        CREATIVE_PLAYERS_DROP_HEADS_CONDITION(CREATIVE_PLAYERS_DROP_HEADS, "creativePlayerDropHeads"),
        LEAVE_STRUCTURE_BASE_CONDITION(LEAVE_STRUCTURE_BASE, "leaveReviveStructureBase");

        public static final StringIdentifiable.EnumCodec<ConditionType> CODEC = StringIdentifiable.createCodec(ConditionType::values);
        private final String name;
        public final boolean defaultValue;
        ConditionType(boolean defaultState, String name) {
            this.name = name;
            this.defaultValue = defaultState;
        }

        @Nullable
        @Contract("_,!null->!null;_,null->_")
        public static ConditionType byName(String name, @Nullable ConditionType defaultCondition) {
            ConditionType condition = (ConditionType)CODEC.byId(name);
            return condition != null ? condition : defaultCondition;
        }
        public String getName() {
            return this.name;
        }
        @Override
        public String asString() {
            return this.name;
        }
    }

    public static void setConditionState(ConditionType condition, boolean state) {
        switch (condition) {
            case LOSE_INVENTORY_CONDITION -> LOSE_INVENTORY = state;
            case RESTRICT_MENU_ACCESS_CONDITION -> RESTRICT_MENU_ACCESS = state;
            case CREATIVE_PLAYERS_DROP_HEADS_CONDITION -> CREATIVE_PLAYERS_DROP_HEADS = state;
            case LEAVE_STRUCTURE_BASE_CONDITION -> LEAVE_STRUCTURE_BASE = state;
        }
    }
    public static boolean getConditionState(ConditionType condition) {
        return switch (condition) {
            case LOSE_INVENTORY_CONDITION -> LOSE_INVENTORY;
            case RESTRICT_MENU_ACCESS_CONDITION -> RESTRICT_MENU_ACCESS;
            case CREATIVE_PLAYERS_DROP_HEADS_CONDITION -> CREATIVE_PLAYERS_DROP_HEADS;
            case LEAVE_STRUCTURE_BASE_CONDITION -> LEAVE_STRUCTURE_BASE;
        };
    }

    private static void defaultConditionValues() {
        LOSE_INVENTORY = ConditionType.LOSE_INVENTORY_CONDITION.defaultValue;
        RESTRICT_MENU_ACCESS = ConditionType.RESTRICT_MENU_ACCESS_CONDITION.defaultValue;
        CREATIVE_PLAYERS_DROP_HEADS = ConditionType.CREATIVE_PLAYERS_DROP_HEADS_CONDITION.defaultValue;
        LEAVE_STRUCTURE_BASE = ConditionType.LEAVE_STRUCTURE_BASE_CONDITION.defaultValue;
    }


    public static class CustomBlockTags {
        public static Set<Block> SOUL_SAND_BLOCKS = registerWithTag(BlockTags.WITHER_SUMMON_BASE_BLOCKS);
        public static Set<Block> FLOWER_BLOCKS = new HashSet<>(Set.of(Blocks.WITHER_ROSE, Blocks.POTTED_WITHER_ROSE));
        public static Set<Block> ORE_BLOCKS = registerWithTag(BlockTags.BEACON_BASE_BLOCKS);
        public static Set<Block> FENCE_BLOCKS = registerWithTag(BlockTags.FENCES);
        public static Set<Block> STAIR_BLOCKS = registerWithTag(BlockTags.STAIRS);

        public enum TagType implements StringIdentifiable {
            SOUL_SAND_BLOCKS_TAG(SOUL_SAND_BLOCKS, "soul_sand_blocks"),
            FLOWER_BLOCKS_TAG(FLOWER_BLOCKS, "flower_blocks"),
            ORE_BLOCKS_TAG(ORE_BLOCKS, "ore_blocks"),
            FENCE_BLOCKS_TAG(FENCE_BLOCKS, "fence_blocks"),
            STAIR_BLOCKS_TAG(STAIR_BLOCKS, "stair_blocks");

            public static final StringIdentifiable.EnumCodec<TagType> CODEC = StringIdentifiable.createCodec(TagType::values);
            private final String name;
            public final Set<Block> defaultValue;
            TagType(Set<Block> blockGroup, String name) {
                this.name = name;
                this.defaultValue = blockGroup;
            }

            @Nullable
            @Contract("_,!null->!null;_,null->_")
            public static TagType byName(String name, @Nullable TagType defaultCondition) {
                TagType tag = (TagType)CODEC.byId(name);
                return tag != null ? tag : defaultCondition;
            }
            public String getName() {
                return this.name;
            }
            @Override
            public String asString() {
                return this.name;
            }
        }

        public static Stream<Identifier> getTagValueIds(TagType tag) {
            return switch (tag) {
                case SOUL_SAND_BLOCKS_TAG -> SOUL_SAND_BLOCKS.stream().map(Registries.BLOCK::getId);
                case FLOWER_BLOCKS_TAG -> FLOWER_BLOCKS.stream().map(Registries.BLOCK::getId);
                case ORE_BLOCKS_TAG -> ORE_BLOCKS.stream().map(Registries.BLOCK::getId);
                case FENCE_BLOCKS_TAG -> FENCE_BLOCKS.stream().map(Registries.BLOCK::getId);
                case STAIR_BLOCKS_TAG -> STAIR_BLOCKS.stream().map(Registries.BLOCK::getId);
            };
        }

        public static Set<Block> getTagValue(TagType tag) {
            return switch (tag) {
                case SOUL_SAND_BLOCKS_TAG -> SOUL_SAND_BLOCKS;
                case FLOWER_BLOCKS_TAG -> FLOWER_BLOCKS;
                case ORE_BLOCKS_TAG -> ORE_BLOCKS;
                case FENCE_BLOCKS_TAG -> FENCE_BLOCKS;
                case STAIR_BLOCKS_TAG -> STAIR_BLOCKS;
            };
        }
        public static void setTagValue(TagType tag, Set<Block> blockSet) {
            switch (tag) {
                case SOUL_SAND_BLOCKS_TAG -> SOUL_SAND_BLOCKS = blockSet;
                case FLOWER_BLOCKS_TAG -> FLOWER_BLOCKS = blockSet;
                case ORE_BLOCKS_TAG -> ORE_BLOCKS = blockSet;
                case FENCE_BLOCKS_TAG -> FENCE_BLOCKS = blockSet;
                case STAIR_BLOCKS_TAG -> STAIR_BLOCKS = blockSet;
            }
        }

        public static void add(Block block, Set<Block> tag) {
            tag.add(block);
        }
        public static void remove(Block block, Set<Block> tag) {
            tag.remove(block);
        }
        public static void reset(TagType tag) {
            switch (tag) {
                case SOUL_SAND_BLOCKS_TAG -> SOUL_SAND_BLOCKS = registerWithTag(BlockTags.WITHER_SUMMON_BASE_BLOCKS);
                case FLOWER_BLOCKS_TAG -> FLOWER_BLOCKS = new HashSet<>(Set.of(Blocks.WITHER_ROSE, Blocks.POTTED_WITHER_ROSE));
                case ORE_BLOCKS_TAG -> ORE_BLOCKS = registerWithTag(BlockTags.BEACON_BASE_BLOCKS);
                case FENCE_BLOCKS_TAG -> FENCE_BLOCKS = registerWithTag(BlockTags.FENCES);
                case STAIR_BLOCKS_TAG -> STAIR_BLOCKS = registerWithTag(BlockTags.STAIRS);
            }
        }

        private static void defaultTagTypes() {
            for (TagType tag : TagType.values()) {
                reset(tag);
            }
        }


        private static Set<Block> registerWithTag(TagKey<Block> tag) {
            Set<Block> group = new HashSet<>(Set.of());
            for (int i=0; i<Registries.BLOCK.size(); i++) {
                if (Registries.BLOCK.get(i).getRegistryEntry().isIn(tag)) {
                    Block block = Registries.BLOCK.get(i);
                    group.add(block);
                }
            }
            return group;
        }
        public static void registerTags() {
            HardcoreReviveMod.LOGGER.info("Registering Tags for " + HardcoreReviveMod.MOD_ID);

            SOUL_SAND_BLOCKS = registerWithTag(BlockTags.WITHER_SUMMON_BASE_BLOCKS);
            FLOWER_BLOCKS = new HashSet<>(Set.of(Blocks.WITHER_ROSE, Blocks.POTTED_WITHER_ROSE));
            ORE_BLOCKS = registerWithTag(BlockTags.BEACON_BASE_BLOCKS);
            FENCE_BLOCKS = registerWithTag(BlockTags.FENCES);
            STAIR_BLOCKS = registerWithTag(BlockTags.STAIRS);
        }
    }

}
