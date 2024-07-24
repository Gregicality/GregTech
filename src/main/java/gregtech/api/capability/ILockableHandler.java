package gregtech.api.capability;

import org.jetbrains.annotations.Nullable;

public interface ILockableHandler<T> {

    void setLock(boolean isLocked);

    boolean isLocked();

    @Nullable
    T getLockedObject();
}
