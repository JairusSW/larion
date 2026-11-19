package com.badgerson.larion.density_function_types;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Interval;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.densityfunction.DensitySampler;
import net.minecraft.world.level.levelgen.densityfunction.DfRewriteRule;

public record MountainRelief(DensityFunction shiftX, DensityFunction shiftZ) implements DensityFunction {
    private static final int SPACING = 768;

    public static final MapCodec<MountainRelief> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(DensityFunction.CODEC.fieldOf("shift_x").forGetter(MountainRelief::shiftX),
                    DensityFunction.CODEC.fieldOf("shift_z").forGetter(MountainRelief::shiftZ))
            .apply(instance, MountainRelief::new));

    @Override
    public DensitySampler compileSampler(CompileContext context) {
        var xShift = shiftX.compileSampler(context);
        var zShift = shiftZ.compileSampler(context);
        return (PointSampler) (samplerContext, x, y, z) -> {
            double east = x + 440 * xShift.sampleValue(samplerContext, x, 0, z);
            double south = z + 440 * zShift.sampleValue(samplerContext, x, 0, z);
            return (float) relief(east, south);
        };
    }

    static double relief(double x, double z) {
        int gridX = (int) Math.floor(x / SPACING);
        int gridZ = (int) Math.floor(z / SPACING);
        double height = 0;
        for (int dz = -1; dz <= 1; dz++) {
            for (int dx = -1; dx <= 1; dx++) {
                int cellX = gridX + dx;
                int cellZ = gridZ + dz;
                long shape = mix(341873128712L * cellX + 132897987541L * cellZ + 117L);
                double east = x - SPACING * (cellX + 0.2 + 0.6 * fraction(shape, 0));
                double south = z - SPACING * (cellZ + 0.2 + 0.6 * fraction(shape, 10));
                double radius = 410 + 115 * fraction(shape, 20);
                double distance = Math.hypot(east, south);
                if (distance >= radius) {
                    continue;
                }

                double angle = Math.atan2(south, east) - 2 * Math.PI * fraction(shape, 30);
                double radial = distance / radius;
                // Spurs stay attached to the summit. Valleys widen downstream and
                // fade into the same foothill apron as the ridges beside them.
                int valleys = 3 + (int) (3 * fraction(shape, 50));
                double winding = valleys * angle + 2.2 * radial + 0.9 * Math.sin(angle);
                double valley = 0.5 + 0.5 * Math.cos(winding);
                double tributaries = 0.5 + 0.5 * Math.cos(2 * winding - radial);
                double profile = smooth(1 - radial);
                double shoulder = radial * smooth(Math.min(1, radial / 0.24));
                double incision = shoulder * (0.76 * valley * valley + 0.12 * valley * tributaries);
                double peak = 210 + 105 * fraction(shape, 40);
                height = Math.max(height, peak * profile * (1 - incision));
            }
        }
        return height;
    }

    private static double smooth(double t) {
        return t * t * (3 - 2 * t);
    }

    private static double fraction(long bits, int shift) {
        return ((bits >>> shift) & 1023) / 1023.0;
    }

    private static long mix(long bits) {
        bits = (bits ^ (bits >>> 30)) * 0xbf58476d1ce4e5b9L;
        bits = (bits ^ (bits >>> 27)) * 0x94d049bb133111ebL;
        return bits ^ (bits >>> 31);
    }

    @Override
    public DensityFunction rewriteChildren(DfRewriteRule rule) {
        return new MountainRelief(rule.rewrite(shiftX), rule.rewrite(shiftZ));
    }

    @Override
    public Interval range() {
        return Interval.ofSymmetric(315);
    }

    @Override
    public int domainAxes() {
        return AXIS_X | AXIS_Z;
    }

    @Override
    public MapCodec<MountainRelief> codec() {
        return CODEC;
    }
}
