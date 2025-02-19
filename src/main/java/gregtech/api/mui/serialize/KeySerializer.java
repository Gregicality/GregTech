package gregtech.api.mui.serialize;

import net.minecraft.util.text.TextFormatting;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.text.CompoundKey;
import com.cleanroommc.modularui.drawable.text.DynamicKey;
import com.cleanroommc.modularui.drawable.text.FormattingState;
import com.cleanroommc.modularui.drawable.text.LangKey;
import com.cleanroommc.modularui.drawable.text.StringKey;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Map;

public class KeySerializer implements JsonHandler<IKey> {

    private static final Map<String, TextFormatting> FORMATTING_MAP = new Object2ObjectOpenHashMap<>();

    static {
        for (var tf : TextFormatting.values()) {
            FORMATTING_MAP.put(tf.toString(), tf);
        }
    }

    @Override
    public IKey deserialize(JsonElement json, JsonDeserializationContext context)
                                                                                  throws JsonParseException {
        JsonObject object = json.getAsJsonObject();
        if (object.has("string")) {
            return IKey.str(object.get("string").getAsString());
        } else if (object.has("lang")) {
            String lang = context.deserialize(object.get("lang"), String.class);
            TextFormatting[] formatting = deserializeArray(object.getAsJsonArray("format"), context,
                    TextFormatting[]::new);
            Object[] args = deserializeArray(
                    object.getAsJsonArray("args"), context, Object[]::new);
            return IKey.lang(lang, args).style(formatting);
        } else if (object.has("keys")) {
            IKey[] keys = deserializeArray(
                    object.getAsJsonArray("keys"), context, IKey[]::new);
            TextFormatting[] formatting = deserializeArray(
                    object.getAsJsonArray("format"), context, TextFormatting[]::new);
            return IKey.comp(keys).style(formatting);
        }
        return IKey.EMPTY;
    }

    @Override
    public JsonElement serialize(IKey src, JsonSerializationContext context) {
        JsonObject obj = new JsonObject();
        if (src instanceof StringKey || src instanceof DynamicKey) {
            obj.add("string", context.serialize(src.getFormatted()));
        } else if (src instanceof LangKey langKey) {
            obj.add("lang", context.serialize(langKey.getKeySupplier().get()));
            TextFormatting[] formattings = convert(langKey.getFormatting());
            obj.add("format", serializeArray(formattings, context));
            Object[] args = langKey.getArgsSupplier().get();
            if (!ArrayUtils.isEmpty(args))
                obj.add("args", serializeArray(args, context));
        } else if (src instanceof CompoundKey compoundKey) {
            obj.add("keys", serializeArray(compoundKey.getKeys(), context));
            obj.add("format", serializeArray(convert(compoundKey.getFormatting()), context));
        }
        return obj;
    }

    public static TextFormatting[] convert(FormattingState state) {
        if (state == null) return new TextFormatting[0];
        String s = state.getFormatting();
        TextFormatting[] formattings = new TextFormatting[s.length() / 2];
        for (int i = 0; i < s.length(); i += 2) {
            formattings[i / 2] = FORMATTING_MAP.get(s.substring(i, i + 2));
        }
        return formattings;
    }
}
