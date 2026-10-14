package spyglassscroll.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.MouseHandler;
import org.joml.Vector2i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import spyglassscroll.SpyglassScroll;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
    @ModifyExpressionValue(method = "onScroll", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/ScrollWheelHandler;onMouseScroll(DD)Lorg/joml/Vector2i;"))
    private Vector2i scrollSpyglass(Vector2i wheel) {
        if (SpyglassScroll.isScoping()) {
            SpyglassScroll.scroll(wheel.y == 0 ? -wheel.x : wheel.y);
            wheel.zero();
        }
        return wheel;
    }
}
