package spyglassscroll.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;
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

    @WrapOperation(method = "turnPlayer", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"))
    private void spyglassSensitivity(LocalPlayer player, double yaw, double pitch, Operation<Void> turn) {
        double sensitivity = SpyglassScroll.sensitivity();
        turn.call(player, yaw * sensitivity, pitch * sensitivity);
    }
}
