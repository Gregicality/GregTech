package gregtech.api.metatileentity.multiblock;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public interface IMultiblockAbilityPart<T> extends IMultiblockPart {

    default @Nullable MultiblockAbility<T> getAbility() {
        return null;
    }

    default @NotNull List<MultiblockAbility<?>> getAbilities() {
        return getAbility() == null ? Collections.emptyList() : Collections.singletonList(getAbility());
    }

    void registerAbilities(@NotNull AbilityInstances abilityInstances);
}
