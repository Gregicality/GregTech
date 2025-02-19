package gregtech.api.metatileentity.multiblock.ui;

import com.cleanroommc.modularui.api.drawable.IDrawable;

public interface KeyManager {

    default void add(IDrawable drawable) {
        add(drawable, Operation.NEW_LINE_SPACE);
    }

    void add(IDrawable drawable, Operation op);

    default void addAll(Iterable<? extends IDrawable> drawables) {
        drawables.forEach(this::add);
    }
}
