package gregtech.api.unification.material;

import gregtech.api.GregTechAPI;
import gregtech.api.fluids.FluidBuilder;
import gregtech.api.fluids.FluidState;
import gregtech.api.fluids.store.FluidStorageKey;
import gregtech.api.fluids.store.FluidStorageKeys;
import gregtech.api.unification.Element;
import gregtech.api.unification.Elements;
import gregtech.api.unification.material.info.MaterialFlag;
import gregtech.api.unification.material.info.MaterialFlags;
import gregtech.api.unification.material.info.MaterialIconSet;
import gregtech.api.unification.material.properties.*;
import gregtech.api.unification.material.registry.MaterialRegistry;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.api.util.FluidTooltipUtil;
import gregtech.api.util.GTUtility;
import gregtech.api.util.LocalizationUtils;
import gregtech.api.util.SmallDigits;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import crafttweaker.annotations.ZenRegister;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import stanhebben.zenscript.annotations.OperatorType;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;
import stanhebben.zenscript.annotations.ZenMethod;
import stanhebben.zenscript.annotations.ZenOperator;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

@ZenClass("mods.gregtech.material.Material")
@ZenRegister
public class Material implements Comparable<Material> {

    /**
     * Basic Info of this Material.
     *
     * @see MaterialInfo
     */
    @NotNull
    private final MaterialInfo materialInfo;

    /**
     * Properties of this Material.
     *
     * @see MaterialProperties
     */
    @NotNull
    private final MaterialProperties properties;

    /**
     * Generation flags of this material
     *
     * @see MaterialFlags
     */
    @NotNull
    private final MaterialFlags flags;

    /**
     * Chemical formula of this material
     */
    private String chemicalFormula;

    @NotNull
    private String calculateChemicalFormula() {
        if (chemicalFormula != null) return this.chemicalFormula;
        if (materialInfo.element != null) {
            return materialInfo.element.getSymbol();
        }
        if (!materialInfo.componentList.isEmpty()) {
            // prevent parenthesis around single component materials
            if (materialInfo.componentList.size() == 1) {
                MaterialStack stack = materialInfo.componentList.get(0);
                if (stack.amount == 1) {
                    return stack.material.getChemicalFormula();
                }
            }
            StringBuilder components = new StringBuilder();
            for (MaterialStack component : materialInfo.componentList)
                components.append(component.toFormatted());
            return components.toString();
        }
        return "";
    }

    @ZenGetter
    public String getChemicalFormula() {
        return chemicalFormula;
    }

    @ZenMethod
    public Material setFormula(String formula) {
        return setFormula(formula, false);
    }

    @ZenMethod
    public Material setFormula(String formula, boolean withFormatting) {
        this.chemicalFormula = withFormatting ? SmallDigits.toSmallDownNumbers(formula) : formula;
        return this;
    }

    public ImmutableList<MaterialStack> getMaterialComponents() {
        return materialInfo.componentList;
    }

    @ZenGetter("components")
    public MaterialStack[] getMaterialComponentsCt() {
        return getMaterialComponents().toArray(new MaterialStack[0]);
    }

    private Material(@NotNull MaterialInfo materialInfo, @NotNull MaterialProperties properties,
                     @NotNull MaterialFlags flags) {
        this.materialInfo = materialInfo;
        this.properties = properties;
        this.flags = flags;
        this.properties.setMaterial(this);
        registerMaterial();
    }

    // thou shall not call
    protected Material(@NotNull ResourceLocation resourceLocation) {
        materialInfo = new MaterialInfo(0, resourceLocation);
        materialInfo.iconSet = MaterialIconSet.DULL;
        properties = new MaterialProperties();
        flags = new MaterialFlags();
    }

    protected void registerMaterial() {
        verifyMaterial();
        GregTechAPI.materialManager.getRegistry(getModid()).register(this);
    }

    public void addFlags(MaterialFlag... flags) {
        if (GregTechAPI.materialManager.canModifyMaterials()) {
            this.flags.addFlags(flags).verify(this);
        } else throw new IllegalStateException("Cannot add flag to material when registry is frozen!");
    }

    @ZenMethod
    public void addFlags(String... names) {
        addFlags(Arrays.stream(names).map(MaterialFlag::getByName).filter(Objects::nonNull)
                .toArray(MaterialFlag[]::new));
    }

    public boolean hasFlag(MaterialFlag flag) {
        return flags.hasFlag(flag);
    }

    public boolean isElement() {
        return materialInfo.element != null;
    }

    @Nullable
    public Element getElement() {
        return materialInfo.element;
    }

    public boolean hasFlags(MaterialFlag... flags) {
        return Arrays.stream(flags).allMatch(this::hasFlag);
    }

    public boolean hasAnyOfFlags(MaterialFlag... flags) {
        return Arrays.stream(flags).anyMatch(this::hasFlag);
    }

    protected void calculateDecompositionType() {
        if (!materialInfo.componentList.isEmpty() && !hasFlag(MaterialFlags.DECOMPOSITION_BY_CENTRIFUGING) &&
                !hasFlag(MaterialFlags.DECOMPOSITION_BY_ELECTROLYZING) &&
                !hasFlag(MaterialFlags.DISABLE_DECOMPOSITION)) {
            boolean onlyMetalMaterials = true;
            for (MaterialStack materialStack : materialInfo.componentList) {
                Material material = materialStack.material;
                onlyMetalMaterials &= material.hasProperty(PropertyKey.INGOT);
            }
            // allow centrifuging of alloy materials only
            if (onlyMetalMaterials) {
                flags.addFlags(MaterialFlags.DECOMPOSITION_BY_CENTRIFUGING);
            } else {
                flags.addFlags(MaterialFlags.DECOMPOSITION_BY_ELECTROLYZING);
            }
        }
    }

    /**
     * Retrieves a fluid from the material. Attempts to retrieve with {@link FluidProperty#getPrimaryKey()},
     * {@link FluidStorageKeys#LIQUID} and {@link FluidStorageKeys#GAS}.
     *
     * @return the fluid
     * @see #getFluid(FluidStorageKey)
     */
    public Fluid getFluid() {
        FluidProperty prop = getProperty(PropertyKey.FLUID);
        if (prop == null) {
            throw new IllegalArgumentException("Material " + getResourceLocation() + " does not have a Fluid!");
        }

        Fluid fluid = prop.get(prop.getPrimaryKey());
        if (fluid != null) return fluid;

        fluid = getFluid(FluidStorageKeys.LIQUID);
        if (fluid != null) return fluid;

        return getFluid(FluidStorageKeys.GAS);
    }

    /**
     * @param key the key for the fluid
     * @return the fluid corresponding with the key
     */
    public Fluid getFluid(@NotNull FluidStorageKey key) {
        FluidProperty prop = getProperty(PropertyKey.FLUID);
        if (prop == null) {
            throw new IllegalArgumentException("Material " + getResourceLocation() + " does not have a Fluid!");
        }

        return prop.get(key);
    }

    /**
     * @param amount the amount the FluidStack should have
     * @return a FluidStack with the fluid and amount
     * @see #getFluid(FluidStorageKey, int)
     */
    public FluidStack getFluid(int amount) {
        return new FluidStack(getFluid(), amount);
    }

    /**
     * @param key    the key for the fluid
     * @param amount the amount the FluidStack should have
     * @return a FluidStack with the fluid and amount
     */
    public FluidStack getFluid(@NotNull FluidStorageKey key, int amount) {
        return new FluidStack(getFluid(key), amount);
    }

    public int getBlockHarvestLevel() {
        if (!hasProperty(PropertyKey.DUST)) {
            throw new IllegalArgumentException(
                    "Material " + getResourceLocation() + " does not have a harvest level! Is probably a Fluid");
        }
        int harvestLevel = getProperty(PropertyKey.DUST).getHarvestLevel();
        return harvestLevel > 0 ? harvestLevel - 1 : harvestLevel;
    }

    public int getToolHarvestLevel() {
        if (!hasProperty(PropertyKey.TOOL)) {
            throw new IllegalArgumentException("Material " + getResourceLocation() +
                    " does not have a tool harvest level! Is probably not a Tool Material");
        }
        return getProperty(PropertyKey.TOOL).getToolHarvestLevel();
    }

    @ZenMethod
    public void setMaterialRGB(int materialRGB) {
        materialInfo.color = materialRGB;
    }

    @ZenGetter("materialRGB")
    public int getMaterialRGB() {
        return materialInfo.color;
    }

    public void setMaterialIconSet(MaterialIconSet materialIconSet) {
        materialInfo.iconSet = materialIconSet;
    }

    public MaterialIconSet getMaterialIconSet() {
        return materialInfo.iconSet;
    }

    @ZenGetter("radioactive")
    public boolean isRadioactive() {
        if (materialInfo.element != null) return materialInfo.element.halfLifeSeconds >= 0;
        for (MaterialStack stack : materialInfo.componentList)
            if (stack.material.isRadioactive()) return true;
        return false;
    }

    // Assumes one mole per item and that it's always "starting to decay"
    @ZenGetter("decaysPerSecond")
    public double getDecaysPerSecond() {
        if (!this.isRadioactive()) {
            return 0;
        }
        if (materialInfo.element != null) {
            return 6e23 * (Math.log(2) * Math.exp(-Math.log(2) / materialInfo.element.halfLifeSeconds));
        }
        double decaysPerSecond = 0;
        for (MaterialStack stack : materialInfo.componentList)
            decaysPerSecond += stack.material.getDecaysPerSecond();
        return decaysPerSecond;
    }

    @ZenGetter("protons")
    public long getProtons() {
        if (materialInfo.element != null) return materialInfo.element.getProtons();
        if (materialInfo.componentList.isEmpty()) return Math.max(1, Elements.Tc.getProtons());
        long totalProtons = 0, totalAmount = 0;
        for (MaterialStack stack : materialInfo.componentList) {
            totalAmount += stack.amount;
            totalProtons += stack.amount * stack.material.getProtons();
        }
        return totalProtons / totalAmount;
    }

    @ZenGetter("neutrons")
    public long getNeutrons() {
        if (materialInfo.element != null) return materialInfo.element.getNeutrons();
        if (materialInfo.componentList.isEmpty()) return Elements.Tc.getNeutrons();
        long totalNeutrons = 0, totalAmount = 0;
        for (MaterialStack stack : materialInfo.componentList) {
            totalAmount += stack.amount;
            totalNeutrons += stack.amount * stack.material.getNeutrons();
        }
        return totalNeutrons / totalAmount;
    }

    @ZenGetter("mass")
    public long getMass() {
        if (materialInfo.element != null) return materialInfo.element.getMass();
        if (materialInfo.componentList.size() == 0) return Elements.Tc.getMass();
        long totalMass = 0, totalAmount = 0;
        for (MaterialStack stack : materialInfo.componentList) {
            totalAmount += stack.amount;
            totalMass += stack.amount * stack.material.getMass();
        }
        return totalMass / totalAmount;
    }

    @ZenGetter("blastTemperature")
    public int getBlastTemperature() {
        BlastProperty prop = properties.getProperty(PropertyKey.BLAST);
        return prop == null ? 0 : prop.getBlastTemperature();
    }

    public FluidStack getPlasma(int amount) {
        return getFluid(FluidStorageKeys.PLASMA, amount);
    }

    // TODO clean up the name-related methods
    @ZenGetter("name")
    @NotNull
    public String getName() {
        return getResourceLocation().getPath();
    }

    @NotNull
    public String getModid() {
        return getResourceLocation().getNamespace();
    }

    @NotNull
    public ResourceLocation getResourceLocation() {
        return materialInfo.resourceLocation;
    }

    @ZenGetter("camelCaseName")
    public String toCamelCaseString() {
        return GTUtility.lowerUnderscoreToUpperCamel(toString());
    }

    @ZenGetter("unlocalizedName")
    public String getUnlocalizedName() {
        ResourceLocation location = getResourceLocation();
        return location.getNamespace() + ".material." + location.getPath();
    }

    @NotNull
    public String getRegistryName() {
        ResourceLocation location = getResourceLocation();
        return location.getNamespace() + ':' + location.getPath();
    }

    @ZenGetter("localizedName")
    public String getLocalizedName() {
        return LocalizationUtils.format(getUnlocalizedName());
    }

    @Override
    @ZenMethod
    public int compareTo(Material material) {
        return getName().compareTo(material.getName());
    }

    @Override
    public String toString() {
        return getName();
    }

    public int getId() {
        return materialInfo.metaItemSubId;
    }

    // must be named multiply for GroovyScript to allow `mat * quantity -> MaterialStack`
    @ZenOperator(OperatorType.MUL)
    public MaterialStack multiply(long amount) {
        return new MaterialStack(this, amount);
    }

    @NotNull
    public MaterialProperties getProperties() {
        return properties;
    }

    public <T extends IMaterialProperty> boolean hasProperty(PropertyKey<T> key) {
        return getProperty(key) != null;
    }

    public <T extends IMaterialProperty> T getProperty(PropertyKey<T> key) {
        return properties.getProperty(key);
    }

    public <T extends IMaterialProperty> void setProperty(PropertyKey<T> key, IMaterialProperty property) {
        if (!GregTechAPI.materialManager.canModifyMaterials()) {
            throw new IllegalStateException("Cannot add properties to a Material when registry is frozen!");
        }
        properties.setProperty(key, property);
        properties.verify();
    }

    public boolean isSolid() {
        return hasProperty(PropertyKey.INGOT) || hasProperty(PropertyKey.GEM);
    }

    public boolean hasFluid() {
        return hasProperty(PropertyKey.FLUID);
    }

    public void verifyMaterial() {
        properties.verify();
        flags.verify(this);
        this.chemicalFormula = calculateChemicalFormula();
        calculateDecompositionType();
    }

    @NotNull
    public MaterialRegistry getRegistry() {
        return GregTechAPI.materialManager.getRegistry(getModid());
    }

    /**
     * @since GTCEu 2.0.0
     */
    public static class Builder {

        private final MaterialInfo materialInfo;
        private final MaterialProperties properties;
        private final MaterialFlags flags;

        private final List<Consumer<Material>> postProcessors = new ArrayList<>(1);

        /*
         * The temporary list of components for this Material.
         */
        private List<MaterialStack> composition = new ArrayList<>();

        /*
         * Temporary value to use to determine how to calculate default RGB
         */
        private boolean averageRGB = false;

        /**
         * Constructs a {@link Material}. This Builder replaces the old constructors, and no longer uses a class
         * hierarchy, instead using a {@link MaterialProperties} system.
         *
         * @param id               The MetaItemSubID for this Material. Must be unique.
         * @param resourceLocation The ModId and Name of this Material. Will be formatted as "<modid>.material.<name>"
         *                         for the Translation Key.
         * @since GTCEu 2.0.0
         */
        public Builder(int id, @NotNull ResourceLocation resourceLocation) {
            String name = resourceLocation.getPath();
            if (name.charAt(name.length() - 1) == '_') {
                throw new IllegalArgumentException("Material name cannot end with a '_'!");
            }
            materialInfo = new MaterialInfo(id, resourceLocation);
            properties = new MaterialProperties();
            flags = new MaterialFlags();
        }

        /*
         * Material Types
         */

        /**
         * Add a {@link FluidProperty} to this Material.<br>
         * Will be created as a {@link FluidStorageKeys#LIQUID}, with
         * standard {@link FluidBuilder} defaults.
         * <p>
         * Can be called multiple times to add multiple fluids.
         */
        public Builder fluid() {
            return fluid(FluidStorageKeys.LIQUID, new FluidBuilder());
        }

        /**
         * Add a {@link FluidProperty} to this Material.<br>
         * Will be created with the specified state a with standard
         * {@link FluidBuilder} defaults.
         * <p>
         * Can be called multiple times to add multiple fluids.
         */
        public Builder fluid(@NotNull FluidStorageKey key, @NotNull FluidState state) {
            return fluid(key, new FluidBuilder().state(state));
        }

        /**
         * Add a {@link FluidProperty} to this Material.<br>
         * <p>
         * Can be called multiple times to add multiple fluids.
         */
        public Builder fluid(@NotNull FluidStorageKey key, @NotNull FluidBuilder builder) {
            properties.ensureSet(PropertyKey.FLUID);
            FluidProperty property = properties.getProperty(PropertyKey.FLUID);
            property.enqueueRegistration(key, builder);

            return this;
        }

        /**
         * Assign an existing fluid to this material. Useful for things like Lava and Water where MC already has a fluid
         * to use, or for cross-mod compatibility.
         *
         * @param fluid The existing liquid
         */
        public Builder fluid(@NotNull Fluid fluid, @NotNull FluidStorageKey key, @NotNull FluidState state) {
            properties.ensureSet(PropertyKey.FLUID);
            FluidProperty property = properties.getProperty(PropertyKey.FLUID);
            property.store(key, fluid);

            postProcessors.add(
                    m -> FluidTooltipUtil.registerTooltip(fluid, FluidTooltipUtil.createFluidTooltip(m, fluid, state)));
            return this;
        }

        /**
         * Add a liquid for this material.
         *
         * @see #fluid(FluidStorageKey, FluidState)
         */
        public Builder liquid() {
            return fluid(FluidStorageKeys.LIQUID, FluidState.LIQUID);
        }

        /**
         * Add a liquid for this material.
         *
         * @see #fluid(FluidStorageKey, FluidState)
         */
        public Builder liquid(@NotNull FluidBuilder builder) {
            return fluid(FluidStorageKeys.LIQUID, builder.state(FluidState.LIQUID));
        }

        /**
         * Add a plasma for this material.
         *
         * @see #fluid(FluidStorageKey, FluidState)
         */
        public Builder plasma() {
            return fluid(FluidStorageKeys.PLASMA, FluidState.PLASMA);
        }

        /**
         * Add a plasma for this material.
         *
         * @see #fluid(FluidStorageKey, FluidState)
         */
        public Builder plasma(@NotNull FluidBuilder builder) {
            return fluid(FluidStorageKeys.PLASMA, builder.state(FluidState.PLASMA));
        }

        /**
         * Add a gas for this material.
         *
         * @see #fluid(FluidStorageKey, FluidState)
         */
        public Builder gas() {
            return fluid(FluidStorageKeys.GAS, FluidState.GAS);
        }

        /**
         * Add a gas for this material.
         *
         * @see #fluid(FluidStorageKey, FluidState)
         */
        public Builder gas(@NotNull FluidBuilder builder) {
            return fluid(FluidStorageKeys.GAS, builder.state(FluidState.GAS));
        }

        /**
         * Add a {@link DustProperty} to this Material.<br>
         * Will be created with a Harvest Level of 2 and no Burn Time
         * (Furnace Fuel).
         *
         * @throws IllegalArgumentException If a {@link DustProperty} has already been added to this Material.
         */
        public Builder dust() {
            properties.ensureSet(PropertyKey.DUST);
            return this;
        }

        /**
         * Add a {@link DustProperty} to this Material.<br>
         * Will be created with no Burn Time (Furnace Fuel).
         *
         * @param harvestLevel The Harvest Level of this block for Mining.<br>
         *                     If this Material also has a
         *                     {@link ToolProperty}, this value will also be used to determine the tool's Mining Level.
         * @throws IllegalArgumentException If a {@link DustProperty} has already been added to this Material.
         */
        public Builder dust(int harvestLevel) {
            return dust(harvestLevel, 0);
        }

        /**
         * Add a {@link DustProperty} to this Material.
         *
         * @param harvestLevel The Harvest Level of this block for Mining.<br>
         *                     If this Material also has a
         *                     {@link ToolProperty}, this value will also be used to determine the tool's Mining Level.
         * @param burnTime     The Burn Time (in ticks) of this Material as a Furnace Fuel.
         * @throws IllegalArgumentException If a {@link DustProperty} has already been added to this Material.
         */
        public Builder dust(int harvestLevel, int burnTime) {
            properties.setProperty(PropertyKey.DUST, new DustProperty(harvestLevel, burnTime));
            return this;
        }

        /**
         * Add a {@link WoodProperty} to this Material.<br>
         * Will be created with a Harvest Level of 0 and a Burn Time of
         * 300 (Furnace Fuel).
         */
        public Builder wood() {
            return wood(0, 300);
        }

        /**
         * Add a {@link WoodProperty} to this Material.<br>
         * Will be created with a Burn Time of 300 (Furnace Fuel).
         *
         * @param harvestLevel The Harvest Level of this block for Mining.<br>
         *                     If this Material also has a
         *                     {@link ToolProperty}, this value will also be used to determine the tool's Mining Level.
         */
        public Builder wood(int harvestLevel) {
            return wood(harvestLevel, 300);
        }

        /**
         * Add a {@link WoodProperty} to this Material.
         *
         * @param harvestLevel The Harvest Level of this block for Mining.<br>
         *                     If this Material also has a
         *                     {@link ToolProperty}, this value will also be used to determine the tool's Mining Level.
         * @param burnTime     The Burn Time (in ticks) of this Material as a Furnace Fuel.
         */
        public Builder wood(int harvestLevel, int burnTime) {
            properties.setProperty(PropertyKey.DUST, new DustProperty(harvestLevel, burnTime));
            properties.setProperty(PropertyKey.WOOD, new WoodProperty());
            return this;
        }

        /**
         * Add an {@link IngotProperty} to this Material.<br>
         * Will be created with a Harvest Level of 2 and no Burn Time
         * (Furnace Fuel).<br>
         * Will automatically add a {@link DustProperty} to this Material if it does not already
         * have one.
         *
         * @throws IllegalArgumentException If an {@link IngotProperty} has already been added to this Material.
         */
        public Builder ingot() {
            properties.ensureSet(PropertyKey.INGOT);
            return this;
        }

        /**
         * Add an {@link IngotProperty} to this Material.<br>
         * Will be created with no Burn Time (Furnace Fuel).<br>
         * Will
         * automatically add a {@link DustProperty} to this Material if it does not already have one.
         *
         * @param harvestLevel The Harvest Level of this block for Mining. 2 will make it require a iron tool.<br>
         *                     If
         *                     this Material also has a {@link ToolProperty}, this value will also be used to determine
         *                     the tool's Mining level (-1). So 2 will make the tool harvest diamonds.<br>
         *                     If this
         *                     Material already had a Harvest Level defined, it will be overridden.
         * @throws IllegalArgumentException If an {@link IngotProperty} has already been added to this Material.
         */
        public Builder ingot(int harvestLevel) {
            return ingot(harvestLevel, 0);
        }

        /**
         * Add an {@link IngotProperty} to this Material.<br>
         * Will automatically add a {@link DustProperty} to this
         * Material if it does not already have one.
         *
         * @param harvestLevel The Harvest Level of this block for Mining. 2 will make it require a iron tool.<br>
         *                     If
         *                     this Material also has a {@link ToolProperty}, this value will also be used to determine
         *                     the tool's Mining level (-1). So 2 will make the tool harvest diamonds.<br>
         *                     If this
         *                     Material already had a Harvest Level defined, it will be overridden.
         * @param burnTime     The Burn Time (in ticks) of this Material as a Furnace Fuel.<br>
         *                     If this Material already
         *                     had a Burn Time defined, it will be overridden.
         * @throws IllegalArgumentException If an {@link IngotProperty} has already been added to this Material.
         */
        public Builder ingot(int harvestLevel, int burnTime) {
            DustProperty prop = properties.getProperty(PropertyKey.DUST);
            if (prop == null) dust(harvestLevel, burnTime);
            else {
                if (prop.getHarvestLevel() == 2) prop.setHarvestLevel(harvestLevel);
                if (prop.getBurnTime() == 0) prop.setBurnTime(burnTime);
            }
            properties.ensureSet(PropertyKey.INGOT);
            return this;
        }

        /**
         * Add a {@link GemProperty} to this Material.<br>
         * Will be created with a Harvest Level of 2 and no Burn Time
         * (Furnace Fuel).<br>
         * Will automatically add a {@link DustProperty} to this Material if it does not already
         * have one.
         *
         * @throws IllegalArgumentException If a {@link GemProperty} has already been added to this Material.
         */
        public Builder gem() {
            properties.ensureSet(PropertyKey.GEM);
            return this;
        }

        /**
         * Add a {@link GemProperty} to this Material.<br>
         * Will be created with no Burn Time (Furnace Fuel).<br>
         * Will
         * automatically add a {@link DustProperty} to this Material if it does not already have one.
         *
         * @param harvestLevel The Harvest Level of this block for Mining.<br>
         *                     If this Material also has a
         *                     {@link ToolProperty}, this value will also be used to determine the tool's Mining
         *                     level.<br>
         *                     If this Material already had a Harvest Level defined, it will be overridden.
         * @throws IllegalArgumentException If a {@link GemProperty} has already been added to this Material.
         */
        public Builder gem(int harvestLevel) {
            return gem(harvestLevel, 0);
        }

        /**
         * Add a {@link GemProperty} to this Material.<br>
         * Will automatically add a {@link DustProperty} to this
         * Material if it does not already have one.
         *
         * @param harvestLevel The Harvest Level of this block for Mining.<br>
         *                     If this Material also has a
         *                     {@link ToolProperty}, this value will also be used to determine the tool's Mining
         *                     level.<br>
         *                     If this Material already had a Harvest Level defined, it will be overridden.
         * @param burnTime     The Burn Time (in ticks) of this Material as a Furnace Fuel.<br>
         *                     If this Material already
         *                     had a Burn Time defined, it will be overridden.
         */
        public Builder gem(int harvestLevel, int burnTime) {
            DustProperty prop = properties.getProperty(PropertyKey.DUST);
            if (prop == null) dust(harvestLevel, burnTime);
            else {
                if (prop.getHarvestLevel() == 2) prop.setHarvestLevel(harvestLevel);
                if (prop.getBurnTime() == 0) prop.setBurnTime(burnTime);
            }
            properties.ensureSet(PropertyKey.GEM);
            return this;
        }

        /**
         * Add a {@link PolymerProperty} to this Material.<br>
         * Will be created with a Harvest Level of 2 and no Burn
         * Time (Furnace Fuel).<br>
         * Will automatically add a {@link DustProperty} to this Material if it does not
         * already have one.
         *
         * @throws IllegalArgumentException If an {@link PolymerProperty} has already been added to this Material.
         */
        public Builder polymer() {
            properties.ensureSet(PropertyKey.POLYMER);
            return this;
        }

        /**
         * Add a {@link PolymerProperty} to this Material.<br>
         * Will automatically add a {@link DustProperty} to this
         * Material if it does not already have one. Will have a burn time of 0
         *
         * @param harvestLevel The Harvest Level of this block for Mining.<br>
         *                     If this Material also has a
         *                     {@link ToolProperty}, this value will also be used to determine the tool's Mining
         *                     level.<br>
         *                     If this Material already had a Harvest Level defined, it will be overridden.
         * @throws IllegalArgumentException If an {@link PolymerProperty} has already been added to this Material.
         */
        public Builder polymer(int harvestLevel) {
            DustProperty prop = properties.getProperty(PropertyKey.DUST);
            if (prop == null) dust(harvestLevel, 0);
            else if (prop.getHarvestLevel() == 2) prop.setHarvestLevel(harvestLevel);
            properties.ensureSet(PropertyKey.POLYMER);
            return this;
        }

        public Builder burnTime(int burnTime) {
            DustProperty prop = properties.getProperty(PropertyKey.DUST);
            if (prop == null) {
                dust();
                prop = properties.getProperty(PropertyKey.DUST);
            }
            prop.setBurnTime(burnTime);
            return this;
        }

        /**
         * Set the Color of this Material.<br>
         * Defaults to 0xFFFFFF unless {@link Builder#colorAverage()} was called,
         * where it will be a weighted average of the components of the Material.
         *
         * @param color The RGB-formatted Color.
         */
        public Builder color(int color) {
            this.materialInfo.color = color;
            return this;
        }

        public Builder colorAverage() {
            this.averageRGB = true;
            return this;
        }

        /**
         * Set the {@link MaterialIconSet} of this Material.<br>
         * Defaults vary depending on if the Material has a:<br>
         * <ul>
         * <li>{@link GemProperty}, it will default to {@link MaterialIconSet#GEM_VERTICAL}
         * <li>{@link IngotProperty} or {@link DustProperty}, it will default to {@link MaterialIconSet#DULL}
         * <li>{@link FluidProperty}, it will default to {@link MaterialIconSet#FLUID}
         * </ul>
         * Default will be determined by first-found Property in this order, unless specified.
         *
         * @param iconSet The {@link MaterialIconSet} of this Material.
         */
        public Builder iconSet(MaterialIconSet iconSet) {
            materialInfo.iconSet = iconSet;
            return this;
        }

        public Builder components(Object... components) {
            Preconditions.checkArgument(components.length % 2 == 0, "Material Components list malformed!");

            for (int i = 0; i < components.length; i += 2) {
                if (components[i] == null) {
                    throw new IllegalArgumentException(
                            "Material in Components List is null for Material " + this.materialInfo.resourceLocation);
                }
                composition.add(new MaterialStack((Material) components[i], (Integer) components[i + 1]));
            }
            return this;
        }

        public Builder components(MaterialStack... components) {
            composition = Arrays.asList(components);
            return this;
        }

        public Builder components(ImmutableList<MaterialStack> components) {
            composition = components;
            return this;
        }

        /**
         * Add {@link MaterialFlags} to this Material.<br>
         * Dependent Flags (for example,
         * {@link MaterialFlags#GENERATE_LONG_ROD} requiring {@link MaterialFlags#GENERATE_ROD}) will be automatically
         * applied.
         */
        public Builder flags(MaterialFlag... flags) {
            this.flags.addFlags(flags);
            return this;
        }

        /**
         * Add {@link MaterialFlags} to this Material.<br>
         * Dependent Flags (for example,
         * {@link MaterialFlags#GENERATE_LONG_ROD} requiring {@link MaterialFlags#GENERATE_ROD}) will be automatically
         * applied.
         *
         * @param f1 A {@link Collection} of {@link MaterialFlag}. Provided this way for easy Flag presets to be
         *           applied.
         * @param f2 An Array of {@link MaterialFlag}. If no {@link Collection} is required, use
         *           {@link Builder#flags(MaterialFlag...)}.
         */
        public Builder flags(Collection<MaterialFlag> f1, MaterialFlag... f2) {
            this.flags.addFlags(f1.toArray(new MaterialFlag[0]));
            this.flags.addFlags(f2);
            return this;
        }

        public Builder element(Element element) {
            this.materialInfo.element = element;
            return this;
        }

        /**
         * Replaced the old toolStats methods which took many parameters. Use {@link ToolProperty.Builder} instead to
         * create a Tool Property.
         */
        public Builder toolStats(ToolProperty toolProperty) {
            properties.setProperty(PropertyKey.TOOL, toolProperty);
            return this;
        }

        public Builder rotorStats(float speed, float damage, int durability) {
            properties.setProperty(PropertyKey.ROTOR, new RotorProperty(speed, damage, durability));
            return this;
        }

        /** @deprecated use {@link Material.Builder#blast(int)}. */
        @ApiStatus.ScheduledForRemoval(inVersion = "2.9")
        @Deprecated
        public Builder blastTemp(int temp) {
            return blast(temp);
        }

        /** @deprecated use {@link Material.Builder#blast(int, BlastProperty.GasTier)}. */
        @ApiStatus.ScheduledForRemoval(inVersion = "2.9")
        @Deprecated
        public Builder blastTemp(int temp, BlastProperty.GasTier gasTier) {
            return blast(temp, gasTier);
        }

        /** @deprecated use {@link Material.Builder#blast(UnaryOperator)} for more detailed stats. */
        @ApiStatus.ScheduledForRemoval(inVersion = "2.9")
        @Deprecated
        public Builder blastTemp(int temp, BlastProperty.GasTier gasTier, int eutOverride) {
            return blast(b -> b.temp(temp, gasTier).blastStats(eutOverride));
        }

        /** @deprecated use {@link Material.Builder#blast(UnaryOperator)} for more detailed stats. */
        @ApiStatus.ScheduledForRemoval(inVersion = "2.9")
        @Deprecated
        public Builder blastTemp(int temp, BlastProperty.GasTier gasTier, int eutOverride, int durationOverride) {
            return blast(b -> b.temp(temp, gasTier).blastStats(eutOverride, durationOverride));
        }

        public Builder blast(int temp) {
            properties.setProperty(PropertyKey.BLAST, new BlastProperty(temp));
            return this;
        }

        public Builder blast(int temp, BlastProperty.GasTier gasTier) {
            properties.setProperty(PropertyKey.BLAST, new BlastProperty(temp, gasTier));
            return this;
        }

        public Builder blast(UnaryOperator<BlastProperty.Builder> b) {
            properties.setProperty(PropertyKey.BLAST, b.apply(new BlastProperty.Builder()).build());
            return this;
        }

        public Builder ore() {
            properties.ensureSet(PropertyKey.ORE);
            return this;
        }

        public Builder ore(boolean emissive) {
            properties.setProperty(PropertyKey.ORE, new OreProperty(1, 1, emissive));
            return this;
        }

        public Builder ore(int oreMultiplier, int byproductMultiplier) {
            properties.setProperty(PropertyKey.ORE, new OreProperty(oreMultiplier, byproductMultiplier));
            return this;
        }

        public Builder ore(int oreMultiplier, int byproductMultiplier, boolean emissive) {
            properties.setProperty(PropertyKey.ORE, new OreProperty(oreMultiplier, byproductMultiplier, emissive));
            return this;
        }

        public Builder washedIn(Material m) {
            properties.ensureSet(PropertyKey.ORE);
            properties.getProperty(PropertyKey.ORE).setWashedIn(m);
            return this;
        }

        public Builder washedIn(Material m, int washedAmount) {
            properties.ensureSet(PropertyKey.ORE);
            properties.getProperty(PropertyKey.ORE).setWashedIn(m, washedAmount);
            return this;
        }

        public Builder separatedInto(Material... m) {
            properties.ensureSet(PropertyKey.ORE);
            properties.getProperty(PropertyKey.ORE).setSeparatedInto(m);
            return this;
        }

        public Builder oreSmeltInto(Material m) {
            properties.ensureSet(PropertyKey.ORE);
            properties.getProperty(PropertyKey.ORE).setDirectSmeltResult(m);
            return this;
        }

        public Builder polarizesInto(Material m) {
            properties.ensureSet(PropertyKey.INGOT);
            properties.getProperty(PropertyKey.INGOT).setMagneticMaterial(m);
            return this;
        }

        public Builder arcSmeltInto(Material m) {
            properties.ensureSet(PropertyKey.INGOT);
            properties.getProperty(PropertyKey.INGOT).setArcSmeltingInto(m);
            return this;
        }

        public Builder macerateInto(Material m) {
            properties.ensureSet(PropertyKey.INGOT);
            properties.getProperty(PropertyKey.INGOT).setMacerateInto(m);
            return this;
        }

        public Builder ingotSmeltInto(Material m) {
            properties.ensureSet(PropertyKey.INGOT);
            properties.getProperty(PropertyKey.INGOT).setSmeltingInto(m);
            return this;
        }

        public Builder addOreByproducts(Material... byproducts) {
            properties.ensureSet(PropertyKey.ORE);
            properties.getProperty(PropertyKey.ORE).addOreByProducts(byproducts);
            return this;
        }

        public Builder cableProperties(long voltage, int amperage, int loss) {
            cableProperties((int) voltage, amperage, loss, false);
            return this;
        }

        public Builder cableProperties(long voltage, int amperage, int loss, boolean isSuperCon) {
            properties.setProperty(PropertyKey.WIRE, new WireProperties((int) voltage, amperage, loss, isSuperCon));
            return this;
        }

        public Builder cableProperties(long voltage, int amperage, int loss, boolean isSuperCon,
                                       int criticalTemperature) {
            properties.setProperty(PropertyKey.WIRE,
                    new WireProperties((int) voltage, amperage, loss, isSuperCon, criticalTemperature));
            return this;
        }

        public Builder fluidPipeProperties(int maxTemp, int throughput, boolean gasProof) {
            return fluidPipeProperties(maxTemp, throughput, gasProof, false, false, false);
        }

        public Builder fluidPipeProperties(int maxTemp, int throughput, boolean gasProof, boolean acidProof,
                                           boolean cryoProof, boolean plasmaProof) {
            properties.setProperty(PropertyKey.FLUID_PIPE,
                    new FluidPipeProperties(maxTemp, throughput, gasProof, acidProof, cryoProof, plasmaProof));
            return this;
        }

        public Builder itemPipeProperties(int priority, float stacksPerSec) {
            properties.setProperty(PropertyKey.ITEM_PIPE, new ItemPipeProperties(priority, stacksPerSec));
            return this;
        }

        public Builder fissionFuel(int maxTemperature, int duration, double slowNeutronCaptureCrossSection,
                                   double fastNeutronCaptureCrossSection,
                                   double slowNeutronFissionCrossSection,
                                   double fastNeutronFissionCrossSection, double neutronGenerationTime) {
            properties.ensureSet(PropertyKey.DUST);
            properties.setProperty(PropertyKey.FISSION_FUEL,
                    new FissionFuelProperty(maxTemperature, duration, slowNeutronCaptureCrossSection,
                            fastNeutronCaptureCrossSection, slowNeutronFissionCrossSection,
                            fastNeutronFissionCrossSection, neutronGenerationTime));
            return this;
        }

        // TODO Clean this up post 2.5 release
        @Deprecated
        public Builder addDefaultEnchant(Enchantment enchant, int level) {
            if (!properties.hasProperty(PropertyKey.TOOL)) // cannot assign default here
                throw new IllegalArgumentException("Material cannot have an Enchant without Tools!");
            properties.getProperty(PropertyKey.TOOL).addEnchantmentForTools(enchant, level);
            return this;
        }

        public Material build() {
            materialInfo.componentList = ImmutableList.copyOf(composition);
            materialInfo.verifyInfo(properties, averageRGB);
            Material m = new Material(materialInfo, properties, flags);
            if (!postProcessors.isEmpty()) postProcessors.forEach(p -> p.accept(m));
            return m;
        }
    }

    /**
     * Holds the basic info for a Material, like the name, color, id, etc..
     */
    private static class MaterialInfo {

        /**
         * The modid and unlocalized name of this Material.
         * <p>
         * Required.
         */
        private final ResourceLocation resourceLocation;

        /**
         * The MetaItem ID of this Material.
         * <p>
         * Required.
         */
        private final int metaItemSubId;

        /**
         * The color of this Material.
         * <p>
         * Default: 0xFFFFFF if no Components, otherwise it will be the average of Components.
         */
        private int color = -1;

        /**
         * The IconSet of this Material.
         * <p>
         * Default: - GEM_VERTICAL if it has GemProperty. - DULL if has DustProperty or IngotProperty. - FLUID if only
         * has FluidProperty.
         */
        private MaterialIconSet iconSet;

        /**
         * The components of this Material.
         * <p>
         * Default: none.
         */
        private ImmutableList<MaterialStack> componentList;

        /**
         * The Element of this Material, if it is a direct Element.
         * <p>
         * Default: none.
         */
        private Element element;

        private MaterialInfo(int metaItemSubId, @NotNull ResourceLocation resourceLocation) {
            this.metaItemSubId = metaItemSubId;
            String name = resourceLocation.getPath();
            if (!GTUtility.toLowerCaseUnderscore(GTUtility.lowerUnderscoreToUpperCamel(name)).equals(name)) {
                throw new IllegalArgumentException(
                        "Cannot add materials with names like 'materialnumber'! Use 'material_number' instead.");
            }
            this.resourceLocation = resourceLocation;
        }

        private void verifyInfo(MaterialProperties p, boolean averageRGB) {
            // Verify IconSet
            if (iconSet == null) {
                if (p.hasProperty(PropertyKey.GEM)) {
                    iconSet = MaterialIconSet.GEM_VERTICAL;
                } else if (p.hasProperty(PropertyKey.DUST) || p.hasProperty(PropertyKey.INGOT) ||
                        p.hasProperty(PropertyKey.POLYMER)) {
                            iconSet = MaterialIconSet.DULL;
                        } else
                    if (p.hasProperty(PropertyKey.FLUID)) {
                        iconSet = MaterialIconSet.FLUID;
                    } else {
                        iconSet = MaterialIconSet.DULL;
                    }
            }

            // Verify MaterialRGB
            if (color == -1) {
                if (!averageRGB || componentList.isEmpty()) color = 0xFFFFFF;
                else {
                    long colorTemp = 0;
                    int divisor = 0;
                    for (MaterialStack stack : componentList) {
                        colorTemp += stack.material.getMaterialRGB() * stack.amount;
                        divisor += stack.amount;
                    }
                    color = (int) (colorTemp / divisor);
                }
            }
        }
    }
}
