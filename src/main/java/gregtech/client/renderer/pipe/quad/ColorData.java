package gregtech.client.renderer.pipe.quad;

import gregtech.api.graphnet.pipenet.physical.tile.PipeTileEntity;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.github.bsideup.jabel.Desugar;

import java.util.Arrays;

@Desugar
@SideOnly(Side.CLIENT)
public record ColorData(int... colorsARGB) {

    public static final ColorData PLAIN = new ColorData(PipeTileEntity.DEFAULT_COLOR);

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ColorData) obj;
        return Arrays.equals(this.colorsARGB, that.colorsARGB);
    }
}
