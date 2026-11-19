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
                if (distance >= radius * 1.34) {
                    continue;
                }

                double angle = Math.atan2(south, east) - 2 * Math.PI * fraction(shape, 30);
                // A longer, gentler flank carries the main spur and a shorter
                // flank exposes a steeper face on the opposite side.
                double span = 1 + 0.19 * Math.cos(angle) + 0.15 * Math.cos(2 * angle - 0.6);
                double radial = distance / (radius * span);
                if (radial >= 1) {
                    continue;
                }
                // Spurs stay attached to the summit. Valleys widen downstream and
                // fade into the same foothill apron as the ridges beside them.
                int valleys = 3 + (int) (3 * fraction(shape, 50));
                double winding = valleys * angle + 2.2 * radial
                        + 0.95 * Math.sin(angle) + 0.48 * Math.sin(2 * angle - 2.8 * radial);
                double valley = 0.5 + 0.5 * Math.cos(winding);
                double profile = smooth(1 - radial);
                double shoulder = radial * smooth(Math.min(1, radial / 0.24));
                double valleyFloor = smooth(Math.clamp((valley - 0.36) / (0.61 - 0.25 * radial), 0, 1));

                // Two ravines fork uphill from each main valley. Their separation
                // shrinks downstream so the cuts converge into a single channel.
                double branch = (1.15 + 0.24 * Math.sin(angle + 0.4)) * (1 - radial);
                double channelDistance = 1 - Math.max(Math.cos(winding - branch), Math.cos(winding + branch));
                double channelWidth = 0.055 + 0.18 * radial;
                double channel = 1 - smooth(Math.min(1, channelDistance / channelWidth));

                // Leave sharper rock ribs between the cuts. Keep them attached to
                // the summit and fade their relief out toward the foot of the slope.
                double ridgeDistance = Math.sqrt(Math.max(0, valley));
                double rib = 1 - smooth(Math.min(1, ridgeDistance / 0.58));
                double incision = shoulder * (0.68 * valleyFloor + 0.37 * channel - 0.31 * rib);
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
