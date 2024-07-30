package gregtech.api.items.materialitem;

import com.github.bsideup.jabel.Desugar;

import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.stats.IItemDurabilityManager;
import gregtech.api.items.metaitem.stats.TurbineRotor;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.material.properties.RotorProperty2;
import gregtech.api.unification.material.registry.MaterialRegistry;
import gregtech.api.unification.ore.OrePrefix;

import gregtech.api.util.GTUtility;
import gregtech.api.util.GradientUtil;

import gregtech.common.metatileentities.multi.electric.generator.turbine.TurbineType;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import net.minecraft.item.ItemStack;

import net.minecraft.nbt.NBTTagCompound;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public class MetaTurbineItem extends MetaPrefixItem {

    private static final String NBT_ROTOR_DAMAGE = "RotorDamage";
    private final Int2ObjectMap<TurbineRotor> rotorStats = new Int2ObjectOpenHashMap<>();

    private final float optimalFlowMultiplier;
    private final float durabilityMultiplier;
    private final float efficiencyMultiplier;

    public MetaTurbineItem(@NotNull MaterialRegistry registry, @NotNull OrePrefix orePrefix) {
        super(registry, orePrefix);
        if (orePrefix == OrePrefix.turbineSmall) {
            optimalFlowMultiplier = 1.0F;
            durabilityMultiplier = 1.0F;
            efficiencyMultiplier = 0.0F;
        } else if (orePrefix == OrePrefix.turbineNormal) {
            optimalFlowMultiplier = 2.0F;
            durabilityMultiplier = 2.0F;
            efficiencyMultiplier = 2.5F;
        } else if (orePrefix == OrePrefix.turbineLarge) {
            optimalFlowMultiplier = 3.0F;
            durabilityMultiplier = 3.0F;
            efficiencyMultiplier = 5.0F;
        } else {
            // Huge uses 4, 4, 7.5
            throw new IllegalArgumentException("Cannot create MetaTurbineItem for orePrefix " + orePrefix);
        }
    }

    @Override
    protected void attachComponents(@NotNull MetaItem<?>.MetaValueItem valueItem) {
        super.attachComponents(valueItem);
        valueItem.addComponents(new DurabilityManager());
        int meta = valueItem.getMetaValue();
        Material material = getMaterial(meta);
        RotorProperty2 property = material.getProperty(PropertyKey.ROTOR_2);
        if (property != null) {
            rotorStats.put(meta, new TurbineRotorImpl(material, property, optimalFlowMultiplier, efficiencyMultiplier));
        }
    }

    /**
     * @param stack the stack containing the rotor data
     * @return the rotor data
     */
    public @Nullable TurbineRotor getRotorStats(@NotNull ItemStack stack) {
        return rotorStats.get(stack.getMetadata());
    }

    /**
     * @param stack the stack to damage
     * @return if the stack broke
     */
    public boolean damage(@NotNull ItemStack stack, int amount) {
        NBTTagCompound tag = GTUtility.getOrCreateNbtCompound(stack);
        int damage;
        if (tag.hasKey(NBT_ROTOR_DAMAGE)) {
            damage = tag.getInteger(NBT_ROTOR_DAMAGE) - amount;
        } else {
            damage = (int) (durabilityMultiplier * getMaxDamage(stack)) - amount;
        }

        if (damage <= 0) {
            return true;
        }

        tag.setInteger(NBT_ROTOR_DAMAGE, damage);
        return false;
    }

    /**
     * @param stack the stack of the turbine
     * @return the current durability of the turbine
     */
    public int getDurability(@NotNull ItemStack stack) {
        NBTTagCompound tag = GTUtility.getOrCreateNbtCompound(stack);
        if (tag.hasKey(NBT_ROTOR_DAMAGE)) {
            return tag.getInteger(NBT_ROTOR_DAMAGE);
        }

        int damage = getMaxDurability(stack);
        tag.setInteger(NBT_ROTOR_DAMAGE, damage);
        return damage;
    }

    /**
     * @param stack the stack of the turbine
     * @return the max durability of the turbine
     */
    public int getMaxDurability(@NotNull ItemStack stack) {
        RotorProperty2 property = getRotorProperty(stack);
        if (property == null) {
            return 0;
        }
        return (int) (property.durability() * durabilityMultiplier);
    }

    private static @Nullable RotorProperty2 getRotorProperty(@NotNull ItemStack stack) {
        Material material = MetaPrefixItem.tryGetMaterial(stack);
        if (material == null) {
            return null;
        }

        return material.getProperty(PropertyKey.ROTOR_2);
    }

    @Desugar
    private record TurbineRotorImpl(Material material, RotorProperty2 property, float optimalFlowMultiplier,
                                    float efficiencyMultiplier) implements TurbineRotor {

        private TurbineRotorImpl(@NotNull Material material, @NotNull RotorProperty2 property,
                                 float optimalFlowMultiplier, float efficiencyMultiplier) {
            this.material = material;
            this.property = property;
            this.optimalFlowMultiplier = optimalFlowMultiplier;
            this.efficiencyMultiplier = efficiencyMultiplier;
        }

        @Override
        public int color() {
            return material.getMaterialRGB();
        }

        @Override
        public int baseEfficiency() {
            return (int) (property.baseEfficiency() + 1000 * efficiencyMultiplier);
        }

        @Override
        public int optimalFlow() {
            return (int) (property.optimalFlow() * optimalFlowMultiplier);
        }

        @Override
        public int overflowMultiplier() {
            return property.overflowMultiplier();
        }

        @Override
        public float flowMultiplier(@NotNull TurbineType type) {
            return property.flowMultiplier(type);
        }
    }

    private static class DurabilityManager implements IItemDurabilityManager {

        private Pair<Color, Color> durabilityColors;

        @Override
        public double getDurabilityForDisplay(@NotNull ItemStack stack) {
            if (stack.getItem() instanceof MetaTurbineItem turbine) {
                int maxDamage = turbine.getMaxDurability(stack);
                int damage = turbine.getDurability(stack);
                return 1.0 - ((maxDamage - damage) / (double) maxDamage);
            }
            return -1.0;
        }

        @Override
        public @Nullable Pair<Color, Color> getDurabilityColorsForDisplay(@NotNull ItemStack stack) {
            if (durabilityColors == null) {
                Material material = MetaPrefixItem.tryGetMaterial(stack);
                if (material == null) {
                    return null;
                }

                durabilityColors = GradientUtil.getGradient(material.getMaterialRGB(), 10);
            }
            return durabilityColors;
        }
    }
}
