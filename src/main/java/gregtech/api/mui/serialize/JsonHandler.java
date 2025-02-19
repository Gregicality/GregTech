package gregtech.api.mui.serialize;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.IntFunction;

public interface JsonHandler<T> extends JsonSerializer<T>, JsonDeserializer<T> {

    @Override
    default JsonElement serialize(T src, Type typeOfSrc, JsonSerializationContext context) {
        return serialize(src, context);
    }

    @Override
    default T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                                                                                              throws JsonParseException {
        return deserialize(json, context);
    }

    JsonElement serialize(T src, JsonSerializationContext context);

    T deserialize(JsonElement json, JsonDeserializationContext context) throws JsonParseException;

    default <R> JsonArray serializeArray(R[] objects, JsonSerializationContext context) {
        JsonArray array = new JsonArray();
        if (objects == null) return array;
        Type arrayType = objects.getClass().getComponentType();
        for (R t : objects) {
            JsonElement element = context.serialize(t, arrayType);
            if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
                JsonObject typed = new JsonObject();
                typed.addProperty("typed", t.getClass().getSimpleName());
                typed.add("element", element);
                array.add(typed);
            } else {
                array.add(element);
            }
        }
        return array;
    }

    @SuppressWarnings("unchecked")
    default <R> JsonArray serializeArray(Collection<R> objects, JsonSerializationContext context) {
        R[] array = null;
        int i = 0;
        for (R object : objects) {
            if (array == null) array = (R[]) Array.newInstance(object.getClass(), objects.size());
            array[i++] = object;
        }
        return serializeArray(array, context);
    }

    default <R> R[] deserializeArray(JsonArray jsonArray, JsonDeserializationContext context,
                                     IntFunction<R[]> function) {
        if (jsonArray == null || jsonArray.size() == 0) return function.apply(0);
        R[] array = function.apply(jsonArray.size());
        Type arrayType = array.getClass().getComponentType();
        Arrays.setAll(array, i -> handleArg(jsonArray.get(i), context, arrayType));
        return array;
    }

    static Object handleArg(JsonElement element, JsonDeserializationContext context, Type arrayType) {
        // args can sometimes be keys
        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            if (!object.has("typed"))
                return context.deserialize(object, IDrawable.class);

            JsonElement value = object.get("element");
            return switch (object.get("typed").getAsString()) {
                case "Integer" -> value.getAsInt();
                case "Long" -> value.getAsLong();
                case "Double" -> value.getAsDouble();
                case "Float" -> value.getAsFloat();
                case "Byte" -> value.getAsByte();
                default -> value.getAsNumber();
            };
        } else if (element instanceof JsonPrimitive primitive && primitive.isNumber()) {
            return primitive.getAsNumber();
        } else {
            return context.deserialize(element, arrayType);
        }
    }
}
