package com.badgerson.larion;

import net.minecraft.world.level.chunk.ChunkAccess;

public interface MaterialRuleContextExtensions {
    void larion$setChunk(ChunkAccess chunk);

    ChunkAccess larion$getChunk();
}
