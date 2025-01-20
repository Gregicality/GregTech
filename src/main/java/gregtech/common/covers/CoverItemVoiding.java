package gregtech.common.covers;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.cover.CoverDefinition;
import gregtech.api.cover.CoverableView;
import gregtech.client.renderer.texture.Textures;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.factory.GuiData;
import com.cleanroommc.modularui.factory.SidedPosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.ToggleButton;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.layout.Row;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CoverItemVoiding extends CoverConveyor {

    protected final NullItemHandler nullItemHandler = new NullItemHandler();

    public CoverItemVoiding(@NotNull CoverDefinition definition, @NotNull CoverableView coverableView,
                            @NotNull EnumFacing attachedSide) {
        super(definition, coverableView, attachedSide, 0, Integer.MAX_VALUE);
        this.isWorkingAllowed = false;
    }

    @Override
    public void update() {
        if (isWorkingAllowed && getCoverableView().getOffsetTimer() % 20 == 0) {
            doTransferItems();
        }
    }

    protected void doTransferItems() {
        IItemHandler myItemHandler = getCoverableView().getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,
                getAttachedSide());
        if (myItemHandler == null) {
            return;
        }
        voidAny(myItemHandler);
    }

    void voidAny(IItemHandler myItemHandler) {
        for (int srcIndex = 0; srcIndex < myItemHandler.getSlots(); srcIndex++) {
            ItemStack sourceStack = myItemHandler.extractItem(srcIndex, Integer.MAX_VALUE, true);
            if (sourceStack.isEmpty()) {
                continue;
            }
            if (!itemFilterContainer.test(sourceStack)) {
                continue;
            }
            myItemHandler.extractItem(srcIndex, Integer.MAX_VALUE, false);
        }
    }

    @Override
    public ModularPanel confgurePanel(ModularPanel panel, boolean isSmallGui) {
        return super.confgurePanel(panel, isSmallGui)
                .height(170);
    }

    @Override
    public @Nullable ParentWidget<?> createUI(ModularPanel mainPanel, PanelSyncManager guiSyncManager) {
        var isWorking = new BooleanSyncValue(this::isWorkingEnabled, this::setWorkingEnabled);

        return super.createUI(data, guiSyncManager)
                .child(Flow.row().height(18).widthRel(1f)
                        .marginBottom(2)
                        .child(new ToggleButton()
                                .value(isWorking)
                                .overlay(IKey.dynamic(() -> IKey.lang(this.isWorkingAllowed ?
                                        "behaviour.soft_hammer.enabled" :
                                        "behaviour.soft_hammer.disabled").get())
                                        .color(Color.WHITE.darker(1)))
                                .widthRel(0.6f)
                                .left(0)));
    }

    @Override
    protected boolean createThroughputRow() {
        return false;
    }

    @Override
    protected boolean createConveyorModeRow() {
        return false;
    }

    @Override
    protected boolean createDistributionModeRow() {
        return false;
    }

    @Override
    public void renderCover(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline,
                            Cuboid6 plateBox, BlockRenderLayer layer) {
        Textures.ITEM_VOIDING.renderSided(getAttachedSide(), plateBox, renderState, pipeline, translation);
    }

    @Override
    public @NotNull EnumActionResult onSoftMalletClick(@NotNull EntityPlayer playerIn, @NotNull EnumHand hand,
                                                       @NotNull CuboidRayTraceResult hitResult) {
        this.isWorkingAllowed = !this.isWorkingAllowed;
        if (!playerIn.world.isRemote) {
            playerIn.sendStatusMessage(new TextComponentTranslation(isWorkingEnabled() ?
                    "cover.voiding.message.enabled" : "cover.voiding.message.disabled"), true);
        }
        return EnumActionResult.SUCCESS;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, T defaultValue) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(nullItemHandler);
        }
        if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE) {
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        }
        return defaultValue;
    }

    class NullItemHandler implements IItemHandler {

        @Override
        public int getSlots() {
            return 9;
        }

        @NotNull
        @Override
        public ItemStack getStackInSlot(int slot) {
            return ItemStack.EMPTY;
        }

        @NotNull
        @Override
        public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (!itemFilterContainer.test(stack)) {
                return stack;
            }
            return ItemStack.EMPTY;
        }

        @NotNull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }
    }
}
