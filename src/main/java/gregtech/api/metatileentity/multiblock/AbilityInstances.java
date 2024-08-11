package gregtech.api.metatileentity.multiblock;

import org.jetbrains.annotations.Nullable;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

public class AbilityInstances extends AbstractList<Object> {

    private final MultiblockAbility<?> key;
    private final List<Object> instances = new ArrayList<>();

    public AbilityInstances(MultiblockAbility<?> key) {
        this.key = key;
    }

    @Override
    public Object get(int index) {
        return instances.get(index);
    }

    public <R> @Nullable R getAndCast(int index, MultiblockAbility<R> key) {
        return key.checkAndCast(get(index));
    }

    @Override
    public boolean add(Object o) {
        if (!key.checkType(o)) return false;
        return instances.add(o);
    }

    @Override
    public int size() {
        return instances.size();
    }
}
