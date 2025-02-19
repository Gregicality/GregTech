package gregtech.api.mui.serialize;

import net.minecraft.util.text.TextFormatting;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;

public class FormatSerializer implements JsonHandler<TextFormatting> {

    @Override
    public TextFormatting deserialize(JsonElement json,
                                      JsonDeserializationContext context) throws JsonParseException {
        return TextFormatting.getValueByName(json.getAsString());
    }

    @Override
    public JsonElement serialize(TextFormatting src,
                                 JsonSerializationContext context) {
        return context.serialize(src.getFriendlyName());
    }
}
