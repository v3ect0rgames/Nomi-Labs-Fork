package com.nomiceu.nomilabs.remap;

import com.nomiceu.nomilabs.LabsValues;
import com.nomiceu.nomilabs.remap.datafixer.DataFixerHandler;
import com.nomiceu.nomilabs.remap.remapper.*;
import com.nomiceu.nomilabs.util.LabsNames;
import gregtech.api.util.GTUtility;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.StartupQuery;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.spongepowered.include.com.google.common.collect.ImmutableList;

import java.util.List;

import static com.nomiceu.nomilabs.LabsValues.*;

public class LabsRemappers {
    public static List<ItemRemapper> ITEM_REMAPPERS;
    public static List<BlockRemapper> BLOCK_REMAPPERS;

    public static boolean checked = false;

    public static void preInit() {
        ITEM_REMAPPERS = ImmutableList.of(
                // Remap Deprecated Items
                new DeprecatedItemRemapper(),

                // Remap Content Tweaker Items
                new ItemRemapper(rl -> (rl.getNamespace().equals(CONTENTTWEAKER_MODID)),
                        rl -> LabsNames.makeLabsName(rl.getPath())),

                /*
                 * Remap old DevTech perfect gem to new perfect gem.
                 * DevTech did this badly, and created a MetaPrefixItem with GT's material registry but their
                 * Mod ID, so we need to map the DevTech metaitem to the GT metaitem in missing mappings.
                 */
                new ItemRemapper(rl -> (rl.getNamespace().equals(DEVTECH_MODID) && rl.getPath().equals(PERFECT_GEM_META)),
                        rl -> GTUtility.gregtechId(PERFECT_GEM_META))
        );
        BLOCK_REMAPPERS = ImmutableList.of(
                // Remap Content Tweaker Blocks
                new BlockRemapper(rl -> (rl.getNamespace().equals(LabsValues.CONTENTTWEAKER_MODID)),
                        rl -> LabsNames.makeLabsName(rl.getPath()))
        );
    }

    public static void remapItems(RegistryEvent.MissingMappings<Item> event) {
        remapEntries(event, ITEM_REMAPPERS);
    }

    public static void remapBlocks(RegistryEvent.MissingMappings<Block> event) {
        remapEntries(event, BLOCK_REMAPPERS);
    }

    private static <T extends IForgeRegistryEntry<T>> void remapEntries(RegistryEvent.MissingMappings<T> event, List<? extends Remapper<T>> remappers) {
        if (!checked && !DataFixerHandler.checked) {
            var needsRemap = false;
            for (var entry : event.getAllMappings()) {
                for (var remapper : remappers) {
                    if (remapper.shouldRemap(entry.key)) needsRemap = true;
                    break;
                }
            }
            if (!needsRemap) return; // Don't set checked to true, other remappers might detect needed remaps

            var message = new StringBuilder("This world must be remapped.\n\n")
                    .append(TextFormatting.BOLD).append("A Backup will be made.\n")
                    .append("Pressing 'No' will cancel world loading.\n\n")
                    .append(TextFormatting.RED)
                    .append("Note that after the world is loaded with this, you CANNOT undo this!\n")
                    .append("You WILL have to load from the backup in order to load in a previous version!");

            if (!StartupQuery.confirm(message.toString())) {
                LabsRemapHelper.abort();
            }

            LabsRemapHelper.createWorldBackup();

            checked = true;
        }
        for (var entry : event.getAllMappings()) {
            for (var remapper : remappers) {
                if (!remapper.shouldRemap(entry.key)) continue;
                remapper.remapEntry(entry);
                break;
            }
        }
    }
}
