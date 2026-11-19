package com.badgerson.larion.mixin;

import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Structure.class)
public class StructureMixin {
    private static final int LARION_MAX_STRUCTURE_GROUND_Y = 144;

    @Inject(method = "generate", at = @At("RETURN"), cancellable = true)
    private void larion$keepStructuresBelowMountains(Holder<Structure> structure,
            ResourceKey<Level> dimension, RegistryAccess registries, ChunkGenerator generator,
            BiomeSource biomes, Climate.Sampler climate, RandomState randomState,
            StructureTemplateManager templates, long seed, ChunkPos chunk, int references,
            LevelHeightAccessor heightAccessor, Predicate<Holder<Biome>> validBiome,
            CallbackInfoReturnable<StructureStart> callback) {
        StructureStart start = callback.getReturnValue();
        if (!start.isValid() || !dimension.equals(Level.OVERWORLD)
                || !(generator instanceof NoiseBasedChunkGenerator noise)
                || !noise.stable(NoiseGeneratorSettings.OVERWORLD) || noise.getMinY() != -128) {
            return;
        }

        for (var piece : start.getPieces()) {
            var box = piece.getBoundingBox();
            if (box.minY() < LARION_MAX_STRUCTURE_GROUND_Y) {
                continue;
            }
            var center = box.getCenter();
            int surface = generator.getBaseHeight(center.getX(), center.getZ(),
                    Heightmap.Types.WORLD_SURFACE_WG, heightAccessor, randomState) - 1;
            if (surface >= LARION_MAX_STRUCTURE_GROUND_Y) {
                callback.setReturnValue(StructureStart.INVALID_START);
                return;
            }
        }
    }
}
