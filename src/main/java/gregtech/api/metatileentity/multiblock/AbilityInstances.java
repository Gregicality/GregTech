package gregtech.api.metatileentity.multiblock;

import gregtech.api.util.GTLog;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AbilityInstances extends AbstractList<Object> {

    public static final AbilityInstances EMPTY = new AbilityInstances(null) {

        @Override
        public boolean isKey(MultiblockAbility<?> key) {
            return false;
        }

        @Override
        public boolean add(Object o) {
            return false;
        }

        @Override
        public Object get(int index) {
            return null;
        }
    };
    private final MultiblockAbility<?> key;
    private final List<Object> instances = new ArrayList<>();

    public AbilityInstances(MultiblockAbility<?> key) {
        this.key = key;
    }

    @Override
    public Object get(int index) {
        return instances.get(index);
    }

    public boolean isKey(MultiblockAbility<?> key) {
        return this.key.equals(key);
    }

    public <R> @Nullable R getAndCast(int index, MultiblockAbility<R> key) {
        return key.checkAndCast(get(index));
    }

    @SuppressWarnings("unchecked")
    public <R> @NotNull List<R> cast() {
        return (List<R>) this;
    }

    @Override
    public boolean add(Object o) {
        if (o instanceof Collection<?>collection) {
            GTLog.logger.warn("Passed in a collection of elements to \"add()\"! Please use \"addAll()\" instead.",
                    new IllegalArgumentException());
            return addAll(collection);
        }
        int s = size();
        add(s, o);
        return s != size();
    }

    @Override
    public void add(int index, Object element) {
        if (key.checkType(element))
            instances.add(index, element);
    }

    @Override
    public Object set(int index, Object element) {
        if (!key.checkType(element)) return null;
        return instances.set(index, element);
    }

    @Override
    public Object remove(int index) {
        return instances.remove(index);
    }

    @Override
    public int size() {
        return instances.size();
    }
}
