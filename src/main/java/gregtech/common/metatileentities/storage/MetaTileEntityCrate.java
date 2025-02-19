package gregtech.common.metatileentities.storage;

import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.items.toolitem.ToolClasses;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.mui.GTGuis;
import gregtech.api.network.AdvancedPacketBuffer;
import gregtech.api.recipes.ModHandler;
import gregtech.api.unification.material.Material;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.items.MetaItems;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemStackHandler;

import codechicken.lib.colour.ColourRGBA;
import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.widgets.ItemSlot;
import com.cleanroommc.modularui.widgets.layout.Grid;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static gregtech.api.capability.GregtechDataCodes.IS_TAPED;

public class MetaTileEntityCrate extends MetaTileEntity {

    private final Material material;
    private final int inventorySize;
    private final int rowSize;
    protected ItemStackHandler inventory;
    private boolean isTaped;
    private final String TAPED_NBT = "Taped";

    public MetaTileEntityCrate(ResourceLocation metaTileEntityId, Material material, int inventorySize, int rowSize) {
        super(metaTileEntityId);
        this.material = material;
        this.inventorySize = inventorySize;
        this.rowSize = rowSize;
        initializeInventory();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityCrate(metaTileEntityId, material, inventorySize, rowSize);
    }

    @Override
    public boolean hasFrontFacing() {
        return false;
    }

    @Override
    public String getHarvestTool() {
        return ModHandler.isMaterialWood(material) ? ToolClasses.AXE : ToolClasses.WRENCH;
    }

    @Override
    protected void initializeInventory() {
        super.initializeInventory();
        this.inventory = new GTItemStackHandler(this, inventorySize);
        this.itemInventory = inventory;
    }

    @Override
    public void clearMachineInventory(@NotNull List<@NotNull ItemStack> itemBuffer) {
        if (!isTaped) {
            clearInventory(itemBuffer, inventory);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        if (ModHandler.isMaterialWood(material)) {
            return Pair.of(Textures.WOODEN_CRATE.getParticleTexture(), getPaintingColorForRendering());
        } else {
            int color = ColourRGBA.multiply(
                    GTUtility.convertRGBtoOpaqueRGBA_CL(material.getMaterialRGB()),
                    GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering()));
            color = GTUtility.convertOpaqueRGBA_CLtoRGB(color);
            return Pair.of(Textures.METAL_CRATE.getParticleTexture(), color);
        }
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        if (material.toString().contains("wood")) {
            Textures.WOODEN_CRATE.render(renderState, translation,
                    GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering()), pipeline);
        } else {
            int baseColor = ColourRGBA.multiply(GTUtility.convertRGBtoOpaqueRGBA_CL(material.getMaterialRGB()),
                    GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering()));
            Textures.METAL_CRATE.render(renderState, translation, baseColor, pipeline);
        }
        boolean taped = isTaped;
        if (renderContextStack != null && renderContextStack.getTagCompound() != null) {
            NBTTagCompound tag = renderContextStack.getTagCompound();
            if (tag.hasKey(TAPED_NBT) && tag.getBoolean(TAPED_NBT)) {
                taped = true;
            }
        }
        if (taped) {
            Textures.TAPED_OVERLAY.render(renderState, translation, pipeline);
        }
    }

    @Override
    public int getDefaultPaintingColor() {
        return 0xFFFFFF;
    }

    @Override
    public boolean usesMui2() {
        return true;
    }

    @Override
    public ModularPanel buildUI(PosGuiData guiData, PanelSyncManager guiSyncManager) {
        guiSyncManager.registerSlotGroup("item_inv", rowSize);

        int rows = inventorySize / rowSize;
        List<List<IWidget>> widgets = new ArrayList<>();
        for (int i = 0; i < rows; i++) {
            widgets.add(new ArrayList<>());
            for (int j = 0; j < this.rowSize; j++) {
                int index = i * rowSize + j;
                widgets.get(i).add(new ItemSlot().slot(SyncHandlers.itemSlot(inventory, index)
                        .slotGroup("item_inv")
                        .changeListener((newItem, onlyAmountChanged, client, init) -> {
                            if (client || init) return;

                            for (var facing : EnumFacing.VALUES) {
                                var neighbor = getNeighbor(facing);
                                if (neighbor instanceof IGregTechTileEntity gtte) {
                                    gtte.getMetaTileEntity().onNeighborChanged();
                                }
                            }
                        })));
            }
        }
        return GTGuis.createPanel(this, rowSize * 18 + 14, 18 + 4 * 18 + 5 + 14 + 18 * rows)
                .child(IKey.lang(getMetaFullName()).asWidget().pos(5, 5))
                .bindPlayerInventory()
                .child(new Grid()
                        .top(18).left(7).right(7).height(rows * 18)
                        .minElementMargin(0, 0)
                        .minColWidth(18).minRowHeight(18)
                        .matrix(widgets));
    }

    @Override
    public boolean onRightClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing,
                                CuboidRayTraceResult hitResult) {
        ItemStack stack = playerIn.getHeldItem(hand);
        if (playerIn.isSneaking() && !isTaped) {
            if (stack.isItemEqual(MetaItems.DUCT_TAPE.getStackForm()) ||
                    stack.isItemEqual(MetaItems.BASIC_TAPE.getStackForm())) {
                if (!playerIn.isCreative()) {
                    stack.shrink(1);
                }
                isTaped = true;
                if (!getWorld().isRemote) {
                    writeCustomData(IS_TAPED, buf -> buf.writeBoolean(isTaped));
                    markDirty();
                }
                return true;
            }
        }
        return super.onRightClick(playerIn, hand, facing, hitResult);
    }

    @Override
    public int getItemStackLimit(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag != null && tag.getBoolean(TAPED_NBT)) {
            return 1;
        }
        return super.getItemStackLimit(stack);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setTag("Inventory", inventory.serializeNBT());
        data.setBoolean(TAPED_NBT, isTaped);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.inventory.deserializeNBT(data.getCompoundTag("Inventory"));
        if (data.hasKey(TAPED_NBT)) {
            this.isTaped = data.getBoolean(TAPED_NBT);
        }
    }

    @Override
    public void initFromItemStackData(NBTTagCompound data) {
        super.initFromItemStackData(data);
        if (data.hasKey(TAG_KEY_PAINTING_COLOR)) {
            this.setPaintingColor(data.getInteger(TAG_KEY_PAINTING_COLOR));
        }
        this.isTaped = data.getBoolean(TAPED_NBT);
        if (isTaped) {
            this.inventory.deserializeNBT(data.getCompoundTag("Inventory"));
        }

        data.removeTag(TAPED_NBT);
        data.removeTag(TAG_KEY_PAINTING_COLOR);

        this.isTaped = false;
    }

    @Override
    public void writeItemStackData(NBTTagCompound data) {
        super.writeItemStackData(data);

        // Account for painting color when breaking the crate
        if (this.isPainted()) {
            data.setInteger(TAG_KEY_PAINTING_COLOR, this.getPaintingColor());
        }
        // Don't write tape NBT if not taped, to stack with ones from JEI
        if (isTaped) {
            data.setBoolean(TAPED_NBT, isTaped);
            data.setTag("Inventory", inventory.serializeNBT());
        }
    }

    @Override
    public void receiveCustomData(int dataId, @NotNull AdvancedPacketBuffer buf) {
        super.receiveCustomData(dataId, buf);

        if (dataId == IS_TAPED) {
            this.isTaped = buf.readBoolean();
            scheduleRenderUpdate();
            markDirty();
        }
    }

    @Override
    protected boolean shouldSerializeInventories() {
        return false;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.universal.tooltip.item_storage_capacity", inventorySize));
        tooltip.add(I18n.format("gregtech.crate.tooltip.taped_movement"));
    }

    @Override
    public void addToolUsages(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.tool_action.screwdriver.access_covers"));
        super.addToolUsages(stack, world, tooltip, advanced);
    }
}
