package gregtech.api.metatileentity.multiblock;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public interface IMultiblockAbilityPart<T> extends IMultiblockPart {

    /**
     * Returns only one ability for this multiblock part.
     * If you need more than one, override {@link #getAbilities()} instead.
     * 
     * @return The MultiblockAbility this part has
     */
    default @Nullable MultiblockAbility<T> getAbility() {
        return null;
    }

    /**
     * Returns a list of abilities that this multiblock part may have.
     * 
     * @return a list of MultiblockAbilities
     */
    default @NotNull List<MultiblockAbility<?>> getAbilities() {
        return getAbility() == null ? Collections.emptyList() : Collections.singletonList(getAbility());
    }

    void registerAbilities(@NotNull AbilityInstances abilityInstances);
}
