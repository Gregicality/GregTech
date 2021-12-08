package gregtech.common.metatileentities.converter;

import gregtech.api.GTValues;
import gregtech.api.capability.FeCompat;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.metatileentity.MTETrait;
import gregtech.api.util.GTUtility;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.BitSet;

public class ConverterTrait extends MTETrait {

    private final BitSet batterySlotsUsedThisTick = new BitSet();

    private final int baseAmps;
    private final int tier;
    private final long voltage;
    private boolean feToEu;

    private final IEnergyStorage energyFE;
    private final IEnergyContainer energyEU;
    private long storedEU;

    private final long baseCapacity;

    private long usedAmps = 0;

    public ConverterTrait(MetaTileEntityConverter mte, int tier, int baseAmps, boolean feToEu) {
        super(mte);
        this.baseAmps = baseAmps;
        this.feToEu = feToEu;
        this.tier = tier;
        this.voltage = GTValues.V[tier];
        this.baseCapacity = this.voltage * 8 * baseAmps;
        this.energyFE = new FEContainer();
        this.energyEU = new EUContainer();
        this.storedEU = 0;
    }

    protected IEnergyContainer getEnergyEUContainer() {
        return energyEU;
    }

    protected IEnergyStorage getEnergyFEContainer() {
        return energyFE;
    }

    public boolean isFeToEu() {
        return feToEu;
    }

    protected void setFeToEu(boolean feToEu) {
        this.feToEu = feToEu;
    }

    public int getBaseAmps() {
        return baseAmps;
    }

    public long getVoltage() {
        return voltage;
    }

    @Override
    public String getName() {
        return "EnergyConvertTrait";
    }

    @Override
    public int getNetworkID() {
        return 1;
    }

    @Override
    public <T> T getCapability(Capability<T> capability) {
        return null;
    }

    public int toFe(long eu){
        return FeCompat.converterToFe(eu, feToEu);
    }

    public long toEu(long fe){
        return FeCompat.converterToEu(fe, feToEu);
    }

    private long addEnergyInternal(long amount, boolean simulate) {
        if (amount <= 0)
            return 0;
        long original = amount;

        // add energy first to internal buffer
        long change = Math.min(baseCapacity - storedEU, amount);
        if (!simulate)
            storedEU += change;
        amount -= change;

        // then to batteries
        IItemHandlerModifiable inventory = getInventory();
        for (int i = 0; i < inventory.getSlots(); i++) {
            if (amount == 0) break;
            ItemStack batteryStack = inventory.getStackInSlot(i);
            IElectricItem electricItem = getBatteryContainer(batteryStack);
            if (electricItem == null) continue;
            long ins = electricItem.charge(amount, tier, false, simulate);
            amount -= ins;
        }

        return original - amount;
    }

    private long removeEnergyInternal(long amount, boolean simulate) {
        if (amount <= 0)
            return 0;
        long original = amount;

        // remove energy first from batteries
        IItemHandlerModifiable inventory = getInventory();
        for (int i = 0; i < inventory.getSlots(); i++) {
            if (amount == 0) break;
            ItemStack batteryStack = inventory.getStackInSlot(i);
            IElectricItem electricItem = getBatteryContainer(batteryStack);
            if (electricItem == null) continue;
            long ins = electricItem.discharge(amount, tier, false, false, simulate);
            amount -= ins;
        }

        // then from internal buffer
        long change = Math.min(storedEU, amount);
        if (!simulate)
            storedEU -= change;
        amount -= change;

        return  original - amount;
    }

    private EnumFacing getFront() {
        return metaTileEntity.getFrontFacing();
    }

    private long getEnergyStoredInternal() {
        long energyStored = 0L;
        IItemHandlerModifiable inventory = getInventory();
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack batteryStack = inventory.getStackInSlot(i);
            IElectricItem electricItem = getBatteryContainer(batteryStack);
            if (electricItem == null) continue;
            energyStored += electricItem.getCharge();
        }
        return energyStored + storedEU;
    }

    protected IElectricItem getBatteryContainer(ItemStack itemStack) {
        IElectricItem electricItem = itemStack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (electricItem != null && electricItem.canProvideChargeExternally())
            return electricItem;
        return null;
    }

    protected IItemHandlerModifiable getInventory() {
        return metaTileEntity.getImportItems();
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setLong("StoredEU", storedEU);
        nbt.setBoolean("feToEu", feToEu);
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        storedEU = nbt.getLong("StoredEU");
        feToEu = nbt.getBoolean("feToEu");
    }

    @Override
    public void update() {
        super.update();
        usedAmps = 0;
        this.batterySlotsUsedThisTick.clear();
        if (metaTileEntity.getWorld().isRemote) return;
        TileEntity tile = metaTileEntity.getWorld().getTileEntity(metaTileEntity.getPos().offset(getFront()));
        if (tile == null) return;
        EnumFacing opposite = getFront().getOpposite();
        // check how much can be extracted
        long extractable = removeEnergyInternal(Long.MAX_VALUE, true);
        if (extractable == 0)
            return;
        long ampsToInsert = Math.min(energyEU.getInputAmperage(), extractable / voltage);
        if (ampsToInsert * voltage > extractable) {
            ampsToInsert = extractable / voltage;
        }
        if (feToEu) {
            IEnergyContainer container = tile.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, opposite);
            if (container != null) {
                // if the energy is not enough for a full package, make it smaller (for batteries below the converters tier)
                long volt = voltage;
                if (ampsToInsert == 0) {
                    ampsToInsert = 1;
                    volt = extractable;
                }

                long ampsUsed = container.acceptEnergyFromNetwork(opposite, volt, ampsToInsert);
                removeEnergyInternal(volt * ampsUsed, false);
            }
        } else {
            IEnergyStorage storage = tile.getCapability(CapabilityEnergy.ENERGY, opposite);
            if (storage != null) {
                // if the energy is not enough for a full package, make it smaller (for batteries below the converters tier)
                long volt = voltage;
                if (ampsToInsert == 0) {
                    ampsToInsert = 1;
                    volt = extractable;
                }

                int inserted = storage.receiveEnergy(toFe(Math.min(extractable, volt * ampsToInsert)), false);
                removeEnergyInternal(toEu(inserted), false);
            }
        }
    }
    // -- GTCEu Energy--------------------------------------------

    public class EUContainer implements IEnergyContainer {

        @Override
        public long acceptEnergyFromNetwork(EnumFacing side, long voltage, long amperage) {
            long inputAmps = getInputAmperage();
            if (feToEu || usedAmps >= inputAmps) return 0;
            long ampsUsed = 0;
            if (amperage <= 0 || voltage <= 0) {
                return 0;
            }
            if (side == null || inputsEnergy(side)) {
                if (voltage > getInputVoltage()) {
                    GTUtility.doOvervoltageExplosion(metaTileEntity, voltage);
                    return Math.min(amperage, inputAmps - usedAmps);
                }

                // first add to internal buffers
                long maxAmps = Math.min(inputAmps - usedAmps, amperage);
                long space = baseCapacity - storedEU;
                if (space > voltage) {
                    maxAmps = Math.min(maxAmps, space / voltage);
                    long energyToAdd = voltage * maxAmps;
                    storedEU += energyToAdd;
                    ampsUsed += maxAmps;
                    if (usedAmps + ampsUsed >= inputAmps) {
                        usedAmps += ampsUsed;
                        return ampsUsed;
                    }
                }

                // then to batteries
                IItemHandlerModifiable inventory = getInventory();
                for (int i = 0; i < inventory.getSlots(); i++) {
                    if (batterySlotsUsedThisTick.get(i)) continue;
                    ItemStack batteryStack = inventory.getStackInSlot(i);
                    IElectricItem electricItem = getBatteryContainer(batteryStack);
                    if (electricItem == null) continue;
                    if (electricItem.charge(voltage, tier, false, true) == voltage) {
                        electricItem.charge(voltage, tier, false, false);
                        inventory.setStackInSlot(i, batteryStack);
                        batterySlotsUsedThisTick.set(i);
                        if (++ampsUsed == maxAmps) break;
                    }
                }
            }
            usedAmps += ampsUsed;
            return ampsUsed;
        }

        @Override
        public boolean inputsEnergy(EnumFacing side) {
            return !feToEu && side != getFront();
        }

        @Override
        public long changeEnergy(long amount) {
            if (amount == 0)
                return 0;
            return amount > 0 ? addEnergyInternal(amount, false) : removeEnergyInternal(-amount, false);
        }

        @Override
        public long addEnergy(long energyToAdd) {
            return addEnergyInternal(energyToAdd, false);
        }

        @Override
        public long removeEnergy(long energyToRemove) {
            return removeEnergyInternal(energyToRemove, false);
        }

        @Override
        public long getEnergyStored() {
            return getEnergyStoredInternal();
        }

        @Override
        public long getEnergyCapacity() {
            long energyCapacity = 0L;
            IItemHandlerModifiable inventory = getInventory();
            for (int i = 0; i < inventory.getSlots(); i++) {
                ItemStack batteryStack = inventory.getStackInSlot(i);
                IElectricItem electricItem = getBatteryContainer(batteryStack);
                if (electricItem == null) continue;
                energyCapacity += electricItem.getMaxCharge();
            }
            return energyCapacity + baseCapacity;
        }

        @Override
        public long getInputAmperage() {
            long inputAmperage = 0L;
            IItemHandlerModifiable inventory = getInventory();
            for (int i = 0; i < inventory.getSlots(); i++) {
                ItemStack batteryStack = inventory.getStackInSlot(i);
                IElectricItem electricItem = getBatteryContainer(batteryStack);
                if (electricItem == null) continue;
                inputAmperage++;
            }
            return inputAmperage > 0 ? inputAmperage : baseAmps;
        }

        @Override
        public long getInputVoltage() {
            return voltage;
        }

        @Override
        public long getOutputAmperage() {
            return feToEu ? getInputAmperage() : 0;
        }

        @Override
        public long getOutputVoltage() {
            return voltage;
        }
    }

    // -- Forge Energy--------------------------------------------

    public class FEContainer implements IEnergyStorage {

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            if (!feToEu) return 0;
            long amount = Math.min(voltage * energyEU.getInputAmperage(), toEu(maxReceive));
            return toFe(addEnergyInternal(amount, simulate));
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            long amount = Math.min(voltage * energyEU.getInputAmperage(), toEu(maxExtract));
            return toFe(removeEnergyInternal(amount, simulate));
        }

        @Override
        public int getEnergyStored() {
            return toFe(getEnergyStoredInternal());
        }

        @Override
        public int getMaxEnergyStored() {
            return toFe(energyEU.getEnergyCapacity());
        }

        @Override
        public boolean canExtract() {
            return false;
        }

        @Override
        public boolean canReceive() {
            return feToEu;
        }
    }
}
