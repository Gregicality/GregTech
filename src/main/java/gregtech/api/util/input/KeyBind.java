package gregtech.api.util.input;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.core.network.packets.PacketKeysPressed;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Supplier;

public enum KeyBind {

    VANILLA_JUMP(() -> () -> Minecraft.getMinecraft().gameSettings.keyBindJump),
    VANILLA_SNEAK(() -> () -> Minecraft.getMinecraft().gameSettings.keyBindSneak),
    VANILLA_FORWARD(() -> () -> Minecraft.getMinecraft().gameSettings.keyBindForward),
    VANILLA_BACKWARD(() -> () -> Minecraft.getMinecraft().gameSettings.keyBindBack),
    VANILLA_LEFT(() -> () -> Minecraft.getMinecraft().gameSettings.keyBindLeft),
    VANILLA_RIGHT(() -> () -> Minecraft.getMinecraft().gameSettings.keyBindRight),
    ARMOR_MODE_SWITCH("gregtech.key.armor_mode_switch", KeyConflictContext.IN_GAME, Keyboard.KEY_M),
    ARMOR_HOVER("gregtech.key.armor_hover", KeyConflictContext.IN_GAME, Keyboard.KEY_H),
    ARMOR_CANCEL_INERTIA("gregtech.key.armor_cancel_inertia", KeyConflictContext.IN_GAME, Keyboard.KEY_I),
    ARMOR_CHARGING("gregtech.key.armor_charging", KeyConflictContext.IN_GAME, Keyboard.KEY_N),
    TOOL_AOE_CHANGE("gregtech.key.tool_aoe_change", KeyConflictContext.IN_GAME, Keyboard.KEY_V);

    public static final KeyBind[] VALUES = values();

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void onInputEvent(InputEvent.KeyInputEvent event) {
        List<KeyBind> updating = new ArrayList<>();
        for (KeyBind keybind : VALUES) {
            boolean previousPressed = keybind.isPressed;
            boolean previousKeyDown = keybind.isKeyDown;
            keybind.isPressed = keybind.isPressed();
            keybind.isKeyDown = keybind.isKeyDown();
            if (previousPressed != keybind.isPressed || previousKeyDown != keybind.isKeyDown) {
                updating.add(keybind);
            }
        }
        if (!updating.isEmpty()) {
            GregTechAPI.networkHandler.sendToServer(new PacketKeysPressed(updating));
        }
    }

    @SideOnly(Side.CLIENT)
    public static boolean scrollingUp() {
        return Mouse.getEventDWheel() > 0;
    }

    @SideOnly(Side.CLIENT)
    public static boolean notScrolling() {
        return Mouse.getEventDWheel() == 0;
    }

    @SideOnly(Side.CLIENT)
    public static boolean scrollingDown() {
        return Mouse.getEventDWheel() < 0;
    }

    private final Map<EntityPlayerMP, Boolean> keysPressed = new WeakHashMap<>();
    private final Map<EntityPlayerMP, Boolean> keysDown = new WeakHashMap<>();

    @SideOnly(Side.CLIENT)
    private KeyBinding mcKeyBinding;
    @SideOnly(Side.CLIENT)
    private boolean isPressed;
    @SideOnly(Side.CLIENT)
    private boolean isKeyDown;

    /**
     * For Vanilla/Other Mod keybinds
     * <p>
     * Double Supplier keeps client classes from loading
     *
     * @param keybindingSupplier supplier to the client side keybinding
     */
    KeyBind(@NotNull Supplier<Supplier<KeyBinding>> keybindingSupplier) {
        if (FMLCommonHandler.instance().getSide().isClient()) {
            this.mcKeyBinding = keybindingSupplier.get().get();
        }
    }

    KeyBind(@NotNull String langKey, int button) {
        if (FMLCommonHandler.instance().getSide().isClient()) {
            this.mcKeyBinding = new KeyBinding(langKey, button, GTValues.MOD_NAME);
            ClientRegistry.registerKeyBinding(this.mcKeyBinding);
        }
    }

    KeyBind(@NotNull String langKey, @NotNull IKeyConflictContext ctx, int button) {
        if (FMLCommonHandler.instance().getSide().isClient()) {
            this.mcKeyBinding = new KeyBinding(langKey, ctx, button, GTValues.MOD_NAME);
            ClientRegistry.registerKeyBinding(this.mcKeyBinding);
        }
    }

    @SideOnly(Side.CLIENT)
    public KeyBinding toMinecraft() {
        return this.mcKeyBinding;
    }

    @SideOnly(Side.CLIENT)
    public boolean isPressed() {
        return this.mcKeyBinding.isPressed();
    }

    @SideOnly(Side.CLIENT)
    public boolean isKeyDown() {
        return this.mcKeyBinding.isKeyDown();
    }

    @ApiStatus.Internal
    public void updateServerState(@NotNull EntityPlayerMP player, boolean pressed, boolean keyDown) {
        this.keysPressed.put(player, pressed);
        this.keysDown.put(player, keyDown);
    }

    /**
     * Can call on either the {@code Server} or {@code Client} side.
     *
     * @param player the player to test
     * @return if the player pressed the key
     */
    public boolean isPressed(@NotNull EntityPlayer player) {
        if (player.world.isRemote) {
            return isPressed();
        } else {
            return keysPressed.getOrDefault((EntityPlayerMP) player, false);
        }
    }

    /**
     * Can call on either the {@code Server} or {@code Client} side.
     *
     * @param player the player to test
     * @return if the player is holding the key down
     */
    public boolean isKeyDown(@NotNull EntityPlayer player) {
        if (player.world.isRemote) {
            return isKeyDown();
        } else {
            return keysDown.getOrDefault((EntityPlayerMP) player, false);
        }
    }
}
