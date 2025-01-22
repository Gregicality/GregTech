package gregtech.common;

import gregtech.api.GTValues;
import gregtech.api.items.armoritem.ArmorHelper;
import gregtech.api.items.armoritem.IGTArmor;

import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static net.minecraft.inventory.EntityEquipmentSlot.MAINHAND;
import static net.minecraft.inventory.EntityEquipmentSlot.OFFHAND;

@Mod.EventBusSubscriber(modid = GTValues.MODID)
public class ArmorEventHandlers {

    @SubscribeEvent
    public static void onLivingEquipmentChange(LivingEquipmentChangeEvent event) {
        EntityEquipmentSlot slot = event.getSlot();
        if (slot == MAINHAND || slot == OFFHAND) {
            return;
        }
        if (!(event.getEntityLiving() instanceof EntityPlayer player)) {
            return;
        }
        // maybe unnecessary sanity check to make sure this same item wasn't immediately re-equipped
        if (event.getFrom().isItemEqual(event.getTo())) {
            return;
        }

        if (event.getFrom().getItem() instanceof IGTArmor armor) {
            armor.onArmorUnequip(player.getEntityWorld(), player, event.getFrom());
        }
        if (event.getTo().getItem() instanceof IGTArmor armor) {
            armor.onArmorEquip(player.getEntityWorld(), player, event.getTo());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onLivingFall(LivingFallEvent event) {
        if (event.getEntity() instanceof EntityPlayerMP player) {
            if (player.fallDistance < 3.2f) {
                return;
            }

            for (EntityEquipmentSlot slot : ArmorHelper.getArmorSlots()) {
                ItemStack armorStack = player.getItemStackFromSlot(slot);
                if (armorStack.isEmpty() || !(armorStack.getItem() instanceof IGTArmor gtArmor)) {
                    continue;
                }
                // todo make sure jetpack sets this fall damage key when needed ("flyMode" nbt key previously?)
                NBTTagCompound behaviorTag = ArmorHelper.getBehaviorsTag(armorStack);
                if (behaviorTag.getBoolean(ArmorHelper.FALL_DAMAGE_KEY)) {
                    if (gtArmor.areBehaviorsActive(armorStack)) {
                        gtArmor.damageArmor(player, armorStack, DamageSource.FALL, (int) (player.fallDistance - 1.2f),
                                slot.getIndex());
                        player.fallDistance = 0;
                        event.setCanceled(true);
                        return;
                    }
                }
            }
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void onClientPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START && !event.player.isSpectator() &&
                !(event.player instanceof EntityOtherPlayerMP) && !(event.player instanceof FakePlayer)) {
            onPlayerTick(event);
        }
    }

    @SubscribeEvent
    @SideOnly(Side.SERVER)
    public static void onServerPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START && !event.player.isSpectator() &&
                !(event.player instanceof FakePlayer)) {
            onPlayerTick(event);
        }
    }

    private static final float MAGIC_STEP_HEIGHT = 1.0023f;

    private static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        EntityPlayer player = event.player;

        // Step Assist
        // for some reason, doing this in ticking and on unequip is not sufficient, this value is somewhat sticky
        if (!player.isSneaking()) {
            for (EntityEquipmentSlot slot : ArmorHelper.getArmorSlots()) {
                ItemStack armorStack = player.getItemStackFromSlot(slot);
                if (armorStack.getItem() instanceof IGTArmor gtArmor) {
                    NBTTagCompound behaviorTag = ArmorHelper.getBehaviorsTag(armorStack);
                    if (behaviorTag.getBoolean(ArmorHelper.STEP_ASSIST_KEY) && gtArmor.areBehaviorsActive(armorStack)) {
                        if (player.stepHeight < MAGIC_STEP_HEIGHT) {
                            player.stepHeight = MAGIC_STEP_HEIGHT;
                            return;
                        }
                    }
                }
            }
        }
        if (player.stepHeight == MAGIC_STEP_HEIGHT) {
            player.stepHeight = 0.6f;
        }
    }
}
