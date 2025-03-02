package gregtech.api.metatileentity.interfaces;

/**
 * This Interface represents a MultiblockPart that should be refreshed before final recipe validation and input
 * consumption.
 */
public interface IRefreshBeforeConsumption {

    /**
     * Called Server Side Only.
     */
    void refreshBeforeConsumption();
}
