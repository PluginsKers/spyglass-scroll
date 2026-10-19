package spyglassscroll;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Properties;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SpyglassScroll implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("spyglass-scroll");
    private static final Path CONFIG = FabricLoader.getInstance().getConfigDir().resolve("spyglass-scroll.properties");
    private static final Identifier HINT = Identifier.fromNamespaceAndPath("spyglass-scroll", "scroll_hint");
    private static final Identifier HINT_ICON = Identifier.fromNamespaceAndPath("spyglass-scroll", "textures/gui/scroll_hint.png");
    private static final Component HINT_TEXT = Component.translatable("hud.spyglass-scroll.scroll_hint");
    private static double zoom = 1.0, savedZoom = 1.0;

    @Override
    public void onInitializeClient() {
        loadZoom();
        // Save once the spyglass is put away rather than on every scroll step.
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!isScoping()) saveZoom();
        });
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> saveZoom());
        HudElementRegistry.attachElementAfter(VanillaHudElements.MISC_OVERLAYS, HINT, SpyglassScroll::extractHint);
    }

    private static void extractHint(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        if (!isScoping()) return;
        var font = Minecraft.getInstance().font;
        // 13x16 icon 4px in from the bottom-right corner, text vertically centered to its left.
        int x = graphics.guiWidth() - 17, y = graphics.guiHeight() - 20;
        graphics.blit(RenderPipelines.GUI_TEXTURED, HINT_ICON, x, y, 0.0F, 0.0F, 13, 16, 13, 16);
        graphics.text(font, HINT_TEXT, x - 4 - font.width(HINT_TEXT), y + 4, CommonColors.WHITE);
    }

    private static void loadZoom() {
        var properties = new Properties();
        try (var reader = Files.newBufferedReader(CONFIG)) {
            properties.load(reader);
            double saved = Double.parseDouble(properties.getProperty("zoom", "1"));
            if (saved >= 0.2 && saved <= 5.0) zoom = savedZoom = saved;
        } catch (NoSuchFileException ignored) {
        } catch (IOException | IllegalArgumentException e) {
            LOGGER.warn("Failed to read {}", CONFIG, e);
        }
    }

    private static void saveZoom() {
        if (zoom == savedZoom) return;
        savedZoom = zoom;
        try {
            Files.writeString(CONFIG, "zoom=" + zoom + System.lineSeparator());
        } catch (IOException e) {
            LOGGER.warn("Failed to save {}", CONFIG, e);
        }
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
