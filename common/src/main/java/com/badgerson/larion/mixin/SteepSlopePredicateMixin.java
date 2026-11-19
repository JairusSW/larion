package com.badgerson.larion.mixin;

import com.badgerson.larion.SomewhatSteepSlopePredicate;
import net.minecraft.world.level.levelgen.material.MaterialRuleContext;
import net.minecraft.world.level.levelgen.material.condition.ConditionEvaluator;
import net.minecraft.world.level.levelgen.material.condition.SteepCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SteepCondition.class)
public abstract class SteepSlopePredicateMixin {
    @Inject(method = "compile", at = @At("HEAD"), cancellable = true)
    private void larion$compileSteepSlope(MaterialRuleContext context, CallbackInfoReturnable<ConditionEvaluator> ci) {
        ci.setReturnValue(new SomewhatSteepSlopePredicate(context, 3));
    }
}
