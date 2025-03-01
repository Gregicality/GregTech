package gregtech.api.metatileentity.multiblock.ui;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IRichTextBuilder;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;

import java.util.function.Consumer;

@FunctionalInterface
public interface Operation {

    Int2ObjectMap<Operation> ID_MAP = new Int2ObjectArrayMap<>();
    Object2IntMap<Operation> REVERSE_MAP = new Object2IntArrayMap<>();

    Operation NO_OP = (drawable, richText) -> {};
    Operation NEW_LINE = (drawable, richText) -> richText.addLine(drawable);
    Operation ADD = (drawable, richText) -> richText.add(drawable);
    Operation NEW_LINE_SPACE = NEW_LINE.andThen(richText -> richText.spaceLine(2));

    static void init() {
        registerOp(NO_OP);
        registerOp(NEW_LINE);
        registerOp(ADD);
        registerOp(NEW_LINE_SPACE);
    }

    static Operation getById(int id) {
        return ID_MAP.get(id);
    }

    static int getId(Operation op) {
        return REVERSE_MAP.get(op);
    }

    static void registerOp(Operation op) {
        int nextId = ID_MAP.size();
        ID_MAP.put(nextId, op);
        REVERSE_MAP.put(op, nextId);
    }

    static void checkOp(Operation op) {
        if (!REVERSE_MAP.containsKey(op))
            throw new IllegalStateException("Operation is not registered!");
        Operation check = ID_MAP.get(REVERSE_MAP.getInt(op));
        if (check != op) throw new IllegalStateException("Operation is not identical!");
    }

    void apply(IDrawable drawable, IRichTextBuilder<?> richText);

    default Operation andThen(Operation after) {
        return (drawable, richText) -> {
            this.apply(drawable, richText);
            after.apply(drawable, richText);
        };
    }

    default Operation andThen(Consumer<IRichTextBuilder<?>> after) {
        return (drawable, richText) -> {
            this.apply(drawable, richText);
            after.accept(richText);
        };
    }
}
