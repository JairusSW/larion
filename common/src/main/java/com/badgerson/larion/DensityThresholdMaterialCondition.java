package com.badgerson.larion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.material.MaterialRuleContext;
import net.minecraft.world.level.levelgen.material.condition.ConditionEvaluator;
import net.minecraft.world.level.levelgen.material.condition.MaterialCondition;

public record DensityThresholdMaterialCondition(DensityFunction density, float threshold) implements MaterialCondition {
    public static final MapCodec<DensityThresholdMaterialCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(DensityFunction.CODEC.fieldOf("density").forGetter(DensityThresholdMaterialCondition::density),
                    Codec.FLOAT.fieldOf("threshold").forGetter(DensityThresholdMaterialCondition::threshold))
            .apply(instance, DensityThresholdMaterialCondition::new));

    @Override
    public ConditionEvaluator compile(MaterialRuleContext context) {
        var sampler = context.getDensitiesInChunk(density, false);
        return () -> sampler.get() >= threshold;
    }

    @Override
    public MapCodec<DensityThresholdMaterialCondition> codec() {
        return CODEC;
    }
}
