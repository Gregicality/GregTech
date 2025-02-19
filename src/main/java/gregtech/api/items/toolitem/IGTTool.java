package gregtech.api.items.toolitem;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.impl.CombinedCapabilityProvider;
import gregtech.api.capability.impl.ElectricItem;
import gregtech.api.items.gui.ItemUIFactory;
import gregtech.api.items.metaitem.ElectricStats;
import gregtech.api.items.toolitem.aoe.AoESymmetrical;
import gregtech.api.items.toolitem.behavior.IToolBehavior;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.api.recipes.ModHandler;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.properties.DustProperty;
import gregtech.api.unification.material.properties.MaterialToolProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.material.properties.ToolProperty;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.api.util.GTUtility;
import gregtech.api.util.Mods;
import gregtech.api.util.TextFormattingUtil;
import gregtech.client.utils.ToolChargeBarRenderer;
import gregtech.client.utils.TooltipHelper;
import gregtech.common.ConfigHolder;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentDurability;
import net.minecraft.enchantment.EnchantmentMending;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.api.implementations.items.IAEWrench;
import buildcraft.api.tools.IToolWrench;
import cofh.api.item.IToolHammer;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.factory.HandGuiData;
import com.cleanroommc.modularui.factory.ItemGuiFactory;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.TextWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.enderio.core.common.interfaces.IOverlayRenderAware;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import crazypants.enderio.api.tool.ITool;
import forestry.api.arboriculture.IToolGrafter;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import mods.railcraft.api.items.IToolCrowbar;
import mrtjp.projectred.api.IScrewdriver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static gregtech.api.items.armor.IArmorLogic.ATTACK_DAMAGE_MODIFIER;
import static gregtech.api.items.armor.IArmorLogic.ATTACK_SPEED_MODIFIER;
import static gregtech.api.items.toolitem.ToolHelper.*;

/**
 * Backing of every variation of a GT Tool
 */
@Optional.InterfaceList({
        @Optional.Interface(modid = Mods.Names.APPLIED_ENERGISTICS2,
                            iface = "appeng.api.implementations.items.IAEWrench"),
        @Optional.Interface(modid = Mods.Names.BUILD_CRAFT_CORE,
                            iface = "buildcraft.api.tools.IToolWrench"),
        @Optional.Interface(modid = Mods.Names.COFH_CORE,
                            iface = "cofh.api.item.IToolHammer"),
        @Optional.Interface(modid = Mods.Names.ENDER_IO,
                            iface = "crazypants.enderio.api.tool.ITool"),
        @Optional.Interface(modid = Mods.Names.FORESTRY,
                            iface = "forestry.api.arboriculture.IToolGrafter"),
        @Optional.Interface(modid = Mods.Names.PROJECT_RED_CORE,
                            iface = "mrtjp.projectred.api.IScrewdriver"),
        @Optional.Interface(modid = Mods.Names.RAILCRAFT,
                            iface = "mods.railcraft.api.items.IToolCrowbar"),
        @Optional.Interface(modid = Mods.Names.ENDER_CORE,
                            iface = "com.enderio.core.common.interfaces.IOverlayRenderAware") })
public interface IGTTool extends ItemUIFactory, IAEWrench, IToolWrench, IToolHammer, ITool, IToolGrafter,
                         IOverlayRenderAware, IScrewdriver, IToolCrowbar {

    /**
     * @return the modid of the tool
     */
    String getDomain();

    /**
     * @return the name of the tool
     */
    String getToolId();

    boolean isElectric();

    int getElectricTier();

    IGTToolDefinition getToolStats();

    @Nullable
    SoundEvent getSound();

    boolean playSoundOnBlockDestroy();

    @Nullable
    String getOreDictName();

    @NotNull
    List<String> getSecondaryOreDicts();

    @Nullable
    Supplier<ItemStack> getMarkerItem();

    default Item get() {
        return (Item) this;
    }

    default ItemStack getRaw() {
        ItemStack stack = new ItemStack(get());
        getToolTag(stack);
        getBehaviorsTag(stack);
        return stack;
    }

    /**
     * @return A tool made from the given material. The tool property (at least overriding) for the material must be
     *         set.
     */
    default ItemStack get(Material material) {
        ItemStack stack = new ItemStack(get());

        NBTTagCompound stackCompound = GTUtility.getOrCreateNbtCompound(stack);
        stackCompound.setBoolean(DISALLOW_CONTAINER_ITEM_KEY, false);

        NBTTagCompound toolTag = getToolTag(stack);
        IGTToolDefinition toolStats = getToolStats();

        // don't show the normal vanilla damage and attack speed tooltips,
        // we handle those ourselves
        stackCompound.setInteger(HIDE_FLAGS, 2);

        // Set Material
        toolTag.setString(MATERIAL_KEY, material.getRegistryName());

        // Grab the definition here because we cannot use getMaxAoEDefinition as it is not initialized yet
        AoESymmetrical aoeDefinition = getToolStats().getAoEDefinition(stack);

        // Set other tool stats (durability)
        ToolProperty finalToolProperty = getToolProperty(material);
        // Durability formula we are working with:
        // Final Durability = (material durability * material durability multiplier) + (tool definition durability *
        // definition durability multiplier) - 1
        // Subtracts 1 internally since Minecraft treats "0" as a valid durability, but we don't want to display this.

        // Tool material modifiers.
        int durability = finalToolProperty.getToolDurability() * finalToolProperty.getDurabilityMultiplier();

        // Tool type modifiers.
        // Most Tool Definitions do not set a base durability, which will lead to ignoring the multiplier if present. So
        // apply the multiplier to the material durability if that would happen
        if (toolStats.getBaseDurability(stack) == 0) {
            durability = (int) (durability * toolStats.getDurabilityMultiplier(stack));
        } else {
            durability += (int) (toolStats.getBaseDurability(stack) * toolStats.getDurabilityMultiplier(stack));
        }

        durability -= 1; // Finally adjust for MC
        toolTag.setInteger(MAX_DURABILITY_KEY, durability);
        toolTag.setInteger(DURABILITY_KEY, 0);

        if (finalToolProperty.getUnbreakable()) {
            stackCompound.setBoolean(UNBREAKABLE_KEY, true);
        }

        // Set tool and material enchantments
        Object2IntMap<Enchantment> enchantments = new Object2IntOpenHashMap<>();
        finalToolProperty.getEnchantments().forEach((enchantment, level) -> enchantments.put(enchantment,
                level.getLevel(finalToolProperty.getToolHarvestLevel())));
        toolStats.getDefaultEnchantments(stack).forEach((enchantment, level) -> enchantments.put(enchantment,
                level.getLevel(finalToolProperty.getToolHarvestLevel())));
        enchantments.forEach((enchantment, level) -> {
            if (stack.getItem().canApplyAtEnchantingTable(stack, enchantment)) {
                stack.addEnchantment(enchantment, level);
            }
        });

        // Set behaviours
        NBTTagCompound behaviourTag = getBehaviorsTag(stack);
        getBehaviors(stack).forEach(behavior -> behavior.addBehaviorNBT(stack, behaviourTag));

        if (aoeDefinition != AoESymmetrical.none()) {
            behaviourTag.setInteger(MAX_AOE_COLUMN_KEY, aoeDefinition.column);
            behaviourTag.setInteger(MAX_AOE_ROW_KEY, aoeDefinition.row);
            behaviourTag.setInteger(MAX_AOE_LAYER_KEY, aoeDefinition.layer);
            behaviourTag.setInteger(AOE_COLUMN_KEY, aoeDefinition.column);
            behaviourTag.setInteger(AOE_ROW_KEY, aoeDefinition.row);
            behaviourTag.setInteger(AOE_LAYER_KEY, aoeDefinition.layer);
        }

        if (finalToolProperty.isMagnetic()) {
            behaviourTag.setBoolean(RELOCATE_MINED_BLOCKS_KEY, true);
        }

        return stack;
    }

    default ItemStack get(Material material, long defaultCharge, long defaultMaxCharge) {
        ItemStack stack = get(material);
        if (isElectric()) {
            ElectricItem electricItem = (ElectricItem) stack
                    .getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
            if (electricItem != null) {
                electricItem.setMaxChargeOverride(defaultMaxCharge);
                electricItem.setCharge(defaultCharge);
            }
        }
        return stack;
    }

    default ItemStack get(Material material, long defaultMaxCharge) {
        return get(material, defaultMaxCharge, defaultMaxCharge);
    }

    default Material getToolMaterial(ItemStack stack) {
        NBTTagCompound toolTag = getToolTag(stack);
        String string = toolTag.getString(MATERIAL_KEY);
        Material material = GregTechAPI.materialManager.getMaterial(string);
        if (material == null) {
            toolTag.setString(MATERIAL_KEY, (material = Materials.Iron).toString());
        }
        return material;
    }

    @Nullable
    default ToolProperty getToolProperty(Material material) {
        ToolProperty finalToolProperty;
        {
            MaterialToolProperty materialToolProperty = material.getProperty(PropertyKey.TOOL);
            if (material.hasProperty(PropertyKey.EXTRATOOL)) {
                finalToolProperty = material.getProperty(PropertyKey.EXTRATOOL)
                        .getOverriddenResult(this.getToolId(), materialToolProperty);
            } else {
                finalToolProperty = materialToolProperty;
            }
        }
        return finalToolProperty;
    }

    @Nullable
    default ToolProperty getToolProperty(ItemStack stack) {
        return getToolProperty(getToolMaterial(stack));
    }

    @Nullable
    default DustProperty getDustProperty(ItemStack stack) {
        return getToolMaterial(stack).getProperty(PropertyKey.DUST);
    }

    default float getMaterialToolSpeed(ItemStack stack) {
        ToolProperty toolProperty = getToolProperty(stack);
        return toolProperty == null ? 0F : toolProperty.getToolSpeed();
    }

    default float getMaterialAttackDamage(ItemStack stack) {
        ToolProperty toolProperty = getToolProperty(stack);
        return toolProperty == null ? 0F : toolProperty.getToolAttackDamage();
    }

    default float getMaterialAttackSpeed(ItemStack stack) {
        ToolProperty toolProperty = getToolProperty(stack);
        return toolProperty == null ? 0F : toolProperty.getToolAttackSpeed();
    }

    default int getMaterialDurability(ItemStack stack) {
        ToolProperty toolProperty = getToolProperty(stack);
        return toolProperty == null ? 0 : toolProperty.getToolDurability() * toolProperty.getDurabilityMultiplier();
    }

    default int getMaterialEnchantability(ItemStack stack) {
        ToolProperty toolProperty = getToolProperty(stack);
        return toolProperty == null ? 0 : toolProperty.getToolEnchantability();
    }

    default int getMaterialHarvestLevel(ItemStack stack) {
        ToolProperty toolProperty = getToolProperty(stack);
        return toolProperty == null ? 0 : toolProperty.getToolHarvestLevel();
    }

    default long getMaxCharge(ItemStack stack) {
        if (isElectric()) {
            NBTTagCompound tag = stack.getTagCompound();
            if (tag != null && tag.hasKey(MAX_CHARGE_KEY, Constants.NBT.TAG_LONG)) {
                return tag.getLong(MAX_CHARGE_KEY);
            }
        }
        return -1L;
    }

    default long getCharge(ItemStack stack) {
        if (isElectric()) {
            NBTTagCompound tag = stack.getTagCompound();
            if (tag != null && tag.hasKey(CHARGE_KEY, Constants.NBT.TAG_LONG)) {
                return tag.getLong(CHARGE_KEY);
            }
        }
        return -1L;
    }

    default float getTotalToolSpeed(ItemStack stack) {
        NBTTagCompound toolTag = getToolTag(stack);
        if (toolTag.hasKey(TOOL_SPEED_KEY, Constants.NBT.TAG_FLOAT)) {
            return toolTag.getFloat(TOOL_SPEED_KEY);
        }
        float toolSpeed = getToolStats().getEfficiencyMultiplier(stack) * getMaterialToolSpeed(stack) +
                getToolStats().getBaseEfficiency(stack);
        toolTag.setFloat(TOOL_SPEED_KEY, toolSpeed);
        return toolSpeed;
    }

    default float getTotalAttackDamage(ItemStack stack) {
        NBTTagCompound toolTag = getToolTag(stack);
        if (toolTag.hasKey(ATTACK_DAMAGE_KEY, Constants.NBT.TAG_FLOAT)) {
            return toolTag.getFloat(ATTACK_DAMAGE_KEY);
        }
        float baseDamage = getToolStats().getBaseDamage(stack);
        float attackDamage = 0;
        // represents a tool that should always have an attack damage value of 0
        if (baseDamage != Float.MIN_VALUE) {
            attackDamage = getMaterialAttackDamage(stack) + baseDamage;
        }
        toolTag.setFloat(ATTACK_DAMAGE_KEY, attackDamage);
        return attackDamage;
    }

    default float getTotalAttackSpeed(ItemStack stack) {
        NBTTagCompound toolTag = getToolTag(stack);
        if (toolTag.hasKey(ATTACK_SPEED_KEY, Constants.NBT.TAG_FLOAT)) {
            return toolTag.getFloat(ATTACK_SPEED_KEY);
        }
        float attackSpeed = getMaterialAttackSpeed(stack) + getToolStats().getAttackSpeed(stack);
        toolTag.setFloat(ATTACK_SPEED_KEY, attackSpeed);
        return attackSpeed;
    }

    default int getTotalMaxDurability(ItemStack stack) {
        NBTTagCompound toolTag = getToolTag(stack);
        if (toolTag.hasKey(MAX_DURABILITY_KEY, Constants.NBT.TAG_INT)) {
            return toolTag.getInteger(MAX_DURABILITY_KEY);
        }

        IGTToolDefinition toolStats = getToolStats();
        int maxDurability = getMaterialDurability(stack);
        int builderDurability = (int) (toolStats.getBaseDurability(stack) * toolStats.getDurabilityMultiplier(stack));

        // If there is no durability set in the tool builder, multiply the builder AOE multiplier to the material
        // durability
        maxDurability = builderDurability == 0 ? (int) (maxDurability * toolStats.getDurabilityMultiplier(stack)) :
                maxDurability + builderDurability;

        toolTag.setInteger(MAX_DURABILITY_KEY, maxDurability);
        return maxDurability;
    }

    default int getTotalEnchantability(ItemStack stack) {
        NBTTagCompound toolTag = getToolTag(stack);
        if (toolTag.hasKey(ENCHANTABILITY_KEY, Constants.NBT.TAG_INT)) {
            return toolTag.getInteger(ENCHANTABILITY_KEY);
        }
        int enchantability = getMaterialEnchantability(stack);
        toolTag.setInteger(ENCHANTABILITY_KEY, enchantability);
        return enchantability;
    }

    default int getTotalHarvestLevel(ItemStack stack) {
        NBTTagCompound toolTag = getToolTag(stack);
        if (toolTag.hasKey(HARVEST_LEVEL_KEY, Constants.NBT.TAG_INT)) {
            return toolTag.getInteger(HARVEST_LEVEL_KEY);
        }
        int harvestLevel = getMaterialHarvestLevel(stack) + getToolStats().getBaseQuality(stack);
        toolTag.setInteger(HARVEST_LEVEL_KEY, harvestLevel);
        return harvestLevel;
    }

    default AoESymmetrical getMaxAoEDefinition(ItemStack stack) {
        return AoESymmetrical.readMax(getBehaviorsTag(stack));
    }

    default AoESymmetrical getAoEDefinition(ItemStack stack) {
        return AoESymmetrical.read(getToolTag(stack), getMaxAoEDefinition(stack));
    }

    // Item.class methods
    default float definition$getDestroySpeed(ItemStack stack, IBlockState state) {
        // special case check (mostly for the sword)
        float specialValue = getDestroySpeed(state, getToolClasses(stack));
        if (specialValue != -1) return specialValue;

        if (isToolEffective(state, getToolClasses(stack), getTotalHarvestLevel(stack))) {
            return getTotalToolSpeed(stack);
        }

        return getToolStats().isToolEffective(state) ? getTotalToolSpeed(stack) : 1.0F;
    }

    default boolean definition$hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
        getBehaviors(stack).forEach(behavior -> behavior.hitEntity(stack, target, attacker));
        damageItem(stack, attacker, getToolStats().getToolDamagePerAttack(stack));
        return true;
    }

    default boolean definition$onBlockStartBreak(ItemStack stack, BlockPos pos, EntityPlayer player) {
        if (player.world.isRemote) return false;
        getBehaviors(stack).forEach(behavior -> behavior.onBlockStartBreak(stack, pos, player));

        if (!player.isSneaking()) {
            EntityPlayerMP playerMP = (EntityPlayerMP) player;
            int result = -1;
            if (isTool(stack, ToolClasses.SHEARS)) {
                result = shearBlockRoutine(playerMP, stack, pos);
            }
            if (result != 0) {
                // prevent exploits with instantly breakable blocks
                IBlockState state = player.world.getBlockState(pos);
                state = state.getBlock().getActualState(state, player.world, pos);
                boolean effective = false;
                for (String type : getToolClasses(stack)) {
                    if (state.getBlock().isToolEffective(type, state)) {
                        effective = true;
                        break;
                    }
                }

                effective |= isToolEffective(state, getToolClasses(stack), getTotalHarvestLevel(stack));

                if (effective) {
                    if (areaOfEffectBlockBreakRoutine(stack, playerMP)) {
                        if (playSoundOnBlockDestroy()) playSound(player);
                    } else {
                        if (result == -1) {
                            treeFellingRoutine(playerMP, stack, pos);
                            if (playSoundOnBlockDestroy()) playSound(player);
                        } else {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    default boolean definition$onBlockDestroyed(ItemStack stack, World worldIn, IBlockState state, BlockPos pos,
                                                EntityLivingBase entityLiving) {
        if (!worldIn.isRemote) {
            getBehaviors(stack)
                    .forEach(behavior -> behavior.onBlockDestroyed(stack, worldIn, state, pos, entityLiving));

            if ((double) state.getBlockHardness(worldIn, pos) != 0.0D) {
                damageItem(stack, entityLiving, getToolStats().getToolDamagePerBlockBreak(stack));
            }
            if (entityLiving instanceof EntityPlayer && playSoundOnBlockDestroy()) {
                // sneaking disables AOE, which means it is okay to play the sound
                // not checking this means the sound will play for every AOE broken block, which is very loud
                if (entityLiving.isSneaking()) {
                    playSound((EntityPlayer) entityLiving);
                }
            }
        }
        return true;
    }

    default boolean definition$getIsRepairable(ItemStack toRepair, ItemStack repair) {
        // full durability tools in the left slot are not repairable
        // this is needed so enchantment merging works when both tools are full durability
        if (toRepair.getItemDamage() == 0) return false;
        if (repair.getItem() instanceof IGTTool) {
            return getToolMaterial(toRepair) == ((IGTTool) repair.getItem()).getToolMaterial(repair);
        }
        UnificationEntry entry = OreDictUnifier.getUnificationEntry(repair);
        if (entry == null || entry.material == null) return false;
        if (entry.material == getToolMaterial(toRepair)) {
            // special case wood to allow Wood Planks
            if (ModHandler.isMaterialWood(entry.material)) {
                return entry.orePrefix == OrePrefix.plank;
            }
            // Gems can use gem and plate, Ingots can use ingot and plate
            if (entry.orePrefix == OrePrefix.plate) {
                return true;
            }
            if (entry.material.hasProperty(PropertyKey.INGOT)) {
                return entry.orePrefix == OrePrefix.ingot;
            }
            if (entry.material.hasProperty(PropertyKey.GEM)) {
                return entry.orePrefix == OrePrefix.gem;
            }
        }
        return false;
    }

    default Multimap<String, AttributeModifier> definition$getAttributeModifiers(EntityEquipmentSlot equipmentSlot,
                                                                                 ItemStack stack) {
        Multimap<String, AttributeModifier> multimap = HashMultimap.create();
        if (equipmentSlot == EntityEquipmentSlot.MAINHAND) {
            multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(),
                    new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", getTotalAttackDamage(stack), 0));
            multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER,
                    "Weapon modifier", Math.max(-3.9D, getTotalAttackSpeed(stack)), 0));
        }
        return multimap;
    }

    default int definition$getHarvestLevel(ItemStack stack, String toolClass, @Nullable EntityPlayer player,
                                           @Nullable IBlockState blockState) {
        return get().getToolClasses(stack).contains(toolClass) ? getTotalHarvestLevel(stack) : -1;
    }

    default boolean definition$canDisableShield(ItemStack stack, ItemStack shield, EntityLivingBase entity,
                                                EntityLivingBase attacker) {
        return getBehaviors(stack).stream()
                .anyMatch(behavior -> behavior.canDisableShield(stack, shield, entity, attacker));
    }

    default boolean definition$doesSneakBypassUse(@NotNull ItemStack stack, @NotNull IBlockAccess world,
                                                  @NotNull BlockPos pos, @NotNull EntityPlayer player) {
        return getToolStats().doesSneakBypassUse();
    }

    default boolean definition$shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack) {
        return oldStack.getItem() != newStack.getItem() || oldStack.getItemDamage() < newStack.getItemDamage();
    }

    default boolean definition$hasContainerItem(ItemStack stack) {
        return stack.getTagCompound() == null || !stack.getTagCompound().getBoolean(DISALLOW_CONTAINER_ITEM_KEY);
    }

    default ItemStack definition$getContainerItem(ItemStack stack) {
        // Sanity-check, callers should really validate with hasContainerItem themselves...
        if (!definition$hasContainerItem(stack)) {
            return ItemStack.EMPTY;
        }
        stack = stack.copy();
        EntityPlayer player = ForgeHooks.getCraftingPlayer();
        damageItemWhenCrafting(stack, player);
        playCraftingSound(player, stack);
        // We cannot simply return the copied stack here because Forge's bug
        // Introduced here: https://github.com/MinecraftForge/MinecraftForge/pull/3388
        // Causing PlayerDestroyItemEvent to never be fired under correct circumstances.
        // While preliminarily fixing ItemStack being null in ForgeHooks#getContainerItem in the PR
        // The semantics was misunderstood, any stack that are "broken" (damaged beyond maxDamage)
        // Will be "empty" ItemStacks (while not == ItemStack.EMPTY, but isEmpty() == true)
        // PlayerDestroyItemEvent will not be fired correctly because of this oversight.
        if (stack.isEmpty()) { // Equal to listening to PlayerDestroyItemEvent
            return getToolStats().getBrokenStack();
        }
        return stack;
    }

    default boolean definition$shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack,
                                                           boolean slotChanged) {
        if (getCharge(oldStack) != getCharge(newStack)) {
            return slotChanged;
        }
        return !oldStack.equals(newStack);
    }

    default boolean definition$onEntitySwing(EntityLivingBase entityLiving, ItemStack stack) {
        getBehaviors(stack).forEach(behavior -> behavior.onEntitySwing(entityLiving, stack));
        return false;
    }

    default boolean definition$canDestroyBlockInCreative(World world, BlockPos pos, ItemStack stack,
                                                         EntityPlayer player) {
        return true;
    }

    default boolean definition$isDamaged(ItemStack stack) {
        return definition$getDamage(stack) > 0;
    }

    default int definition$getDamage(ItemStack stack) {
        // bypass the Forge OreDictionary using ItemStack#getItemDamage instead of ItemStack#getMetadata
        // this will allow tools to retain their oredicts when durability changes.
        // No normal tool ItemStack a player has should ever have a metadata value other than 0
        // so this should not cause unexpected behavior for them
        if (stack.getMetadata() == GTValues.W) {
            return GTValues.W;
        }

        NBTTagCompound toolTag = getToolTag(stack);
        if (toolTag.hasKey(DURABILITY_KEY, Constants.NBT.TAG_INT)) {
            return toolTag.getInteger(DURABILITY_KEY);
        }
        toolTag.setInteger(DURABILITY_KEY, 0);
        return 0;
    }

    default int definition$getMaxDamage(ItemStack stack) {
        return getTotalMaxDurability(stack);
    }

    default void definition$setDamage(ItemStack stack, int durability) {
        NBTTagCompound toolTag = getToolTag(stack);
        toolTag.setInteger(DURABILITY_KEY, durability);
    }

    default double definition$getDurabilityForDisplay(ItemStack stack) {
        int damage = stack.getItem().getDamage(stack);
        int maxDamage = stack.getItem().getMaxDamage(stack);
        return (double) damage / (double) maxDamage;
    }

    @Nullable
    default ICapabilityProvider definition$initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
        List<ICapabilityProvider> providers = new ArrayList<>();
        if (isElectric()) {
            providers.add(ElectricStats.createElectricItem(0L, getElectricTier()).createProvider(stack));
        }
        for (IToolBehavior behavior : getBehaviors(stack)) {
            ICapabilityProvider behaviorProvider = behavior.createProvider(stack, nbt);
            if (behaviorProvider != null) {
                providers.add(behaviorProvider);
            }
        }
        if (providers.isEmpty()) return null;
        if (providers.size() == 1) return providers.get(0);
        return new CombinedCapabilityProvider(providers);
    }

    @NotNull
    default List<IToolBehavior> getBehaviors(ItemStack stack) {
        return getToolStats().getBehaviors();
    }

    default EnumActionResult definition$onItemUseFirst(@NotNull EntityPlayer player, @NotNull World world,
                                                       @NotNull BlockPos pos, @NotNull EnumFacing facing, float hitX,
                                                       float hitY, float hitZ, @NotNull EnumHand hand) {
        for (IToolBehavior behavior : getBehaviors(player.getHeldItem(hand))) {
            if (behavior.onItemUseFirst(player, world, pos, facing, hitX, hitY, hitZ, hand) ==
                    EnumActionResult.SUCCESS) {
                return EnumActionResult.SUCCESS;
            }
        }

        return EnumActionResult.PASS;
    }

    default EnumActionResult definition$onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand,
                                                  EnumFacing facing, float hitX, float hitY, float hitZ) {
        for (IToolBehavior behavior : getBehaviors(player.getHeldItem(hand))) {
            if (behavior.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ) ==
                    EnumActionResult.SUCCESS) {
                return EnumActionResult.SUCCESS;
            }
        }

        return EnumActionResult.PASS;
    }

    default ActionResult<ItemStack> definition$onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (!world.isRemote) {
            // TODO: relocate to keybind action when keybind PR happens
            if (player.isSneaking() && getMaxAoEDefinition(stack) != AoESymmetrical.none()) {
                ItemGuiFactory.INSTANCE.open((EntityPlayerMP) player, hand);
                return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
            }
        }

        for (IToolBehavior behavior : getBehaviors(stack)) {
            if (behavior.onItemRightClick(world, player, hand).getType() == EnumActionResult.SUCCESS) {
                return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
            }
        }
        return ActionResult.newResult(EnumActionResult.PASS, stack);
    }

    default void definition$getSubItems(@NotNull NonNullList<ItemStack> items) {
        if (getMarkerItem() != null) {
            items.add(getMarkerItem().get());
        } else if (isElectric()) {
            items.add(get(Materials.Iron, Integer.MAX_VALUE));
        } else {
            items.add(get(Materials.Iron));
        }
    }

    // Client-side methods

    @SideOnly(Side.CLIENT)
    default void definition$addInformation(@NotNull ItemStack stack, @Nullable World world,
                                           @NotNull List<String> tooltip, ITooltipFlag flag) {
        if (!(stack.getItem() instanceof IGTTool)) return;
        IGTTool tool = (IGTTool) stack.getItem();

        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) return;

        IGTToolDefinition toolStats = tool.getToolStats();

        // electric info
        if (this.isElectric()) {
            tooltip.add(I18n.format("metaitem.generic.electric_item.tooltip",
                    getCharge(stack),
                    getMaxCharge(stack),
                    GTValues.VNF[getElectricTier()]));
        }

        // durability info
        if (!tagCompound.getBoolean(UNBREAKABLE_KEY)) {
            // Plus 1 to match vanilla behavior where tools can still be used once at zero durability. We want to not
            // show this
            int damageRemaining = tool.getTotalMaxDurability(stack) - stack.getItemDamage() + 1;
            if (toolStats.isSuitableForCrafting(stack)) {
                tooltip.add(I18n.format("item.gt.tool.tooltip.crafting_uses", TextFormattingUtil
                        .formatNumbers(damageRemaining / Math.max(1, toolStats.getToolDamagePerCraft(stack)))));
            }

            tooltip.add(I18n.format("item.gt.tool.tooltip.general_uses",
                    TextFormattingUtil.formatNumbers(damageRemaining)));
        }

        // attack info
        if (toolStats.isSuitableForAttacking(stack)) {
            tooltip.add(I18n.format("item.gt.tool.tooltip.attack_damage",
                    TextFormattingUtil.formatNumbers(2 + tool.getTotalAttackDamage(stack))));
            tooltip.add(I18n.format("item.gt.tool.tooltip.attack_speed",
                    TextFormattingUtil.formatNumbers(4 + tool.getTotalAttackSpeed(stack))));
        }

        // mining info
        if (toolStats.isSuitableForBlockBreak(stack)) {
            tooltip.add(I18n.format("item.gt.tool.tooltip.mining_speed",
                    TextFormattingUtil.formatNumbers(tool.getTotalToolSpeed(stack))));

            int harvestLevel = tool.getTotalHarvestLevel(stack);
            String harvestName = "item.gt.tool.harvest_level." + harvestLevel;
            if (I18n.hasKey(harvestName)) { // if there's a defined name for the harvest level, use it
                tooltip.add(I18n.format("item.gt.tool.tooltip.harvest_level_extra", harvestLevel,
                        I18n.format(harvestName)));
            } else {
                tooltip.add(I18n.format("item.gt.tool.tooltip.harvest_level", harvestLevel));
            }
        }

        // behaviors
        boolean addedBehaviorNewLine = false;
        AoESymmetrical aoeDefinition = ToolHelper.getAoEDefinition(stack);

        if (aoeDefinition != AoESymmetrical.none()) {
            addedBehaviorNewLine = tooltip.add("");
            tooltip.add(I18n.format("item.gt.tool.behavior.aoe_mining",
                    aoeDefinition.column * 2 + 1, aoeDefinition.row * 2 + 1, aoeDefinition.layer + 1));
        }

        NBTTagCompound behaviorsTag = getBehaviorsTag(stack);
        if (behaviorsTag.getBoolean(RELOCATE_MINED_BLOCKS_KEY)) {
            if (!addedBehaviorNewLine) {
                addedBehaviorNewLine = true;
                tooltip.add("");
            }
            tooltip.add(I18n.format("item.gt.tool.behavior.relocate_mining"));
        }

        if (!addedBehaviorNewLine && !getBehaviors(stack).isEmpty()) {
            tooltip.add("");
        }
        getBehaviors(stack).forEach(behavior -> behavior.addInformation(stack, world, tooltip, flag));

        // unique tooltip
        String uniqueTooltip = "item.gt.tool." + getToolId() + ".tooltip";
        if (I18n.hasKey(uniqueTooltip)) {
            tooltip.add("");
            tooltip.add(I18n.format(uniqueTooltip));
        }

        tooltip.add("");

        // valid tools
        tooltip.add(I18n.format("item.gt.tool.usable_as",
                stack.getItem().getToolClasses(stack).stream()
                        .map(s -> I18n.format("gt.tool.class." + s))
                        .collect(Collectors.joining(", "))));

        // repair info
        if (!tagCompound.getBoolean(UNBREAKABLE_KEY)) {
            if (TooltipHelper.isShiftDown()) {
                Material material = getToolMaterial(stack);

                Collection<String> repairItems = new ArrayList<>();
                if (!ModHandler.isMaterialWood(material)) {
                    if (material.hasProperty(PropertyKey.INGOT)) {
                        repairItems.add(OrePrefix.ingot.getLocalNameForItem(material));
                    } else if (material.hasProperty(PropertyKey.GEM)) {
                        repairItems.add(OrePrefix.gem.getLocalNameForItem(material));
                    }
                }
                if (!OreDictUnifier.get(OrePrefix.plate, material).isEmpty()) {
                    repairItems.add(OrePrefix.plate.getLocalNameForItem(material));
                }
                if (!repairItems.isEmpty()) {
                    tooltip.add(I18n.format("item.gt.tool.tooltip.repair_material", String.join(", ", repairItems)));
                }
            } else {
                tooltip.add(I18n.format("item.gt.tool.tooltip.repair_info"));
            }
        }
        if (this.isElectric()) {
            tooltip.add(I18n.format("item.gt.tool.replace_tool_head"));
        }
    }

    default boolean definition$canApplyAtEnchantingTable(@NotNull ItemStack stack, Enchantment enchantment) {
        if (stack.isEmpty()) return false;

        // special case enchants from other mods
        switch (enchantment.getName()) {
            case "enchantment.cofhcore.smashing":
                // block cofhcore smashing enchant from all tools
                return false;
            case "enchantment.autosmelt": // endercore
            case "enchantment.cofhcore.smelting": // cofhcore
            case "enchantment.as.smelting": // astral sorcery
                // block autosmelt enchants from AoE and Tree-Felling tools
                return getToolStats().getAoEDefinition(stack) == AoESymmetrical.none() &&
                        !getBehaviorsTag(stack).hasKey(TREE_FELLING_KEY);
        }

        // Block Mending and Unbreaking on Electric tools
        if (isElectric() &&
                (enchantment instanceof EnchantmentMending || enchantment instanceof EnchantmentDurability)) {
            return false;
        }

        if (enchantment.type == null) return true;
        // bypass EnumEnchantmentType#canEnchantItem and define custom stack-aware logic.
        // the Minecraft method takes an Item, and does not respect NBT nor meta.
        switch (enchantment.type) {
            case DIGGER: {
                return getToolStats().isSuitableForBlockBreak(stack);
            }
            case WEAPON: {
                return getToolStats().isSuitableForAttacking(stack);
            }
            case BREAKABLE:
                return stack.getTagCompound() != null && !stack.getTagCompound().getBoolean(UNBREAKABLE_KEY);
            case ALL: {
                return true;
            }
        }

        ToolProperty property = getToolProperty(stack);
        if (property == null) return false;

        // Check for any special enchantments specified by the material of this Tool
        if (!property.getEnchantments().isEmpty() && property.getEnchantments().containsKey(enchantment)) {
            return true;
        }

        // Check for any additional Enchantment Types added in the builder
        return getToolStats().isEnchantable(stack) && getToolStats().canApplyEnchantment(stack, enchantment);
    }

    @SideOnly(Side.CLIENT)
    default int getColor(ItemStack stack, int tintIndex) {
        return tintIndex % 2 == 1 ? getToolMaterial(stack).getMaterialRGB() : 0xFFFFFF;
    }

    @SideOnly(Side.CLIENT)
    default String getModelPath() {
        return getDomain() + ":" + "tools/" + getToolId();
    }

    @SideOnly(Side.CLIENT)
    default ModelResourceLocation getModelLocation() {
        return new ModelResourceLocation(getModelPath(), "inventory");
    }

    // Sound Playing
    default void playCraftingSound(EntityPlayer player, ItemStack stack) {
        stack = toolbeltPassthrough(stack);
        // player null check for things like auto-crafters
        if (ConfigHolder.client.toolCraftingSounds && getSound() != null && player != null) {
            if (canPlaySound(stack)) {
                setLastCraftingSoundTime(stack);
                player.getEntityWorld().playSound(null, player.posX, player.posY, player.posZ, getSound(),
                        SoundCategory.PLAYERS, 1F, 1F);
            }
        }
    }

    default void setLastCraftingSoundTime(ItemStack stack) {
        getToolTag(stack).setInteger(LAST_CRAFTING_USE_KEY, (int) System.currentTimeMillis());
    }

    default boolean canPlaySound(ItemStack stack) {
        return Math.abs((int) System.currentTimeMillis() - getToolTag(stack).getInteger(LAST_CRAFTING_USE_KEY)) > 1000;
    }

    default void playSound(EntityPlayer player) {
        if (ConfigHolder.client.toolUseSounds && getSound() != null) {
            player.getEntityWorld().playSound(null, player.posX, player.posY, player.posZ, getSound(),
                    SoundCategory.PLAYERS, 1F, 1F);
        }
    }

    @Override
    default ModularPanel buildUI(HandGuiData guiData, PanelSyncManager manager) {
        final var usedStack = guiData.getUsedItemStack();
        final var behaviorsTag = getBehaviorsTag(usedStack);
        final var defaultDefinition = getMaxAoEDefinition(usedStack);

        var columnValue = new IntSyncValue(
                () -> AoESymmetrical.getColumn(behaviorsTag, defaultDefinition),
                i -> AoESymmetrical.setColumn(behaviorsTag, i, defaultDefinition));
        var rowValue = new IntSyncValue(
                () -> AoESymmetrical.getRow(behaviorsTag, defaultDefinition),
                i -> AoESymmetrical.setRow(behaviorsTag, i, defaultDefinition));
        var layerValue = new IntSyncValue(
                () -> AoESymmetrical.getLayer(behaviorsTag, defaultDefinition),
                i -> AoESymmetrical.setLayer(behaviorsTag, i, defaultDefinition));

        manager.syncValue("row_value", rowValue);
        manager.syncValue("column_value", columnValue);
        manager.syncValue("layer_value", layerValue);

        return GTGuis.createPanel(usedStack.getTranslationKey(), 120, 80)
                .child(Flow.row()
                        .widthRel(1f)
                        .margin(4, 0)
                        .alignY(0.5f)
                        .coverChildrenHeight()
                        .mainAxisAlignment(Alignment.MainAxis.SPACE_BETWEEN)
                        .child(createColumn(columnValue, "columns", true, defaultDefinition.column))
                        .child(createColumn(rowValue, "rows", true, defaultDefinition.row))
                        .child(createColumn(layerValue, "layers", false, defaultDefinition.layer)));
    }

    default Flow createColumn(IntSyncValue syncValue, String lang, boolean shouldDouble, int max) {
        final var display = IKey.dynamic(
                () -> String.valueOf(1 + (shouldDouble ? 2 * syncValue.getIntValue() : syncValue.getIntValue())));

        IWidget increaseButton = new ButtonWidget<>()
                .size(9, 18)
                .background(GTGuiTextures.MC_BUTTON)
                .overlay(GTGuiTextures.PLUS)
                .disableHoverBackground()
                .onMousePressed(data -> {
                    int val = syncValue.getIntValue();
                    if (val < max) syncValue.setIntValue(++val);
                    return true;
                });

        IWidget decreaseButton = new ButtonWidget<>()
                .size(9, 18)
                .background(GTGuiTextures.MC_BUTTON)
                .overlay(GTGuiTextures.MINUS)
                .disableHoverBackground()
                .onMousePressed(data -> {
                    int val = syncValue.getIntValue();
                    if (val > 0) syncValue.setIntValue(--val);
                    return true;
                });

        return Flow.column()
                .coverChildren()
                .child(new TextWidget(IKey.lang("item.gt.tool.aoe." + lang))
                        .marginBottom(5))
                .child(Flow.row()
                        .coverChildren()
                        .marginBottom(5)
                        .child(increaseButton)
                        .child(decreaseButton))
                .child(new TextWidget(display)
                        .alignment(Alignment.Center)
                        .widthRel(1f));
    }

    Set<String> getToolClasses(ItemStack stack);

    // Extended Interfaces

    // IAEWrench

    /**
     * Check if the wrench can be used.
     *
     * @param player wrenching player
     * @param pos    of block.
     * @return true if wrench can be used
     */
    @Override
    default boolean canWrench(ItemStack wrench, EntityPlayer player, BlockPos pos) {
        return get().getToolClasses(wrench).contains(ToolClasses.WRENCH);
    }

    // IToolWrench

    /***
     * Called to ensure that the wrench can be used.
     *
     * @param player   - The player doing the wrenching
     * @param hand     - Which hand was holding the wrench
     * @param wrench   - The item stack that holds the wrench
     * @param rayTrace - The object that is being wrenched
     *
     * @return true if wrenching is allowed, false if not
     */
    @Override
    default boolean canWrench(EntityPlayer player, EnumHand hand, ItemStack wrench, RayTraceResult rayTrace) {
        return get().getToolClasses(wrench).contains(ToolClasses.WRENCH);
    }

    /***
     * Callback after the wrench has been used. This can be used to decrease durability or for other purposes.
     *
     * @param player   - The player doing the wrenching
     * @param hand     - Which hand was holding the wrench
     * @param wrench   - The item stack that holds the wrench
     * @param rayTrace - The object that is being wrenched
     */
    @Override
    default void wrenchUsed(EntityPlayer player, EnumHand hand, ItemStack wrench, RayTraceResult rayTrace) {
        damageItem(player.getHeldItem(hand), player);
        playSound(player);
    }

    // IToolHammer
    @Override
    default boolean isUsable(ItemStack item, EntityLivingBase user, BlockPos pos) {
        return get().getToolClasses(item).contains(ToolClasses.WRENCH);
    }

    @Override
    default boolean isUsable(ItemStack item, EntityLivingBase user, Entity entity) {
        return get().getToolClasses(item).contains(ToolClasses.WRENCH);
    }

    @Override
    default void toolUsed(ItemStack item, EntityLivingBase user, BlockPos pos) {
        damageItem(item, user);
        if (user instanceof EntityPlayer) {
            playSound((EntityPlayer) user);
        }
    }

    @Override
    default void toolUsed(ItemStack item, EntityLivingBase user, Entity entity) {
        damageItem(item, user);
    }

    // ITool
    @Override
    default boolean canUse(@NotNull EnumHand hand, @NotNull EntityPlayer player, @NotNull BlockPos pos) {
        return get().getToolClasses(player.getHeldItem(hand)).contains(ToolClasses.WRENCH);
    }

    @Override
    default void used(@NotNull EnumHand hand, @NotNull EntityPlayer player, @NotNull BlockPos pos) {
        damageItem(player.getHeldItem(hand), player);
        playSound(player);
    }

    // IHideFacades
    @Override
    default boolean shouldHideFacades(@NotNull ItemStack stack, @NotNull EntityPlayer player) {
        return get().getToolClasses(stack).contains(ToolClasses.WRENCH);
    }

    // IToolGrafter

    /**
     * Called by leaves to determine the increase in sapling droprate.
     *
     * @param stack ItemStack containing the grafter.
     * @param world Minecraft world the player and the target block inhabit.
     * @param pos   Coordinate of the broken leaf block.
     * @return Float representing the factor the usual drop chance is to be multiplied by.
     */
    @Override
    default float getSaplingModifier(ItemStack stack, World world, EntityPlayer player, BlockPos pos) {
        return getToolClasses(stack).contains(ToolClasses.GRAFTER) ? 100F : 1.0F;
    }

    // IOverlayRenderAware
    @Override
    default void renderItemOverlayIntoGUI(@NotNull ItemStack stack, int xPosition, int yPosition) {
        ToolChargeBarRenderer.renderBarsTool(this, stack, xPosition, yPosition);
    }

    // IScrewdriver
    @Override
    default boolean canUse(EntityPlayer player, ItemStack stack) {
        return get().getToolClasses(stack).contains(ToolClasses.SCREWDRIVER);
    }

    @Override
    default void damageScrewdriver(EntityPlayer player, ItemStack stack) {
        damageItem(stack, player);
    }

    // IToolCrowbar

    @Override
    default boolean canWhack(EntityPlayer player, EnumHand hand, ItemStack crowbar, BlockPos pos) {
        return get().getToolClasses(crowbar).contains(ToolClasses.CROWBAR);
    }

    @Override
    default void onWhack(EntityPlayer player, EnumHand hand, ItemStack crowbar, BlockPos pos) {
        damageItem(crowbar, player);
    }

    @Override
    default boolean canLink(EntityPlayer player, EnumHand hand, ItemStack crowbar, EntityMinecart cart) {
        return get().getToolClasses(crowbar).contains(ToolClasses.CROWBAR);
    }

    @Override
    default void onLink(EntityPlayer player, EnumHand hand, ItemStack crowbar, EntityMinecart cart) {
        damageItem(crowbar, player);
    }

    @Override
    default boolean canBoost(EntityPlayer player, EnumHand hand, ItemStack crowbar, EntityMinecart cart) {
        return get().getToolClasses(crowbar).contains(ToolClasses.CROWBAR);
    }

    @Override
    default void onBoost(EntityPlayer player, EnumHand hand, ItemStack crowbar, EntityMinecart cart) {
        damageItem(crowbar, player);
    }
}
