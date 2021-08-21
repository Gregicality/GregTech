package gregtech.api.capability;

import gregtech.api.GTValues;
import gregtech.api.capability.impl.EUToFEProvider;
import gregtech.api.capability.tool.ICutterItem;
import gregtech.api.capability.tool.IScrewdriverItem;
import gregtech.api.capability.tool.ISoftHammerItem;
import gregtech.api.capability.tool.IWrenchItem;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = GTValues.MODID)
public class GregtechCapabilities {

    @CapabilityInject(IEnergyContainer.class)
    public static Capability<IEnergyContainer> CAPABILITY_ENERGY_CONTAINER = null;

    @CapabilityInject(IElectricItem.class)
    public static Capability<IElectricItem> CAPABILITY_ELECTRIC_ITEM = null;

    @CapabilityInject(IWrenchItem.class)
    public static Capability<IWrenchItem> CAPABILITY_WRENCH = null;

    @CapabilityInject(ICutterItem.class)
    public static Capability<ICutterItem> CAPABILITY_CUTTER = null;

    @CapabilityInject(IScrewdriverItem.class)
    public static Capability<IScrewdriverItem> CAPABILITY_SCREWDRIVER = null;

    @CapabilityInject(ISoftHammerItem.class)
    public static Capability<ISoftHammerItem> CAPABILITY_MALLET = null;

    @CapabilityInject(IFuelable.class)
    public static Capability<IFuelable> CAPABILITY_FUELABLE = null;

    @CapabilityInject(IMultiblockController.class)
    public static Capability<IMultiblockController> CAPABILITY_MULTIBLOCK_CONTROLLER = null;

    private static final ResourceLocation CAPABILITY_EU_TO_FE = new ResourceLocation(GTValues.MODID, "fe_capability");

    @SubscribeEvent
    public static void attachTileCapability(AttachCapabilitiesEvent<TileEntity> event) {
        event.addCapability(CAPABILITY_EU_TO_FE, new EUToFEProvider(event.getObject()));
    }
}
