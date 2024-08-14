package gregtech.api.metatileentity.multiblock;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

public class AbilityInstances extends AbstractList<Object> {

    public static final AbilityInstances EMPTY = new AbilityInstances(null) {

        @Override
        public boolean isKey(MultiblockAbility<?> key) {
            return false;
        }

        @Override
        public void add(int index, Object element) {
            // do nothing
        }

        @Override
        public Object set(int index, Object element) {
            return null;
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
        int s = size();
        // if what's added isn't what the key expects,
        // and it's an iterable, try to add all of its elements instead
        if (!this.key.checkType(o) && o instanceof Iterable<?>iterable) {
            for (var e : iterable)
                add(size(), e);
            return s != size();
        }
        // otherwise add as normal
        add(s, o);
        return s != size();
    }

    @Override
    public void add(int index, Object element) {
        if (!key.checkType(element))
            throw new IllegalArgumentException(
                    String.format("element's class \"%s\" is not of type \"%s\"", element.getClass(), this.key.getType()));
        instances.add(index, element);
    }

    @Override
    public Object set(int index, Object element) {
        if (!key.checkType(element))
            throw new IllegalArgumentException(
                    String.format("element's class \"%s\" is not of type \"%s\"", element.getClass(), this.key.getType()));
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
