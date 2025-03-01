package gregtech.api.metatileentity.multiblock;

import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.ProgressWidget;
import org.jetbrains.annotations.NotNull;

public interface ProgressBarMultiblock {

    int getProgressBarCount();

    @NotNull
    ProgressWidget createProgressBar(PanelSyncManager panelSyncManager, int index);

    /**
     * @return the amount of columns in the progress bar grid
     */
    default int getProgressBarCols() {
        int count = getProgressBarCount();
        return switch (count) {
            case 0, 1, 2, 3 -> count;
            case 4 -> 2;
            case 5, 6 -> 3;
            case 7, 8 -> 4;
            default -> throw new UnsupportedOperationException("Cannot compute progress bar cols for count " + count);
        };
    }

    /**
     * @return the amount of rows in the progress bar grid
     */
    default int getProgressBarRows() {
        int count = getProgressBarCount();
        if (count <= 3) {
            return 1;
        }

        if (count <= 8) {
            return 2;
        }

        throw new UnsupportedOperationException("Cannot compute progress bar rows for count " + count);
    }
}
