package com.badgerson.larion;

import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.material.MaterialRuleContext;

public final class SomewhatSteepSlopePredicate extends MaterialRuleContext.LazyXZCondition {
    private final int threshold;

    public SomewhatSteepSlopePredicate(MaterialRuleContext context, int threshold) {
        super(context);
        this.threshold = threshold;
    }

    @Override
    protected boolean compute() {
        ChunkAccess chunk = ((MaterialRuleContextExtensions) (Object) context).larion$getChunk();
        int x = context.blockX() & 15;
        int z = context.blockZ() & 15;
        var heightmap = Heightmap.Types.WORLD_SURFACE_WG;
        int height = chunk.getHeight(heightmap, x, z);

        return height - chunk.getHeight(heightmap, x, Math.max(z - 1, 0)) > threshold
                || height - chunk.getHeight(heightmap, x, Math.min(z + 1, 15)) > threshold
                || height - chunk.getHeight(heightmap, Math.max(x - 1, 0), z) > threshold
                || height - chunk.getHeight(heightmap, Math.min(x + 1, 15), z) > threshold;
    }
}
