package com.badgerson.larion.mixin;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.CarverOutput;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NoiseBasedChunkGenerator.class)
public class NoiseChunkGeneratorMixin {
  @ModifyArg(method = "generateCarvers", at = @At(value = "INVOKE",
      target = "Lnet/minecraft/world/level/levelgen/carver/WorldCarver;carve(Lnet/minecraft/world/level/levelgen/WorldGenerationContext;Lnet/minecraft/util/RandomSource;Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/world/level/chunk/CarverOutput;)Z"))
  private CarverOutput larion$limitCarverHeight(CarverOutput output) {
    var generator = (NoiseBasedChunkGenerator) (Object) this;
    if (!generator.stable(NoiseGeneratorSettings.OVERWORLD) || generator.getMinY() != -128) {
      return output;
    }
    return new CarverOutput() {
      @Override
      public void carve(int x, int y, int z) {
        if (y < 80) {
          output.carve(x, y, z);
        }
      }

      @Override
      public int minY() {
        return output.minY();
      }

      @Override
      public int maxY() {
        return Math.min(output.maxY(), 79);
      }
    };
  }

  @Inject(method = "createFluidPicker", at = @At("HEAD"), cancellable = true)
  private static void createFluidPicker(NoiseGeneratorSettings settings,
      CallbackInfoReturnable<Aquifer.FluidPicker> ci) {
    // This mixin modifies the aquifer generator so that the "bedrock lava
    // level" (in vanilla hardcoded to -54) is instead determined by the world's
    // minimum Y level, allowing for deeper caves without them being flooded.
    // Ideally the lava sea level would be a field in the noise settings
    // like normal sea level, but this is easier to implement
    var lavaSeaLevel = settings.noiseSettings().minY() + 10;
    Aquifer.FluidStatus lavaFluidStatus = new Aquifer.FluidStatus(lavaSeaLevel, Blocks.LAVA.defaultBlockState());
    int i = settings.seaLevel();
    Aquifer.FluidStatus defaultFluidStatus = new Aquifer.FluidStatus(i, settings.defaultFluid());
    // AquiferSampler.FluidLevel fluidLevel3 = new
    // AquiferSampler.FluidLevel(DimensionType.MIN_HEIGHT * 2,
    // Blocks.AIR.getDefaultState());
    ci.setReturnValue((x, y, z) -> y < Math.min(lavaSeaLevel, i) ? lavaFluidStatus : defaultFluidStatus);
  }
}
