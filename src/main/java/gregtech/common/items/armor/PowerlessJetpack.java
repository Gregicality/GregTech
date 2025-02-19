package gregtech.common.items.armor;

import gregtech.api.capability.IFilter;
import gregtech.api.capability.impl.GTFluidHandlerItemStack;
import gregtech.api.items.armor.ArmorMetaItem;
import gregtech.api.items.armor.ArmorMetaItem.ArmorMetaValueItem;
import gregtech.api.items.armor.ArmorUtils;
import gregtech.api.items.armor.ISpecialArmorLogic;
import gregtech.api.items.metaitem.stats.*;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.GTUtility;
import gregtech.api.util.GradientUtil;
import gregtech.api.util.input.KeyBind;

import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.ISpecialArmor;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class PowerlessJetpack implements ISpecialArmorLogic, IJetpack, IItemHUDProvider {

    public static final int tankCapacity = 16000;

    private Recipe previousRecipe = null;
    private Recipe currentRecipe = null;
    private int burnTimer = 0;

    @SideOnly(Side.CLIENT)
    private ArmorUtils.ModularHUD HUD;

    public PowerlessJetpack() {
        if (ArmorUtils.SIDE.isClient())
            // noinspection NewExpressionSideOnly
            HUD = new ArmorUtils.ModularHUD();
    }

    @Override
    public void onArmorTick(World world, EntityPlayer player, @NotNull ItemStack stack) {
        IFluidHandlerItem internalTank = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY,
                null);
        if (internalTank == null)
            return;

        NBTTagCompound data = GTUtility.getOrCreateNbtCompound(stack);
        byte toggleTimer = 0;
        boolean hover = false;

        if (data.hasKey("burnTimer")) burnTimer = data.getShort("burnTimer");
        if (data.hasKey("toggleTimer")) toggleTimer = data.getByte("toggleTimer");
        if (data.hasKey("hover")) hover = data.getBoolean("hover");

        if (toggleTimer == 0 && KeyBind.ARMOR_HOVER.isKeyDown(player)) {
            hover = !hover;
            toggleTimer = 5;
            data.setBoolean("hover", hover);
            if (!world.isRemote) {
                if (hover)
                    player.sendStatusMessage(new TextComponentTranslation("metaarmor.jetpack.hover.enable"), true);
                else
                    player.sendStatusMessage(new TextComponentTranslation("metaarmor.jetpack.hover.disable"), true);
            }
        }

        // This causes a caching issue. currentRecipe is only set to null in findNewRecipe, so the fuel is never updated
        // Rewrite in Armor Rework
        if (currentRecipe == null)
            findNewRecipe(stack);

        performFlying(player, hover, false, stack);

        if (toggleTimer > 0)
            toggleTimer--;

        data.setBoolean("hover", hover);
        data.setShort("burnTimer", (short) burnTimer);
        data.setByte("toggleTimer", toggleTimer);
        player.inventoryContainer.detectAndSendChanges();
    }

    @Override
    public EntityEquipmentSlot getEquipmentSlot(ItemStack itemStack) {
        return EntityEquipmentSlot.CHEST;
    }

    @Override
    public void addToolComponents(@NotNull ArmorMetaValueItem mvi) {
        mvi.addComponents(new Behaviour(tankCapacity));
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
        return "gregtech:textures/armor/liquid_fuel_jetpack.png";
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void drawHUD(@NotNull ItemStack item) {
        IFluidHandlerItem tank = item.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        if (tank != null) {
            IFluidTankProperties[] prop = tank.getTankProperties();
            if (prop[0] != null) {
                if (prop[0].getContents() != null) {
                    if (prop[0].getContents().amount == 0) return;
                    String formated = String.format("%.1f",
                            (prop[0].getContents().amount * 100.0F / prop[0].getCapacity()));
                    this.HUD.newString(I18n.format("metaarmor.hud.fuel_lvl", formated + "%"));
                    NBTTagCompound data = item.getTagCompound();

                    if (data != null) {
                        if (data.hasKey("hover")) {
                            String status = (data.getBoolean("hover") ? I18n.format("metaarmor.hud.status.enabled") :
                                    I18n.format("metaarmor.hud.status.disabled"));
                            String result = I18n.format("metaarmor.hud.hover_mode", status);
                            this.HUD.newString(result);
                        }
                    }
                }
            }
        }
        this.HUD.draw();
        this.HUD.reset();
    }

    @Override
    public int getEnergyPerUse() {
        return 1;
    }

    @Override
    public boolean canUseEnergy(ItemStack stack, int amount) {
        FluidStack fuel = getFuel();
        if (fuel == null) {
            return false;
        }

        IFluidHandlerItem fluidHandlerItem = getIFluidHandlerItem(stack);
        if (fluidHandlerItem == null)
            return false;

        FluidStack fluidStack = fluidHandlerItem.drain(fuel, false);
        if (fluidStack == null)
            return false;

        return fluidStack.amount >= fuel.amount;
    }

    @Override
    public void drainEnergy(ItemStack stack, int amount) {
        if (this.burnTimer == 0) {
            FluidStack fuel = getFuel();
            if (fuel == null) return;
            getIFluidHandlerItem(stack).drain(fuel, true);
            burnTimer = currentRecipe.getDuration();
        }
        this.burnTimer--;
    }

    @Override
    public boolean hasEnergy(ItemStack stack) {
        return burnTimer > 0 || currentRecipe != null;
    }

    private static IFluidHandlerItem getIFluidHandlerItem(@NotNull ItemStack stack) {
        return stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
    }

    public void findNewRecipe(@NotNull ItemStack stack) {
        IFluidHandlerItem internalTank = getIFluidHandlerItem(stack);
        if (internalTank != null) {
            FluidStack fluidStack = internalTank.drain(1, false);
            if (previousRecipe != null && fluidStack != null &&
                    fluidStack.isFluidEqual(previousRecipe.getFluidInputs().get(0).getInputFluidStack()) &&
                    fluidStack.amount > 0) {
                currentRecipe = previousRecipe;
                return;
            } else if (fluidStack != null) {
                Recipe recipe = RecipeMaps.COMBUSTION_GENERATOR_FUELS.find(Collections.emptyList(),
                        Collections.singletonList(fluidStack), (Objects::nonNull));
                if (recipe != null) {
                    previousRecipe = recipe;
                    currentRecipe = previousRecipe;
                    return;
                }
            }
        }
        currentRecipe = null;
    }

    public void resetRecipe() {
        currentRecipe = null;
        previousRecipe = null;
    }

    public FluidStack getFuel() {
        if (currentRecipe != null) {
            return currentRecipe.getFluidInputs().get(0).getInputFluidStack();
        }

        return null;
    }

    public ActionResult<ItemStack> onRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (player.getHeldItem(hand).getItem() instanceof ArmorMetaItem) {
            ItemStack armor = player.getHeldItem(hand);
            if (armor.getItem() instanceof ArmorMetaItem && player.inventory.armorInventory
                    .get(getEquipmentSlot(player.getHeldItem(hand)).getIndex()).isEmpty() && !player.isSneaking()) {
                player.inventory.armorInventory.set(getEquipmentSlot(player.getHeldItem(hand)).getIndex(),
                        armor.copy());
                player.setHeldItem(hand, ItemStack.EMPTY);
                player.playSound(new SoundEvent(new ResourceLocation("item.armor.equip_generic")), 1.0F, 1.0F);
                return ActionResult.newResult(EnumActionResult.SUCCESS, armor);
            }
        }

        return ActionResult.newResult(EnumActionResult.PASS, player.getHeldItem(hand));
    }

    @Override
    public ISpecialArmor.ArmorProperties getProperties(EntityLivingBase player, @NotNull ItemStack armor,
                                                       @NotNull DamageSource source, double damage,
                                                       EntityEquipmentSlot equipmentSlot) {
        int damageLimit = (int) Math.min(Integer.MAX_VALUE, burnTimer * 1.0 / 32 * 25.0);
        if (source.isUnblockable()) return new ISpecialArmor.ArmorProperties(0, 0.0, 0);
        return new ISpecialArmor.ArmorProperties(0, 0, damageLimit);
    }

    @Override
    public int getArmorDisplay(EntityPlayer player, @NotNull ItemStack armor, int slot) {
        return 0;
    }

    public class Behaviour implements IItemDurabilityManager, IItemCapabilityProvider, IItemBehaviour, ISubItemHandler {

        private static final IFilter<FluidStack> JETPACK_FUEL_FILTER = new IFilter<>() {

            @Override
            public boolean test(@NotNull FluidStack fluidStack) {
                return RecipeMaps.COMBUSTION_GENERATOR_FUELS.find(Collections.emptyList(),
                        Collections.singletonList(fluidStack), (Objects::nonNull)) != null;
            }

            @Override
            public int getPriority() {
                return IFilter.whitelistLikePriority();
            }
        };

        public final int maxCapacity;
        private final Pair<Color, Color> durabilityBarColors;

        public Behaviour(int internalCapacity) {
            this.maxCapacity = internalCapacity;
            this.durabilityBarColors = GradientUtil.getGradient(0xB7AF08, 10);
        }

        @Override
        public double getDurabilityForDisplay(@NotNull ItemStack itemStack) {
            IFluidHandlerItem fluidHandlerItem = itemStack
                    .getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
            if (fluidHandlerItem == null) return 0;
            IFluidTankProperties fluidTankProperties = fluidHandlerItem.getTankProperties()[0];
            FluidStack fluidStack = fluidTankProperties.getContents();
            return fluidStack == null ? 0 : (double) fluidStack.amount / (double) fluidTankProperties.getCapacity();
        }

        @Nullable
        @Override
        public Pair<Color, Color> getDurabilityColorsForDisplay(ItemStack itemStack) {
            return durabilityBarColors;
        }

        @Override
        public ICapabilityProvider createProvider(ItemStack itemStack) {
            return new GTFluidHandlerItemStack(itemStack, maxCapacity)
                    .setFilter(JETPACK_FUEL_FILTER);
        }

        @Override
        public void addInformation(ItemStack itemStack, List<String> lines) {
            IItemBehaviour.super.addInformation(itemStack, lines);
            NBTTagCompound data = GTUtility.getOrCreateNbtCompound(itemStack);
            String status = I18n.format("metaarmor.hud.status.disabled");
            if (data.hasKey("hover")) {
                if (data.getBoolean("hover"))
                    status = I18n.format("metaarmor.hud.status.enabled");
            }
            lines.add(I18n.format("metaarmor.hud.hover_mode", status));
        }

        @Override
        public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
            return onRightClick(world, player, hand);
        }

        @Override
        public String getItemSubType(ItemStack itemStack) {
            return "";
        }

        @Override
        public void getSubItems(ItemStack itemStack, CreativeTabs creativeTab, NonNullList<ItemStack> subItems) {
            ItemStack copy = itemStack.copy();
            IFluidHandlerItem fluidHandlerItem = copy
                    .getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
            if (fluidHandlerItem != null) {
                fluidHandlerItem.fill(Materials.Diesel.getFluid(tankCapacity), true);
                subItems.add(copy);
            } else {
                subItems.add(itemStack);
            }
        }
    }
}
