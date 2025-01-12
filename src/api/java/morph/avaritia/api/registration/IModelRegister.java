package morph.avaritia.api.registration;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Implemented on an item for model registration, completely arbitrary.
 */
public interface IModelRegister {

    /**
     * Called when it is time to initialize models in preInit.
     */
    @SideOnly (Side.CLIENT)
    void registerModels();

}
