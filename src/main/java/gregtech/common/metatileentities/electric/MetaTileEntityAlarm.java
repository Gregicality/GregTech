package gregtech.common.metatileentities.electric;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.gui.widgets.TextFieldWidget2;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.network.AdvancedPacketBuffer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.ConfigHolder;
import gregtech.common.gui.widget.terminal.gui.widgets.SelectorWidget;
import gregtech.core.sound.GTSoundEvents;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MetaTileEntityAlarm extends TieredMetaTileEntity {

    private SoundEvent selectedSound;
    private boolean isActive;
    private int radius = 64;
    public static final int BASE_EU_CONSUMPTION = 4;

    public MetaTileEntityAlarm(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, 1);
        selectedSound = GTSoundEvents.DEFAULT_ALARM;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityAlarm(metaTileEntityId);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.universal.tooltip.uses_per_tick", BASE_EU_CONSUMPTION));
        tooltip.add(
                I18n.format("gregtech.universal.tooltip.energy_storage_capacity", energyContainer.getEnergyCapacity()));
    }

    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (this.isActive) {
            Textures.ALARM_OVERLAY_ACTIVE.renderSided(getFrontFacing(), renderState, translation, pipeline);
        } else {
            Textures.ALARM_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
        }
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return ModularUI.builder(GuiTextures.BACKGROUND, 240, 86)
                .widget(new LabelWidget(10, 5, getMetaFullName()))
                .widget(new SelectorWidget(10, 20, 220, 20,
                        getSounds().stream().map(this::getNameOfSound)
                                .collect(Collectors.toList()),
                        0x555555, () -> getNameOfSound(this.selectedSound), true).setOnChanged((v) -> {
                            if (this.getWorld().isRemote)
                                GregTechAPI.soundManager.stopTileSound(getPos());
                            SoundEvent newSound = SoundEvent.REGISTRY.getObject(new ResourceLocation(v));
                            if (this.selectedSound != newSound) {
                                this.selectedSound = SoundEvent.REGISTRY.getObject(new ResourceLocation(v));
                                this.writeCustomData(GregtechDataCodes.UPDATE_SOUND,
                                        (writer) -> writer
                                                .writeResourceLocation(getResourceLocationOfSound(this.selectedSound)));
                            }
                        }))
                .widget(new ImageWidget(10, 54, 220, 20, GuiTextures.DISPLAY))
                .label(10, 44, "gregtech.gui.alarm.radius")
                .widget(new TextFieldWidget2(12, 60, 216, 16, () -> String.valueOf(radius), value -> {
                    if (!value.isEmpty()) {
                        int newRadius = Integer.parseInt(value);
                        if (newRadius != radius) {
                            this.writeCustomData(GregtechDataCodes.UPDATE_RADIUS,
                                    (writer) -> writer.writeInt(newRadius));
                            radius = newRadius;
                        }
                    }
                }).setMaxLength(10).setNumbersOnly(0, 128))
                .build(this.getHolder(), entityPlayer);
    }

    protected List<SoundEvent> getSounds() {
        if (GTValues.FOOLS.get() && ConfigHolder.misc.specialEvents) {
            return Arrays.asList(GTSoundEvents.DEFAULT_ALARM, GTSoundEvents.ARC, SoundEvents.ENTITY_WOLF_HOWL,
                    SoundEvents.ENTITY_ENDERMEN_DEATH, GTSoundEvents.SUS_RECORD);
        }
        return Arrays.asList(GTSoundEvents.DEFAULT_ALARM, GTSoundEvents.ARC, SoundEvents.ENTITY_WOLF_HOWL,
                SoundEvents.ENTITY_ENDERMEN_DEATH);
    }

    @Override
    public SoundEvent getSound() {
        return selectedSound;
    }

    @Override
    public boolean isActive() {
        if (this.getWorld().isRemote) {
            return isActive;
        }
        return this.isBlockRedstonePowered() &&
                this.energyContainer.changeEnergy(-BASE_EU_CONSUMPTION) == -BASE_EU_CONSUMPTION;
    }

    @Override
    public void update() {
        super.update();
        if (!this.getWorld().isRemote) {
            if (this.isActive != this.isActive()) {
                this.writeCustomData(GregtechDataCodes.UPDATE_ACTIVE, (writer) -> writer.writeBoolean(this.isActive()));
                this.isActive = this.isActive();
            }
        }
    }

    @Override
    public void receiveCustomData(int dataId, @NotNull AdvancedPacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.UPDATE_ACTIVE) {
            this.isActive = buf.readBoolean();
            this.scheduleRenderUpdate();
        } else if (dataId == GregtechDataCodes.UPDATE_SOUND) {
            this.selectedSound = SoundEvent.REGISTRY.getObject(buf.readResourceLocation());
            GregTechAPI.soundManager.stopTileSound(getPos());
        } else if (dataId == GregtechDataCodes.UPDATE_RADIUS) {
            this.radius = buf.readInt();
            GregTechAPI.soundManager.stopTileSound(getPos());
        }
    }

    @Override
    public float getVolume() {
        return radius / 16f;
    }

    @Override
    public void receiveInitialSyncData(@NotNull AdvancedPacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.isActive = buf.readBoolean();
        this.selectedSound = SoundEvent.REGISTRY.getObject(buf.readResourceLocation());
        this.radius = buf.readInt();
    }

    @Override
    public void writeInitialSyncData(@NotNull AdvancedPacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(this.isActive);
        buf.writeResourceLocation(getResourceLocationOfSound(this.selectedSound));
        buf.writeInt(this.radius);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        data.setBoolean("isActive", this.isActive);
        data.setString("selectedSound", getNameOfSound(this.selectedSound));
        data.setInteger("radius", this.radius);
        return super.writeToNBT(data);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        this.isActive = data.getBoolean("isActive");
        this.selectedSound = SoundEvent.REGISTRY.getObject(new ResourceLocation(data.getString("selectedSound")));
        this.radius = data.getInteger("radius");
        super.readFromNBT(data);
    }

    public String getNameOfSound(SoundEvent sound) {
        return getResourceLocationOfSound(sound).toString();
    }

    public ResourceLocation getResourceLocationOfSound(SoundEvent sound) {
        return SoundEvent.REGISTRY.getNameForObject(sound);
    }
}
