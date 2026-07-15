package org.dromara.milvus.plus.util;

import com.google.common.reflect.TypeToken;
import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;
import java.util.Map;

public final class GsonUtil {

    /**
     * Reusable Gson instance. Creating a new GsonBuilder on every call is expensive
     * and was previously broken for Date because serializer/deserializer overwrote each other.
     */
    private static final Gson GSON = createGson();

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
                // Date 序列化/反序列化必须注册到同一个 TypeAdapter，否则后者会覆盖前者
                .registerTypeAdapter(Date.class, new DateTypeAdapter())
                .create();
    }

    /**
     * 创建一个自定义的 Gson 实例，使用指定的反序列化器和序列化器。
     *
     * @param serializersAndDeserializers 自定义的序列化器和反序列化器。
     * @return 自定义的 Gson 实例
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
        return GSON.toJson(object);
    }

    /**
     * 将 JSON 字符串转换为对象。
     *
     * @param json  JSON 字符串
     * @param clazz 对象的类
     * @param <T>   对象的类型
     * @return 转换后的对象
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        return GSON.fromJson(json, clazz);
    }

    /**
     * 将 JSON 字符串转换为 Map 类型。
     *
     * @param json JSON 字符串
     * @param <K>  Map 的键类型
     * @param <V>  Map 的值类型
     * @return 转换后的 Map
     */
    public static <K, V> Map<K, V> fromJsonToMap(String json) {
        return GSON.fromJson(json, new TypeToken<Map<K, V>>() {
        }.getType());
    }

    /**
     * 将 JSON 字符串转换为 List 类型。
     *
     * @param json  JSON 字符串
     * @param clazz 对象的类
     * @param <T>   对象的类型
     * @return 转换后的 List
     */
    public static <T> List<T> fromJsonToList(String json, Class<T> clazz) {
        return GSON.fromJson(json, new TypeToken<List<T>>() {
        }.getType());
    }

    /**
     * 将 JSON 字符串转换为 JsonArray。
     *
     * @param json JSON 字符串
     * @return 转换后的 JsonArray
     */
    public static JsonArray fromJsonToJsonArray(String json) {
        return GSON.fromJson(json, JsonArray.class);
    }

    /**
     * 将 Map 转换为特定类型的对象。
     *
     * @param map  包含属性值的 Map
     * @param type Java 类型
     * @param <T>  对象的类型
     * @return 转换后的对象
     */
    public static <T> T convertMapToType(Map<String, Object> map, Type type) {
        String json = GSON.toJson(map);
        return GSON.fromJson(json, type);
    }

    /**
     * 将键值对添加到 JsonObject 中。
     * <p>
     * 跳过 null key，避免 Gson LinkedTreeMap NPE（社区反馈：动态字段/映射异常时 key 可能为 null）。
     *
     * @param jsonObject 要添加键值对的 JsonObject。
     * @param key        JSON 键。
     * @param value      JSON 值，可以是字符串、数字、布尔值等。
     */
    public static void put(JsonObject jsonObject, String key, Object value) {
        if (jsonObject == null || key == null) {
            return;
        }
        if (value == null) {
            jsonObject.add(key, JsonNull.INSTANCE);
            return;
        }
        if (value instanceof String) {
            jsonObject.addProperty(key, (String) value);
        } else if (value instanceof Number) {
            jsonObject.addProperty(key, (Number) value);
        } else if (value instanceof Boolean) {
            jsonObject.addProperty(key, (Boolean) value);
        } else if (value instanceof Character) {
            jsonObject.addProperty(key, (Character) value);
        } else if (value instanceof JsonElement) {
            jsonObject.add(key, (JsonElement) value);
        } else {
            jsonObject.add(key, GSON.toJsonTree(value));
        }
    }

    /**
     * Date 类型适配器：同时处理序列化与反序列化，避免 registerTypeAdapter 互相覆盖。
     */
    private static class DateTypeAdapter implements JsonSerializer<Date>, JsonDeserializer<Date> {
        @Override
        public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
            return src == null ? JsonNull.INSTANCE : new JsonPrimitive(src.getTime());
        }

        @Override
        public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            if (json == null || json.isJsonNull()) {
                return null;
            }
            return new Date(json.getAsJsonPrimitive().getAsLong());
        }
    }
}
