package gregtech.api.render.scene;

import net.minecraft.util.BlockRenderLayer;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: KilaBash
 * @Date: 2021/08/25
 * @Description: Scene Render State hooks.
 * This is where you decide whether or not this group of pos should be rendered. What other requirements do you have for rendering.
 */
public interface ISceneRenderHook {
    /***
     *
     * @return should blocks be rendered.
     */
    boolean apply(boolean isTESR, int pass, BlockRenderLayer layer);
}
