package gregtech.api.capability;

import org.jetbrains.annotations.Nullable;

import java.util.Comparator;

/**
 * Interface for fluid containers ({@link net.minecraftforge.fluids.IFluidTank IFluidTank} or
 * {@link net.minecraftforge.fluids.capability.IFluidHandler IFluidHandler}) associated with {@link IFilter}.
 */
public interface IFilteredHandler<T> {

    /**
     * Compare logic for filtered instances.
     */
    Comparator<IFilteredHandler<?>> COMPARATOR = Comparator.nullsLast(
            Comparator.comparing(IFilteredHandler::getFilter, IFilter.FILTER_COMPARATOR));

    /**
     * @return instance of {@link IFilter} associated to this object, or {@code null} if there's no filter
     *         associated.
     */
    @Nullable
    IFilter<T> getFilter();
}
