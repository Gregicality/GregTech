package gregtech.api.mui.serialize;

import gregtech.api.mui.drawables.HoverableKey;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;

import java.util.Arrays;

public class DrawableSerializer implements JsonHandler<IDrawable> {

    KeySerializer keySerializer = new KeySerializer();

    @Override
    public IDrawable deserialize(JsonElement json, JsonDeserializationContext context)
                                                                                       throws JsonParseException {
        if (!json.isJsonObject()) return IDrawable.EMPTY;
        JsonObject parsed = json.getAsJsonObject();
        if (parsed.has("key") && parsed.has("tooltip")) {
            IKey key = context.deserialize(parsed.get("key"), IKey.class);

            IDrawable[] list = deserializeArray(parsed.getAsJsonArray("tooltip"), context, IDrawable[]::new);
            return HoverableKey.of(key).addLines(Arrays.asList(list));
        } else {
            return keySerializer.deserialize(json, context);
        }
    }

    @Override
    public JsonElement serialize(IDrawable src, JsonSerializationContext context) {
        JsonObject object = new JsonObject();
        if (src instanceof IKey key) {
            return keySerializer.serialize(key, context);
        } else if (src instanceof HoverableKey hoverable) {
            object.add("key", keySerializer.serialize(hoverable.getKey(), context));
            object.add("tooltip", serializeArray(hoverable.getTooltipLines(), context));
        }
        return object;
    }
}
