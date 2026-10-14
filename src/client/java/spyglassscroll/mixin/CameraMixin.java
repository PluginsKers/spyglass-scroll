package spyglassscroll.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import spyglassscroll.SpyglassScroll;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @ModifyExpressionValue(method = "tickFov", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/player/AbstractClientPlayer;getFieldOfViewModifier(ZF)F"))
    private float spyglassFov(float original) {
        return SpyglassScroll.isScoping() ? original / SpyglassScroll.zoom() : original;
    }

    @ModifyConstant(method = "tickFov", constant = @Constant(floatValue = 0.1F))
    private float spyglassMinimumFov(float original) {
        // Vanilla's lower bound otherwise prevents zooming beyond the default spyglass magnification.
        return SpyglassScroll.isScoping() ? original / Math.max(1.0F, SpyglassScroll.zoom()) : original;
    }
}
