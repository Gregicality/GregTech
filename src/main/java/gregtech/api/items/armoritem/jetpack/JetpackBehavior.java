package gregtech.api.items.armoritem.jetpack;

import gregtech.api.items.armoritem.ArmorHelper;
import gregtech.api.items.armoritem.IArmorBehavior;
import gregtech.api.util.input.KeyBind;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public abstract class JetpackBehavior implements IArmorBehavior {

    private final IJetpackStats jetpackStats;

    public JetpackBehavior(IJetpackStats jetpackStats) {
        this.jetpackStats = jetpackStats;
    }

    /** How much Fuel (or Energy) to use per tick while flying. Can be modified by stats in {@link IJetpackStats}. */
    protected abstract int getFuelPerUse();

    /** Drain the specified amount of Fuel (or Energy) from this Jetpack. */
    protected abstract boolean drainFuel(@NotNull ItemStack stack, int amount, boolean simulate);

    @Override
    public Set<KeyBind> getListenedKeys() {
        return Collections.singleton(KeyBind.ARMOR_HOVER);
    }

    @Override
    public void onKeyPressed(@NotNull ItemStack stack, @NotNull EntityPlayer player, KeyBind keyPressed) {
        if (keyPressed == KeyBind.ARMOR_HOVER) {
            NBTTagCompound tag = ArmorHelper.getBehaviorsTag(stack);
            boolean wasHover = tag.getBoolean(ArmorHelper.JETPACK_HOVER_KEY);
            tag.setBoolean(ArmorHelper.JETPACK_HOVER_KEY, !wasHover);

            if (wasHover) {
                player.sendStatusMessage(new TextComponentTranslation("metaarmor.jetpack.hover.disable"), true);
            } else {
                player.sendStatusMessage(new TextComponentTranslation("metaarmor.jetpack.hover.enable"), true);
            }
        }
    }

    @Override
    public void onArmorTick(@NotNull World world, @NotNull EntityPlayer player, @NotNull ItemStack stack) {
        NBTTagCompound tag = ArmorHelper.getBehaviorsTag(stack);
        boolean hover = tag.getBoolean(ArmorHelper.JETPACK_HOVER_KEY);
        performFlying(player, stack, hover);
    }

    /*
     * Called every tick, performs flying if the correct keys are pressed.
     * Logic from SimplyJetpacks2:
     * https://github.com/Tomson124/SimplyJetpacks2/blob/1.12/src/main/java/tonius/simplyjetpacks/item/ItemJetpack.java
     */
    private void performFlying(@NotNull EntityPlayer player, @NotNull ItemStack stack, boolean hover) {
        double currentAccel = jetpackStats.getVerticalAcceleration() * (player.motionY < 0.3D ? 2.5D : 1.0D);
        double currentSpeedVertical = jetpackStats.getVerticalSpeed() * (player.isInWater() ? 0.4D : 1.0D);
        boolean flyKeyDown = KeyBind.VANILLA_JUMP.isKeyDown(player);
        boolean descendKeyDown = KeyBind.VANILLA_SNEAK.isKeyDown(player);

        if (flyKeyDown || hover && !player.onGround) {
            if (!player.isInWater() && !player.isInLava() && drainFuel(stack, getFuelPerUse(), true)) {
                drainFuel(stack,
                        (int) (player.isSprinting() ?
                                Math.round(getFuelPerUse() * jetpackStats.getSprintEnergyModifier()) : getFuelPerUse()),
                        false);

                if (flyKeyDown) {
                    if (!hover) {
                        player.motionY = Math.min(player.motionY + currentAccel, currentSpeedVertical);
                    } else {
                        if (descendKeyDown) player.motionY = Math.min(player.motionY + currentAccel,
                                jetpackStats.getVerticalHoverSlowSpeed());
                        else player.motionY = Math.min(player.motionY + currentAccel,
                                jetpackStats.getVerticalHoverSpeed());
                    }
                } else if (descendKeyDown) {
                    player.motionY = Math.min(player.motionY + currentAccel, -jetpackStats.getVerticalHoverSpeed());
                } else {
                    player.motionY = Math.min(player.motionY + currentAccel, -jetpackStats.getVerticalHoverSlowSpeed());
                }
                float speedSideways = (float) (player.isSneaking() ? jetpackStats.getSidewaysSpeed() * 0.5f :
                        jetpackStats.getSidewaysSpeed());
                float speedForward = (float) (player.isSprinting() ?
                        speedSideways * jetpackStats.getSprintSpeedModifier() : speedSideways);

                if (KeyBind.VANILLA_FORWARD.isKeyDown(player))
                    player.moveRelative(0, 0, speedForward, speedForward);
                if (KeyBind.VANILLA_BACKWARD.isKeyDown(player))
                    player.moveRelative(0, 0, -speedSideways, speedSideways * 0.8f);
                if (KeyBind.VANILLA_LEFT.isKeyDown(player))
                    player.moveRelative(speedSideways, 0, 0, speedSideways);
                if (KeyBind.VANILLA_RIGHT.isKeyDown(player))
                    player.moveRelative(-speedSideways, 0, 0, speedSideways);
                if (!player.getEntityWorld().isRemote) {
                    player.fallDistance = 0;
                }
                spawnParticle(player.getEntityWorld(), player, jetpackStats.getParticle());
            }
        }
        // TODO Reset player floating time?
        // TODO Emergency hover?
        // TODO Jetpack sfx?
    }

    /* Spawn particle behind player with speedY speed */
    private static void spawnParticle(@NotNull World world, EntityPlayer player, EnumParticleTypes type) {
        if (type != null && world.isRemote) {
            Vec3d forward = player.getForward();
            world.spawnParticle(type, player.posX - forward.x, player.posY + 0.5D, player.posZ - forward.z, 0.0D, -0.6D,
                    0.0D);
        }
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable World world, @NotNull List<String> tooltip) {
        addInformation(stack, tooltip);
    }

    @Override
    public void addHudInformation(@NotNull ItemStack stack, @NotNull List<String> hudText) {
        addInformation(stack, hudText);
    }

    private void addInformation(ItemStack stack, List<String> tooltip) {
        NBTTagCompound tag = ArmorHelper.getBehaviorsTag(stack);
        String status;
        if (tag.getBoolean(ArmorHelper.JETPACK_HOVER_KEY)) {
            status = I18n.format("metaarmor.hud.status.enabled");
        } else {
            status = I18n.format("metaarmor.hud.status.disabled");
        }
        tooltip.add(I18n.format("metaarmor.hud.hover_mode", status));
    }
}
