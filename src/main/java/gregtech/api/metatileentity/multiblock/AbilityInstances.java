package gregtech.api.metatileentity.multiblock;

import gregtech.api.util.GTLog;

import it.unimi.dsi.fastutil.objects.AbstractObjectList;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Stores a list of {@link MultiblockAbility} instances.
 * <br />
 * <br />
 * Make sure to use {@link AbilityInstances#isKey(MultiblockAbility)} to check what kind of
 * instances to add to this list.
 */
public class AbilityInstances extends AbstractObjectList<Object> {

    public static final AbilityInstances EMPTY = new AbilityInstances(null) {

        @Override
        public boolean isKey(MultiblockAbility<?> key) {
            return false;
        }

        @Override
        protected boolean canAdd(Object o) {
            return false;
        }

        @Override
        public @NotNull <R> List<R> cast() {
            return Collections.emptyList();
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

    public <R> @NotNull List<R> cast() {
        return this.key.castList(this);
    }

    /**
     * Adds an instance of a MultiblockAbility to this list. If the object
     * passed in is a list of instances that match the key, they will all be added.
     * if the object is not what the key expects, an exception will be thrown.
     * 
     * @param o element whose class matches the type from the key
     * @return true if the list was modified
     */
    @Override
    public boolean add(Object o) {
        int s = size();
        // if what's added isn't what the key expects,
        // and it's an iterable, try to add all of its elements instead
        if (!canAdd(o) && o instanceof Iterable<?>iterable) {
            for (var e : iterable)
                add(size(), e);
        } else {
            // otherwise add as normal
            add(s, o);
        }
        return s != size();
    }

    protected boolean canAdd(Object o) {
        return this.key.checkType(o);
    }

    @Override
    public void add(int index, Object element) {
        if (canAdd(element) && !instances.contains(element))
            instances.add(index, element);
    }

    /**
     * Replaces the element at the index. Will throw an exception if the element is not what the key expects.
     * 
     * @param index   index of the element to replace
     * @param element element to be stored at the specified position
     * @return the element previously stored at the index
     */
    @Override
    public Object set(int index, Object element) {
        if (!canAdd(element)) return null;

        int existing = instances.indexOf(element);
        if (existing != -1) {
            GTLog.logger.warn("attempted to set \"{}\" at index {} when we already have it at {}",
                    element.getClass().getSimpleName(), index, existing);
            return null;
        }

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
