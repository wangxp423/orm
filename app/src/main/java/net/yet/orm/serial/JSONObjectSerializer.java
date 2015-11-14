package net.yet.orm.serial;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by yangentao on 2015/11/9.
 * entaoyang@163.com
 */
public class JSONObjectSerializer extends TextSerializer<JSONObject> {

	@Override
	public String toSqliteValue(Class<?> fieldType, JSONObject fieldValue) {
		return fieldValue.toString();
	}

	@Override
	public JSONObject fromSqliteValue(Class<?> fieldType, String sqliteValue) {
		try {
			return new JSONObject(sqliteValue);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
}
