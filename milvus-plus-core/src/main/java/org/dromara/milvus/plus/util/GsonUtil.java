package org.dromara.milvus.plus.util;

import com.google.common.reflect.TypeToken;
import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;
import java.util.Map;

public final class GsonUtil {

    private GsonUtil() {
        // 私有构造方法，防止实例化
    }

    /**
     * 创建一个标准的 Gson 实例。
     *
     * @return 标准的 Gson 实例。
     */
    public static Gson createGson() {
        return new GsonBuilder()
                .setPrettyPrinting()
                .serializeNulls()
                .registerTypeAdapter(Date.class, new DateSerializer())
                .registerTypeAdapter(Date.class, new DateDeserializer())
                .create();
    }

    /**
     * 创建一个自定义的 Gson 实例，使用指定的反序列化器和序列化器。
     *
     * @param serializersAndDeserializers 自定义的序列化器和反序列化器。
     * @return 自定义的 Gson 实例。
     */
    public static Gson createGsonWithAdapters(JsonSerializer<?>... serializersAndDeserializers) {
        GsonBuilder builder = new GsonBuilder();
        for (JsonSerializer<?> serializer : serializersAndDeserializers) {
            builder.registerTypeAdapter(serializer.getClass(), serializer);
        }
        return builder.create();
    }

    /**
     * 将对象转换为 JSON 字符串。
     *
     * @param object 要转换的对象
     * @return JSON 字符串
     */
    public static String toJson(Object object) {
        return createGson().toJson(object);
    }

    /**
     * 将 JSON 字符串转换为对象。
     *
     * @param json    JSON 字符串
     * @param clazz   对象的类
     * @param <T>     对象的类型
     * @return 转换后的对象
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        return createGson().fromJson(json, clazz);
    }

    /**
     * 将 JSON 字符串转换为 Map 类型。
     *
     * @param json  JSON 字符串
     * @param <K>   Map 的键类型
     * @param <V>   Map 的值类型
     * @return 转换后的 Map
     */
    public static <K, V> Map<K, V> fromJsonToMap(String json) {
        return createGson().fromJson(json, new TypeToken<Map<K, V>>() {}.getType());
    }

    /**
     * 将 JSON 字符串转换为 List 类型。
     *
     * @param json    JSON 字符串
     * @param clazz   对象的类
     * @param <T>     对象的类型
     * @return 转换后的 List
     */
    public static <T> List<T> fromJsonToList(String json, Class<T> clazz) {
        return createGson().fromJson(json, new TypeToken<List<T>>() {}.getType());
    }

    /**
     * 将 JSON 字符串转换为 JsonArray。
     *
     * @param json  JSON 字符串
     * @return 转换后的 JsonArray
     */
    public static com.google.gson.JsonArray fromJsonToJsonArray(String json) {
        return createGson().fromJson(json, com.google.gson.JsonArray.class);
    }

    /**
     * 日期的自定义序列化器。
     */
    private static class DateSerializer implements JsonSerializer<Date> {
        @Override
        public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.getTime());
        }
    }
    /**
     * 将 Map 转换为特定类型的对象。
     *
     * @param map     包含属性值的 Map
     * @param type    Java 类型
     * @param <T>     对象的类型
     * @return 转换后的对象
     */
    public static <T> T convertMapToType(Map<String, Object> map, Type type) {
        Gson gson = createGson();
        // 将 Map 转换为 JSON 字符串
        String json = gson.toJson(map);
        // 将 JSON 字符串转换为特定类型的对象
        return gson.fromJson(json, type);
    }
    /**
     * 将键值对添加到 JsonObject 中。
     *
     * @param jsonObject 要添加键值对的 JsonObject。
     * @param key        JSON 键。
     * @param value      JSON 值，可以是字符串、数字、布尔值等。
     */
    public static void put(JsonObject jsonObject, String key, Object value) {
        Gson gson = createGson();
        if (value instanceof String) {
            jsonObject.addProperty(key, (String) value);
        } else if (value instanceof Number) {
            jsonObject.addProperty(key, (Number) value);
        } else if (value instanceof Boolean) {
            jsonObject.addProperty(key, (Boolean) value);
        } else {
            jsonObject.add(key, gson.toJsonTree(value));
        }
    }

    /**
     * 日期的自定义反序列化器。
     */
    private static class DateDeserializer implements JsonDeserializer<Date> {
        @Override
        public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return new Date(json.getAsJsonPrimitive().getAsLong());
        }
    }
}