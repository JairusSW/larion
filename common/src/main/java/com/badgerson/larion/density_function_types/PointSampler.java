package com.badgerson.larion.density_function_types;

import net.minecraft.world.level.levelgen.densityfunction.DensityBuffer;
import net.minecraft.world.level.levelgen.densityfunction.DensitySampler;
import net.minecraft.world.level.levelgen.densityfunction.DensityVolume;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;

@FunctionalInterface
interface PointSampler extends DensitySampler {
    @Override
    default void sampleVolume(SamplerContext context, DensityBuffer buffer, DensityVolume volume) {
        DensitySampler.sampleVolumeNaive(context, buffer, volume, this);
    }
}
