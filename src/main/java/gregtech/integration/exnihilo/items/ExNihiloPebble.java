package gregtech.integration.exnihilo.items;

import gregtech.integration.exnihilo.ExNihiloModule;

import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import exnihilocreatio.ExNihiloCreatio;
import exnihilocreatio.entities.ProjectileStone;
import exnihilocreatio.util.Data;
import exnihilocreatio.util.IHasModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExNihiloPebble extends Item implements IHasModel {

    public ExNihiloPebble() {
        setTranslationKey("gtPebble");
        setRegistryName("gtPebble");
        setCreativeTab(ExNihiloCreatio.tabExNihilo);
        setHasSubtypes(true);
        Data.ITEMS.add(this);
    }

    @Override
    @NotNull
    public String getTranslationKey(@NotNull ItemStack stack) {
        return String.format("%s.%s", getTranslationKey(), GTPebbles.VALUES[stack.getItemDamage()].getName());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(@Nullable CreativeTabs tab, @NotNull NonNullList<ItemStack> list) {
        if (tab != null && isInCreativeTab(tab)) {
            for (GTPebbles pebble : GTPebbles.VALUES) {
                list.add(new ItemStack(ExNihiloModule.GTPebbles, 1, pebble.ordinal()));
            }
        }
    }

    @Override
    @NotNull
    public ActionResult<ItemStack> onItemRightClick(@NotNull World world, @NotNull EntityPlayer player,
                                                    @NotNull EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);

        if (!stack.isEmpty()) {
            world.playSound(player, player.posX, player.posY, player.posZ,
                    SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.NEUTRAL, 0.5F,
                    0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));

            if (!world.isRemote) {
                ItemStack thrown = stack.copy();
                thrown.setCount(1);

                ProjectileStone projectile = new ProjectileStone(world, player);
                projectile.setStack(thrown);
                projectile.shoot(player, player.rotationPitch, player.rotationYaw, 0.0F, 1.5F, 0.5F);
                world.spawnEntity(projectile);
            }

            if (!player.capabilities.isCreativeMode) {
                stack.shrink(1);
            }
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }
        return new ActionResult<>(EnumActionResult.FAIL, stack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void initModel(ModelRegistryEvent e) {
        ModelResourceLocation[] locations = new ModelResourceLocation[GTPebbles.VALUES.length];
        for (GTPebbles pebble : GTPebbles.VALUES) {
            locations[pebble.ordinal()] = new ModelResourceLocation(getRegistryName(),
                    String.format("type=%s", pebble.getName()));
        }

        ModelBakery.registerItemVariants(this, locations);
        ModelLoader.setCustomMeshDefinition(this, stack -> locations[stack.getMetadata()]);
    }

    private enum GTPebbles implements IStringSerializable {

        BASALT("basalt"),
        BLACK_GRANITE("black_granite"),
        MARBLE("marble"),
        RED_GRANITE("red_granite");

        private final String name;
        public static final GTPebbles[] VALUES = values();

        GTPebbles(String name) {
            this.name = name;
        }

        @Override
        @NotNull
        public String getName() {
            return this.name;
        }
    }
}
