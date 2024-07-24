package gregtech.api.cover;

/**
 * Simple interface specifying that an attached energy detector(advanced or not) cover reads custom EU values from the
 * MTE
 */
public interface ICustomEnergyCover {

    /**
     * @return The total EU capacity
     */
    long getCoverCapacity();

    /**
     * @return The stored EU capacity
     */
    long getCoverStored();
}
