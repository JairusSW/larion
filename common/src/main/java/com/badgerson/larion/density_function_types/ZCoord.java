// Original credit: https://github.com/klinbee/More-Density-Functions (thanks)
package com.badgerson.larion.density_function_types;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.Interval;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.densityfunction.DensitySampler;
import net.minecraft.world.level.levelgen.densityfunction.DfRewriteRule;

public record ZCoord() implements DensityFunction {
    public static final MapCodec<ZCoord> CODEC = MapCodec.unit(new ZCoord());

    @Override
    public DensitySampler compileSampler(CompileContext context) {
        return (PointSampler) (samplerContext, x, y, z) -> Math.clamp(z, -30_000_000, 30_000_000);
    }

    @Override
    public DensityFunction rewriteChildren(DfRewriteRule rule) {
        return this;
    }

    @Override
    public Interval range() {
        return Interval.ofSymmetric(30_000_000);
    }

    @Override
    public int domainAxes() {
        return AXIS_Z;
    }

    @Override
    public MapCodec<ZCoord> codec() {
        return CODEC;
    }
}
