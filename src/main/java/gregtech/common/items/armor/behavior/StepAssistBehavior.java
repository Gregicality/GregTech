package gregtech.common.items.armor.behavior;

import gregtech.api.items.armoritem.ArmorHelper;
import gregtech.api.items.armoritem.IArmorBehavior;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class StepAssistBehavior implements IArmorBehavior {

    public static final StepAssistBehavior INSTANCE = new StepAssistBehavior();

    protected StepAssistBehavior() {/**/}

    @Override
    public void addBehaviorNBT(@NotNull ItemStack stack, @NotNull NBTTagCompound tag) {
        tag.setBoolean(ArmorHelper.STEP_ASSIST_KEY, true);
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable World world, @NotNull List<String> tooltip) {
        tooltip.add(I18n.format("metaarmor.tooltip.stepassist"));
    }
}
