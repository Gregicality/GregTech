package gregtech.common.items.armor;

import gregtech.api.items.armor.ArmorUtils;
import gregtech.api.util.input.KeyBind;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;

import org.jetbrains.annotations.NotNull;

/**
 * Logic from SimplyJetpacks2:
 * https://github.com/Tomson124/SimplyJetpacks2/blob/1.12/src/main/java/tonius/simplyjetpacks/item/ItemJetpack.java
 */
public interface IJetpack {

    default double getSprintEnergyModifier() {
        return 1.0D;
    }

    default double getSprintSpeedModifier() {
        return 1.0D;
    }

    default double getVerticalHoverSpeed() {
        return 0.18D;
    }

    default double getVerticalHoverSlowSpeed() {
        return 0.14D;
    }

    default double getVerticalAcceleration() {
        return 0.1D;
    }

    default double getVerticalSpeed() {
        return 0.22D;
    }

    default double getSidewaysSpeed() {
        return 0.0D;
    }

    default EnumParticleTypes getParticle() {
        return EnumParticleTypes.SMOKE_LARGE;
    }

    default float getFallDamageReduction() {
        return 0.0f;
    }

    int getEnergyPerUse();

    boolean canUseEnergy(ItemStack stack, int amount);

    void drainEnergy(ItemStack stack, int amount);

    boolean hasEnergy(ItemStack stack);

    default void performFlying(@NotNull EntityPlayer player, boolean hover, boolean cancelInertia, ItemStack stack) {
        double currentAccel = getVerticalAcceleration() * (player.motionY < 0.3D ? 2.5D : 1.0D);
        double currentSpeedVertical = getVerticalSpeed() * (player.isInWater() ? 0.4D : 1.0D);
        boolean flyKeyDown = KeyBind.VANILLA_JUMP.isKeyDown(player);
        boolean descendKeyDown = KeyBind.VANILLA_SNEAK.isKeyDown(player);

        if (!player.isInWater() && !player.isInLava() && canUseEnergy(stack, getEnergyPerUse())) {
            if (flyKeyDown || hover && !player.onGround) {
                drainEnergy(stack, (int) (player.isSprinting() ?
                        Math.round(getEnergyPerUse() * getSprintEnergyModifier()) : getEnergyPerUse()));

                if (hasEnergy(stack)) {
                    if (flyKeyDown) {
                        if (!hover) {
                            player.motionY = Math.min(player.motionY + currentAccel, currentSpeedVertical);
                        } else {
                            if (descendKeyDown)
                                player.motionY = Math.min(player.motionY + currentAccel, getVerticalHoverSlowSpeed());
                            else player.motionY = Math.min(player.motionY + currentAccel, getVerticalHoverSpeed());
                        }
                    } else if (descendKeyDown) {
                        player.motionY = Math.min(player.motionY + currentAccel, -getVerticalHoverSpeed());
                    } else {
                        player.motionY = Math.min(player.motionY + currentAccel, -getVerticalHoverSlowSpeed());
                    }
                    float speedSideways = (float) (player.isSneaking() ? getSidewaysSpeed() * 0.5f :
                            getSidewaysSpeed());
                    float speedForward = (float) (player.isSprinting() ? speedSideways * getSprintSpeedModifier() :
                            speedSideways);

                    boolean anyKeyPressed = flyKeyDown || descendKeyDown;
                    if (KeyBind.VANILLA_FORWARD.isKeyDown(player)) {
                        player.moveRelative(0, 0, speedForward, speedForward);
                        anyKeyPressed = true;
                    }
                    if (KeyBind.VANILLA_BACKWARD.isKeyDown(player)) {
                        player.moveRelative(0, 0, -speedSideways, speedSideways * 0.8f);
                        anyKeyPressed = true;
                    }
                    if (KeyBind.VANILLA_LEFT.isKeyDown(player)) {
                        player.moveRelative(speedSideways, 0, 0, speedSideways);
                        anyKeyPressed = true;
                    }
                    if (KeyBind.VANILLA_RIGHT.isKeyDown(player)) {
                        player.moveRelative(-speedSideways, 0, 0, speedSideways);
                        anyKeyPressed = true;
                    }

                    /* hover check not needed, because of "flyKeyDown || hover && !player.onGround" check */
                    if (!anyKeyPressed && cancelInertia) {
                        player.motionX *= 0.75;
                        player.motionY *= 0.75;
                        player.motionZ *= 0.75;
                    }

                    if (!player.getEntityWorld().isRemote) {
                        player.fallDistance = 0;
                    }

                }
                ArmorUtils.spawnParticle(player.getEntityWorld(), player, getParticle(), -0.6D);
            }
        }
    }
}
