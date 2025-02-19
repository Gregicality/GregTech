package gregtech.api.util.input;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.util.GTLog;
import gregtech.core.network.packets.PacketKeysPressed;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.apache.commons.lang3.tuple.MutablePair;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.List;
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

    public static void init() {
        GTLog.logger.info("Registering KeyBinds");
        if (FMLCommonHandler.instance().getSide().isClient()) {
            MinecraftForge.EVENT_BUS.register(KeyBind.class);
        }
    }

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

    @SideOnly(Side.CLIENT)
    private KeyBinding keybinding;
    @SideOnly(Side.CLIENT)
    private boolean isPressed, isKeyDown;

    private final WeakHashMap<EntityPlayerMP, MutablePair<Boolean, Boolean>> mapping = new WeakHashMap<>();

    // For Vanilla/Other Mod keybinds
    // Double Supplier to keep client classes from loading
    KeyBind(Supplier<Supplier<KeyBinding>> keybindingGetter) {
        if (FMLCommonHandler.instance().getSide().isClient()) {
            this.keybinding = keybindingGetter.get().get();
        }
    }

    KeyBind(String langKey, int button) {
        if (FMLCommonHandler.instance().getSide().isClient()) {
            this.keybinding = new KeyBinding(langKey, button, GTValues.MOD_NAME);
            ClientRegistry.registerKeyBinding(this.keybinding);
        }
    }

    KeyBind(String langKey, IKeyConflictContext ctx, int button) {
        if (FMLCommonHandler.instance().getSide().isClient()) {
            this.keybinding = new KeyBinding(langKey, ctx, button, GTValues.MOD_NAME);
            ClientRegistry.registerKeyBinding(this.keybinding);
        }
    }

    @SideOnly(Side.CLIENT)
    public KeyBinding toMinecraft() {
        return this.keybinding;
    }

    @SideOnly(Side.CLIENT)
    public boolean isPressed() {
        return this.keybinding.isPressed();
    }

    @SideOnly(Side.CLIENT)
    public boolean isKeyDown() {
        return this.keybinding.isKeyDown();
    }

    public void update(boolean pressed, boolean keyDown, EntityPlayerMP player) {
        MutablePair<Boolean, Boolean> pair = this.mapping.get(player);
        if (pair == null) {
            this.mapping.put(player, MutablePair.of(pressed, keyDown));
        } else {
            pair.left = pressed;
            pair.right = keyDown;
        }
    }

    public boolean isPressed(EntityPlayer player) {
        if (player.world.isRemote) {
            return isPressed();
        } else {
            MutablePair<Boolean, Boolean> pair = this.mapping.get((EntityPlayerMP) player);
            return pair != null && pair.left;
        }
    }

    public boolean isKeyDown(EntityPlayer player) {
        if (player.world.isRemote) {
            return isKeyDown();
        } else {
            MutablePair<Boolean, Boolean> pair = this.mapping.get((EntityPlayerMP) player);
            return pair != null && pair.right;
        }
    }
}
