package spyglassscroll;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

public final class SpyglassClientGameTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        context.runOnClient(client -> client.options.menuBackgroundBlurriness().set(0));
        context.getInput().resizeWindow(1280, 720);
        try (var world = context.worldBuilder().create()) {
            world.getServer().runOnServer(server -> {
                var player = server.getPlayerList().getPlayers().getFirst();
                player.getInventory().setItem(0, new ItemStack(Items.SPYGLASS));
                player.getInventory().setItem(1, new ItemStack(Items.STONE));
                player.getInventory().setSelectedSlot(0);
                player.inventoryMenu.broadcastChanges();
                var target = player.blockPosition().offset(0, 0, 32);
                for (int x = -3; x <= 3; x++) {
                    for (int y = 0; y < 7; y++) {
                        player.level().setBlockAndUpdate(target.offset(x, y, 0),
                                (x == 0 ? Blocks.CHISELED_STONE_BRICKS : Blocks.STONE_BRICKS).defaultBlockState());
                    }
                }
            });
            sync(context, world);
            context.runOnClient(client -> {
                client.player.getInventory().setSelectedSlot(0);
                client.player.setYRot(0);
                client.player.setXRot(0);
            });
            context.getInput().holdMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            context.waitFor(client -> client.player.isScoping());
            settle(context);
            float defaultFov = context.computeOnClient(client -> fov(client));
            context.takeScreenshot("spyglass-default");
            context.getInput().scroll(2);
            settle(context);
            checkFov(context, defaultFov / 1.5625F);
            checkSensitivity(context, 1 / 1.5625);
            context.runOnClient(client -> check(client.player.getInventory().getSelectedSlot() == 0, "Scoped scroll keeps the selected hotbar slot"));
            context.getInput().scroll(-2);
            settle(context);
            checkFov(context, defaultFov);
            checkSensitivity(context, 1.0);
            context.getInput().scroll(100);
            settle(context);
            checkFov(context, defaultFov / 5.0F);
            checkSensitivity(context, 0.2);
            context.takeScreenshot("spyglass-maximum");
            context.getInput().scroll(-100);
            settle(context);
            checkFov(context, defaultFov * 5.0F);
            checkSensitivity(context, 5.0);
            context.runOnClient(client -> client.options.smoothCamera = true);
            checkSensitivity(context, 1.0);
            context.runOnClient(client -> client.options.smoothCamera = false);

            context.setScreen(() -> new InventoryScreen(Minecraft.getInstance().player));
            context.getInput().scroll(2);
            context.runOnClient(client -> check(client.player.getInventory().getSelectedSlot() == 0, "Inventory scroll never changes the hotbar"));
            context.setScreen(() -> null);
            context.getInput().releaseMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            context.waitFor(client -> !client.player.isScoping());
            settle(context);
            context.runOnClient(client -> check(SpyglassScroll.zoom() == 1.0F, "Releasing the spyglass resets zoom"));
            context.getInput().holdMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            context.waitFor(client -> client.player.isScoping());
            settle(context);
            checkFov(context, defaultFov);
            context.getInput().releaseMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            context.waitFor(client -> !client.player.isScoping());
            context.getInput().scroll(-1);
            context.waitFor(client -> client.player.getInventory().getSelectedSlot() == 1);

            sync(context, world);
            world.getServer().runOnServer(server -> {
                var player = server.getPlayerList().getPlayers().getFirst();
                player.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.SPYGLASS));
                player.getInventory().setItem(1, ItemStack.EMPTY);
                player.inventoryMenu.broadcastChanges();
            });
            sync(context, world);
            context.getInput().holdMouse(InputConstants.MOUSE_BUTTON_RIGHT);
            context.waitFor(client -> client.player.isScoping());
            context.getInput().scroll(1);
            settle(context);
            checkFov(context, defaultFov / 1.25F);
            checkSensitivity(context, 0.8);
            context.runOnClient(client -> check(client.player.getInventory().getSelectedSlot() == 1, "Offhand spyglass scroll keeps the selected hotbar slot"));
            context.getInput().releaseMouse(InputConstants.MOUSE_BUTTON_RIGHT);
        }
        context.waitTick();
        context.runOnClient(client -> check(SpyglassScroll.zoom() == 1.0F, "Leaving a world clears zoom"));
    }

    private static void settle(ClientGameTestContext context) {
        for (int i = 0; i < 20; i++) context.waitTick();
    }

    private static float fov(Minecraft client) {
        //? if >=26.1 {
        return client.gameRenderer.mainCamera().getFov();
        //?} else {
        /*try {
            var method = net.minecraft.client.renderer.GameRenderer.class.getDeclaredMethod("getFov", net.minecraft.client.Camera.class, float.class, boolean.class);
            method.setAccessible(true);
            return (float) method.invoke(client.gameRenderer, client.gameRenderer.getMainCamera(), 1.0F, true);
        } catch (ReflectiveOperationException e) { throw new AssertionError(e); }
        *///?}
    }

    private static void checkFov(ClientGameTestContext context, float expected) {
        context.runOnClient(client -> {
            float actual = fov(client);
            check(Math.abs(actual - expected) < 0.05F, "Expected FOV " + expected + ", got " + actual);
        });
    }

    private static void checkSensitivity(ClientGameTestContext context, double expected) {
        context.runOnClient(client -> {
            double actual = SpyglassScroll.sensitivity();
            check(Math.abs(actual - expected) < 1.0E-6, "Expected sensitivity " + expected + ", got " + actual);
        });
    }

    private static void sync(ClientGameTestContext context, TestSingleplayerContext world) {
        //? if >=26.2 {
        world.getConnection().waitForServerboundPackets();
        world.getConnection().waitForClientboundPackets();
        //?} else {
        /*context.waitTicks(2);
        *///?}
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
