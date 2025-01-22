package gregtech.api.items.materialitem;

import gregtech.api.GTValues;
import gregtech.api.damagesources.DamageSources;
import gregtech.api.items.armoritem.ArmorHelper;
import gregtech.api.items.armoritem.IGTArmor;
import gregtech.api.items.metaitem.StandardMetaItem;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.info.MaterialIconSet;
import gregtech.api.unification.material.properties.DustProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.material.properties.ToolProperty;
import gregtech.api.unification.material.registry.MaterialRegistry;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.common.creativetab.GTCreativeTabs;

import net.minecraft.block.BlockCauldron;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MetaPrefixItem extends StandardMetaItem {

    private final MaterialRegistry registry;
    private final OrePrefix prefix;

    public static final Map<OrePrefix, OrePrefix> purifyMap = new HashMap<>();

    static {
        purifyMap.put(OrePrefix.crushed, OrePrefix.crushedPurified);
        purifyMap.put(OrePrefix.dustImpure, OrePrefix.dust);
        purifyMap.put(OrePrefix.dustPure, OrePrefix.dust);
    }

    public MetaPrefixItem(@NotNull MaterialRegistry registry, @NotNull OrePrefix orePrefix) {
        super();
        this.registry = registry;
        this.prefix = orePrefix;
        this.setCreativeTab(GTCreativeTabs.TAB_GREGTECH_MATERIALS);
    }

    @Override
    public void registerSubItems() {
        for (Material material : registry) {
            short i = (short) registry.getIDForObject(material);
            if (prefix != null && canGenerate(prefix, material)) {
                addItem(i, new UnificationEntry(prefix, material).toString());
            }
        }
    }

    public void registerOreDict() {
        for (short metaItem : metaItems.keySet()) {
            Material material = getMaterial(metaItem);
            ItemStack item = new ItemStack(this, 1, metaItem);
            OreDictUnifier.registerOre(item, prefix, material);
            registerSpecialOreDict(item, material, prefix);
        }
    }

    private static void registerSpecialOreDict(ItemStack item, Material material, OrePrefix prefix) {
        if (prefix.getAlternativeOreName() != null) {
            OreDictUnifier.registerOre(item, prefix.getAlternativeOreName(), material);
        }

        if (material == Materials.Saltpeter) {
            OreDictUnifier.registerOre(item, prefix.name() + material.toCamelCaseString());
        }
    }

    protected static boolean canGenerate(OrePrefix orePrefix, Material material) {
        return orePrefix.doGenerateItem(material);
    }

    @NotNull
    @Override
    public String getItemStackDisplayName(@NotNull ItemStack itemStack) {
        Material material = getMaterial(itemStack);
        if (material == null || prefix == null) return "";
        return prefix.getLocalNameForItem(material);
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected int getColorForItemStack(@NotNull ItemStack stack, int tintIndex) {
        if (tintIndex == 0) {
            Material material = getMaterial(stack);
            if (material == null)
                return 0xFFFFFF;
            return material.getMaterialRGB();
        }
        return super.getColorForItemStack(stack, tintIndex);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModels() {
        Map<Short, ModelResourceLocation> alreadyRegistered = new Short2ObjectOpenHashMap<>();
        for (short metaItem : metaItems.keySet()) {
            MaterialIconSet materialIconSet = getMaterial(metaItem).getMaterialIconSet();

            short registrationKey = (short) (prefix.id + materialIconSet.id);
            if (!alreadyRegistered.containsKey(registrationKey)) {
                ResourceLocation resourceLocation = Objects.requireNonNull(prefix.materialIconType)
                        .getItemModelPath(materialIconSet);
                ModelBakery.registerItemVariants(this, resourceLocation);
                alreadyRegistered.put(registrationKey, new ModelResourceLocation(resourceLocation, "inventory"));
            }
            ModelResourceLocation resourceLocation = alreadyRegistered.get(registrationKey);
            metaItemsModels.put(metaItem, resourceLocation);
        }

        // Make some default models for meta prefix items without any materials associated
        if (metaItems.keySet().isEmpty()) {
            MaterialIconSet defaultIcon = MaterialIconSet.DULL;
            ResourceLocation defaultLocation = Objects.requireNonNull(OrePrefix.ingot.materialIconType)
                    .getItemModelPath(defaultIcon);
            ModelBakery.registerItemVariants(this, defaultLocation);
        }
    }

    @Override
    public int getItemStackLimit(@NotNull ItemStack stack) {
        if (prefix == null) return 64;
        return prefix.maxStackSize;
    }

    @Override
    public void onUpdate(@NotNull ItemStack itemStack, @NotNull World worldIn, @NotNull Entity entityIn, int itemSlot,
                         boolean isSelected) {
        super.onUpdate(itemStack, worldIn, entityIn, itemSlot, isSelected);
        if (metaItems.containsKey((short) itemStack.getItemDamage()) && entityIn instanceof EntityLivingBase entity) {
            if (entityIn.ticksExisted % 20 == 0) {
                if (prefix.heatDamageFunction == null) return;

                Material material = getMaterial(itemStack);
                if (material == null || !material.hasProperty(PropertyKey.BLAST)) return;

                float heatDamage = prefix.heatDamageFunction.apply(material.getBlastTemperature());
                for (EntityEquipmentSlot slot : ArmorHelper.getArmorSlots()) {
                    ItemStack armor = entity.getItemStackFromSlot(slot);
                    if (!armor.isEmpty() && armor.getItem() instanceof IGTArmor gtArmor) {
                        NBTTagCompound tag = ArmorHelper.getBehaviorsTag(armor);
                        if (tag.hasKey(ArmorHelper.HEAT_REDUCTION_KEY) && gtArmor.areBehaviorsActive(armor)) {
                            float reductionFactor = tag.getFloat(ArmorHelper.HEAT_REDUCTION_KEY);
                            if (reductionFactor == 0) return;
                            heatDamage *= reductionFactor;
                        }
                    }
                }

                if (heatDamage > 0.0) {
                    entity.attackEntityFrom(DamageSources.getHeatDamage().setDamageBypassesArmor(), heatDamage);
                } else if (heatDamage < 0.0) {
                    entity.attackEntityFrom(DamageSources.getFrostDamage().setDamageBypassesArmor(), -heatDamage);
                }
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(@NotNull ItemStack itemStack, @Nullable World worldIn, @NotNull List<String> lines,
                               @NotNull ITooltipFlag tooltipFlag) {
        super.addInformation(itemStack, worldIn, lines, tooltipFlag);
        Material material = getMaterial(itemStack);
        if (prefix == null || material == null) return;
        addMaterialTooltip(lines, itemStack);
    }

    /**
     * For general use. Can return null if the stack metadata is an invalid material ID.
     * Requires the ItemStack's item to be a MetaPrefixItem.
     *
     * @return the material
     */
    @Nullable
    public Material getMaterial(@NotNull ItemStack stack) {
        return registry.getObjectById(stack.getMetadata());
    }

    /**
     * For registration use only. Assumes the metadata is a valid material ID.
     *
     * @return the material
     */
    @NotNull
    protected Material getMaterial(int metadata) {
        return Objects.requireNonNull(registry.getObjectById(metadata));
    }

    /**
     * Attempt to get a material from an ItemStack, whose item may not be a MetaPrefixItem.
     *
     * @return the material
     */
    @Nullable
    public static Material tryGetMaterial(@NotNull ItemStack itemStack) {
        if (itemStack.getItem() instanceof MetaPrefixItem metaPrefixItem) {
            return metaPrefixItem.getMaterial(itemStack);
        }
        return null;
    }

    public OrePrefix getOrePrefix() {
        return this.prefix;
    }

    @Override
    public int getItemBurnTime(@NotNull ItemStack itemStack) {
        Material material = getMaterial(itemStack);
        DustProperty property = material == null ? null : material.getProperty(PropertyKey.DUST);
        if (property != null) return (int) (property.getBurnTime() * prefix.getMaterialAmount(material) / GTValues.M);
        return super.getItemBurnTime(itemStack);
    }

    @Override
    public boolean isBeaconPayment(@NotNull ItemStack stack) {
        Material material = getMaterial(stack);
        if (material != null && this.prefix != OrePrefix.ingot && this.prefix != OrePrefix.gem) {
            ToolProperty property = material.getProperty(PropertyKey.TOOL);
            return property != null && property.getToolHarvestLevel() >= 2;
        }
        return false;
    }

    @Override
    public boolean onEntityItemUpdate(EntityItem itemEntity) {
        if (itemEntity.getEntityWorld().isRemote)
            return false;

        Material material = getMaterial(itemEntity.getItem());
        if (!purifyMap.containsKey(this.prefix))
            return false;

        BlockPos blockPos = new BlockPos(itemEntity);
        IBlockState blockState = itemEntity.getEntityWorld().getBlockState(blockPos);

        if (!(blockState.getBlock() instanceof BlockCauldron))
            return false;

        int waterLevel = blockState.getValue(BlockCauldron.LEVEL);
        if (waterLevel == 0)
            return false;

        itemEntity.getEntityWorld().setBlockState(blockPos,
                blockState.withProperty(BlockCauldron.LEVEL, waterLevel - 1));
        ItemStack replacementStack = OreDictUnifier.get(purifyMap.get(prefix), material,
                itemEntity.getItem().getCount());
        itemEntity.setItem(replacementStack);
        return false;
    }

    protected void addMaterialTooltip(@NotNull List<String> lines, @NotNull ItemStack itemStack) {
        if (this.prefix.tooltipFunc != null) {
            lines.addAll(this.prefix.tooltipFunc.apply(getMaterial(itemStack)));
        }
    }
}
