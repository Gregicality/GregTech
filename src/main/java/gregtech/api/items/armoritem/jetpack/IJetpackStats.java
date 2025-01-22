package gregtech.api.items.armoritem.jetpack;

import net.minecraft.util.EnumParticleTypes;

import org.jetbrains.annotations.Nullable;

public interface IJetpackStats {

    double getSprintEnergyModifier();

    double getSprintSpeedModifier();

    double getVerticalHoverSpeed();

    double getVerticalHoverSlowSpeed();

    double getVerticalAcceleration();

    double getVerticalSpeed();

    double getSidewaysSpeed();

    float getFallDamageReduction();

    @Nullable
    EnumParticleTypes getParticle();
}
