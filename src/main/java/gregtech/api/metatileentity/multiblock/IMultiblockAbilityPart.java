package gregtech.api.metatileentity.multiblock;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public interface IMultiblockAbilityPart<T> extends IMultiblockPart {

    MultiblockAbility<T> getAbility();

    default @NotNull List<MultiblockAbility<?>> getAbilities() {
        return Collections.singletonList(getAbility());
    }

    @NotNull
    List<? extends T> registerAbilities(@NotNull MultiblockAbility<? extends T> key);
}
