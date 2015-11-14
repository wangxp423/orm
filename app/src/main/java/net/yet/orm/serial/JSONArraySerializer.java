package net.yet.orm.serial;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by yangentao on 2015/11/9.
 * entaoyang@163.com
 */
public class JSONArraySerializer extends TextSerializer<JSONArray> {

	@Override
	public String toSqliteValue(Class<?> fieldType, JSONArray fieldValue) {
		return fieldValue.toString();
	}

	@Override
	public JSONArray fromSqliteValue(Class<?> fieldType, String sqliteValue) {
		try {
			return new JSONArray(sqliteValue);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
}
