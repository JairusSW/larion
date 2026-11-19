package com.badgerson.larion.mixin;

import com.badgerson.larion.MaterialRuleContextExtensions;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.material.MaterialRuleContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(MaterialRuleContext.class)
public abstract class MaterialRuleContextMixin implements MaterialRuleContextExtensions {
    @Unique
    private ChunkAccess larion$chunk;

    @Override
    public void larion$setChunk(ChunkAccess chunk) {
        larion$chunk = chunk;
    }

    @Override
    public ChunkAccess larion$getChunk() {
        return larion$chunk;
    }
}
