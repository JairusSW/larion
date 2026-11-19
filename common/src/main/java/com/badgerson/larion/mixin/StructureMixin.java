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
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.ScatteredFeaturePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.structures.IglooStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Structure.class)
public class StructureMixin {
    @Unique
    private static final int LARION_FIRST_ALPINE_STRUCTURE_Y = 144;

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
            // Some pieces are moved to the surface during block placement.
            boolean followsSurface = piece instanceof ScatteredFeaturePiece
                    || start.getStructure() instanceof IglooStructure
                    || (piece instanceof PoolElementStructurePiece poolPiece
                            && poolPiece.getElement().getProjection() == StructureTemplatePool.Projection.TERRAIN_MATCHING);
            if (!followsSurface && box.maxY() < LARION_FIRST_ALPINE_STRUCTURE_Y) {
                continue;
            }

            var center = box.getCenter();
            int surface = generator.getBaseHeight(center.getX(), center.getZ(),
                    Heightmap.Types.WORLD_SURFACE_WG, heightAccessor, randomState) - 1;
            if (surface >= LARION_FIRST_ALPINE_STRUCTURE_Y && (followsSurface || box.maxY() >= surface - 4)) {
                callback.setReturnValue(StructureStart.INVALID_START);
                return;
            }
        }
    }
}
