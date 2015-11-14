package net.yet.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.lang.reflect.Type;

/**
 * Type mapType = new TypeToken<HashMap<String, String>>() {
 * }.getType();
 * HashMap<String, String> m = JsonUtil.loadGeneric(FILE_NAME, mapType);
 *
 * @author yangentao@gmail.com
 */
public class JsonUtil {
	public static JsonObject parseObject(String s) {
		if (Util.notEmpty(s)) {
			JsonParser p = new JsonParser();
			JsonElement je = p.parse(s);
			if (je != null && je.isJsonObject()) {
				return je.getAsJsonObject();
			}
		}
		return null;
	}

	public static JsonArray parseArray(String s) {
		if (Util.notEmpty(s)) {
			JsonParser p = new JsonParser();
			JsonElement je = p.parse(s);
			if (je != null && je.isJsonArray()) {
				return je.getAsJsonArray();
			}
		}
		return null;
	}

	public static <T> T fromJson(String json, Class<T> cls) {
		Gson g = new Gson();
		return g.fromJson(json, cls);
	}

	public static <T> T fromJsonGeneric(String json, Type type) {
		Gson g = new Gson();
		return g.fromJson(json, type);
	}

	public static String toJson(Object data) {
		Gson g = new Gson();
		return g.toJson(data);
	}

	// List<String>, Map<String, Object> ....
	public static String toJsonGeneric(Object data, Type type) {
		Gson g = new Gson();
		return g.toJson(data, type);
	}

}
