package com.badgerson.larion.mixin;

import com.badgerson.larion.MaterialRuleContextExtensions;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.material.MaterialRuleContext;
import net.minecraft.world.level.levelgen.material.MaterialSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(MaterialSystem.class)
public abstract class MaterialSystemMixin {
    @Inject(method = "buildSurface", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/world/level/levelgen/material/rule/MaterialRule;compile(Lnet/minecraft/world/level/levelgen/material/MaterialRuleContext;)Lnet/minecraft/world/level/levelgen/material/rule/RuleEvaluator;"))
    private void larion$attachSurfaceChunk(CallbackInfo ci, @Local(argsOnly = true) ChunkAccess chunk,
                                           @Local MaterialRuleContext context) {
        ((MaterialRuleContextExtensions) (Object) context).larion$setChunk(chunk);
    }

    @Inject(method = "topMaterial", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/world/level/levelgen/material/rule/MaterialRule;compile(Lnet/minecraft/world/level/levelgen/material/MaterialRuleContext;)Lnet/minecraft/world/level/levelgen/material/rule/RuleEvaluator;"))
    private void larion$attachTopMaterialChunk(CallbackInfoReturnable<Optional<BlockState>> ci,
                                               @Local(argsOnly = true) ChunkAccess chunk,
                                               @Local MaterialRuleContext context) {
        ((MaterialRuleContextExtensions) (Object) context).larion$setChunk(chunk);
    }
}
