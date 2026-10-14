package spyglassscroll;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;

public final class SpyglassScroll implements ClientModInitializer {
    private static double zoom = 1.0;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!isScoping()) zoom = 1.0;
        });
    }

    public static boolean isScoping() {
        var client = Minecraft.getInstance();
        return client.player != null && client.getCameraEntity() == client.player
                && client.options.getCameraType().isFirstPerson() && client.player.isScoping();
    }

    public static void scroll(int steps) {
        zoom = Math.clamp(zoom * Math.pow(1.25, steps), 0.2, 5.0);
    }

    public static float zoom() { return (float) zoom; }

    public static double sensitivity() {
        if (!isScoping()) return 1.0;
        // Smooth camera skips vanilla's scoped slowdown, so it only slows down past the default magnification.
        return 1.0 / (Minecraft.getInstance().options.smoothCamera ? Math.max(1.0, zoom) : zoom);
    }
}
