package net.yet.orm.serial;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * Created by yangentao on 2015/11/9.
 * entaoyang@163.com
 */
public class GsonSerializer extends TextSerializer<JsonElement> {

	@Override
	public String toSqliteValue(Class<?> fieldType, JsonElement fieldValue) {
		return fieldValue.toString();
	}

	@Override
	public JsonElement fromSqliteValue(Class<?> fieldType, String sqliteValue) {
		JsonParser p = new JsonParser();
		return p.parse(sqliteValue);
	}
}
