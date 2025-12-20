package starred.skies.odin;

import com.google.gson.*;
import com.odtheking.odin.features.Category;

import java.lang.reflect.Type;

public class CategoryAdapter implements JsonSerializer<Category>, JsonDeserializer<Category> {
    @Override
    public JsonElement serialize(Category src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.name());
    }

    @Override
    public Category deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        String name = json.getAsString();
        for (Category cat : Category.values()) {
            if (cat.name().equals(name)) return cat;
        }
        return null;
    }
}