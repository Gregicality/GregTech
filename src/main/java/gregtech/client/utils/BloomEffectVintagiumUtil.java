package gregtech.client.utils;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import me.jellysquid.mods.sodium.client.render.chunk.passes.BlockRenderPass;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@SideOnly(Side.CLIENT)
public class BloomEffectVintagiumUtil {

    /**
     * @return {@link BlockRenderPass} instance for the bloom render layer.
     */
    @NotNull
    @SuppressWarnings("unused")
    public static BlockRenderPass getBloomPass() {
        return Objects.requireNonNull(BlockRenderPass.valueOf("BLOOM"), "Bloom effect is not initialized yet");
    }

    public static void init() {}
}
