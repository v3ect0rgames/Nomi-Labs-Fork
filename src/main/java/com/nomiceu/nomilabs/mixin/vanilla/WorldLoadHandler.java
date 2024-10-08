package com.nomiceu.nomilabs.mixin.vanilla;

import java.io.File;

import net.minecraft.util.datafix.DataFixer;
import net.minecraft.world.storage.SaveFormatOld;
import net.minecraft.world.storage.SaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.fml.common.Loader;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.nomiceu.nomilabs.LabsValues;
import com.nomiceu.nomilabs.config.LabsConfig;
import com.nomiceu.nomilabs.remap.datafixer.DataFixerHandler;
import com.nomiceu.nomilabs.util.LabsSide;

@Mixin(value = SaveFormatOld.class)
public class WorldLoadHandler {

    // No need for remap, forge method
    @Inject(method = "loadAndFix(Ljava/io/File;Lnet/minecraft/util/datafix/DataFixer;Lnet/minecraft/world/storage/SaveHandler;)Lnet/minecraft/world/storage/WorldInfo;",
            at = @At(value = "INVOKE_ASSIGN",
                     target = "Lnet/minecraft/nbt/CompressedStreamTools;readCompressed(Ljava/io/InputStream;)Lnet/minecraft/nbt/NBTTagCompound;",
                     remap = true),
            remap = false,
            require = 1)
    private static void loadDataFixers(File file, DataFixer fixer, SaveHandler save,
                                       CallbackInfoReturnable<WorldInfo> cir) {
        if (!LabsSide.isServer())
            return;

        DataFixerHandler.onWorldLoad(save);

        if (DataFixerHandler.hasNoNewFixes()) return;

        if (Loader.isModLoaded(LabsValues.ENDER_STORAGE_MODID) &&
                LabsConfig.modIntegration.enableEnderStorageIntegration)
            DataFixerHandler.processEnderStorageInfo(fixer, save);
    }
}
