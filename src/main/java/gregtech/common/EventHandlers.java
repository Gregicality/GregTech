package gregtech.common;

import gregtech.api.GTValues;
import gregtech.common.blocks.BlockConcrete;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovementInput;
import net.minecraft.util.MovementInputFromOptions;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = GTValues.MODID)
public class EventHandlers {

    @SubscribeEvent
    public static void onEndermanTeleportEvent(EnderTeleportEvent event) {
        if (event.getEntity() instanceof EntityEnderman && event.getEntityLiving()
                .getActivePotionEffect(MobEffects.WEAKNESS) != null) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onPlayerInteraction(PlayerInteractEvent.RightClickBlock event) {
        ItemStack stack = event.getItemStack();
        if (!stack.isEmpty() && stack.getItem() == Items.FLINT_AND_STEEL) {
            if (!event.getWorld().isRemote
                    && !event.getEntityPlayer().capabilities.isCreativeMode
                    && event.getWorld().rand.nextInt(100) >= ConfigHolder.flintChanceToCreateFire) {
                stack.damageItem(1, event.getEntityPlayer());
                if (stack.getItemDamage() >= stack.getMaxDamage()) {
                    stack.shrink(1);
                }
                event.setCanceled(true);
            }
        }
    }

    private static MovementInput manualInputCheck;

    @SubscribeEvent
    public static void onWalkSpeed(PlayerTickEvent event) {
        // Thanks, Chisel devs!
        if (event.phase == TickEvent.Phase.START && event.side.isClient() && event.player.onGround && event.player instanceof EntityPlayerSP) {
            if (manualInputCheck == null) {
                manualInputCheck = new MovementInputFromOptions(Minecraft.getMinecraft().gameSettings);
            }
            EntityPlayer player = event.player;
            IBlockState below = player.getEntityWorld().getBlockState(new BlockPos(player.posX, player.posY - (1 / 16D), player.posZ));
            if (below.getBlock() instanceof BlockConcrete) {
                manualInputCheck.updatePlayerMoveState();
                if ((manualInputCheck.moveForward != 0 || manualInputCheck.moveStrafe != 0) && !player.isInWater()) {
                    player.motionX *= 1.6;
                    player.motionZ *= 1.6;
                }
            }
        }
    }
}
