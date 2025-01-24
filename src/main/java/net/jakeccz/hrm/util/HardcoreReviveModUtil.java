package net.jakeccz.hrm.util;

import net.jakeccz.hrm.HardcoreReviveMod;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class HardcoreReviveModUtil {
    public static final Text LORE_TEXT = Text.translatable("item.lore.reviving_item").setStyle(Style.EMPTY.withColor(TextColor.fromFormatting(Formatting.DARK_RED)).withItalic(false));
    public static final List<Text> LORE = List.of(LORE_TEXT);

    public static Identifier createId(String path) {
        return Identifier.of(HardcoreReviveMod.MOD_ID, path);
    }
    public static Identifier getIdFromBlock(Block block) {
        return Registries.BLOCK.getId(block);
    }
    public static Block getBlockFromId(Identifier id) {
        return Registries.BLOCK.get(id);
    }
    public static Set<Block> readBlockSet(int[] blockIds) {
        return Arrays.stream(blockIds).mapToObj(Registries.BLOCK::get).collect(Collectors.toSet());
    }

    public static String capitalize(String text)
    {
        String newText = text.substring(text.indexOf(':') + 1);
        char[] array = newText.toLowerCase().toCharArray();
        array[0] = Character.toUpperCase(array[0]);

        for (int i = 1; i < array.length; i++) {
            if (!Character.isAlphabetic(array[i])) {
                array[i] = ' ';
            } else if (Character.isWhitespace(array[i - 1])) {
                array[i] = Character.toUpperCase(array[i]);
            }
        }

        return new String(array);
    }
}
