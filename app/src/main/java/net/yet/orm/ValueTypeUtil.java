package net.yet.orm;

import java.util.HashMap;

/**
 * Created by yangentao on 2015/11/9.
 * entaoyang@163.com
 */
public class ValueTypeUtil {
	static final HashMap<Class<?>, SqliteType> TYPE_MAP = new HashMap<Class<?>, SqliteType>() {
		{
			put(byte.class, SqliteType.INTEGER);
			put(short.class, SqliteType.INTEGER);
			put(int.class, SqliteType.INTEGER);
			put(long.class, SqliteType.INTEGER);
			put(float.class, SqliteType.REAL);
			put(double.class, SqliteType.REAL);
			put(boolean.class, SqliteType.INTEGER);
			put(char.class, SqliteType.TEXT);
			put(byte[].class, SqliteType.BLOB);
			put(Byte.class, SqliteType.INTEGER);
			put(Short.class, SqliteType.INTEGER);
			put(Integer.class, SqliteType.INTEGER);
			put(Long.class, SqliteType.INTEGER);
			put(Float.class, SqliteType.REAL);
			put(Double.class, SqliteType.REAL);
			put(Boolean.class, SqliteType.INTEGER);
			put(Character.class, SqliteType.TEXT);
			put(String.class, SqliteType.TEXT);
			put(Byte[].class, SqliteType.BLOB);
		}
	};

	private static boolean inSet(Class<?> clsVal, Class<?>... clsSet) {
		for (Class<?> one : clsSet) {
			if (clsVal == one) {
				return true;
			}
		}
		return false;
	}

	public static boolean subclassOf(Class<?> type, Class<?> superClass) {
		while (type != null) {
			if (type == superClass) {
				return true;
			}
			type = type.getSuperclass();
		}
		return false;
	}

	/**
	 * 只能返回String, double, long, byte[] 四种类型之一
	 *
	 * @param fieldType   非空
	 * @param normalValue 非空
	 * @return
	 */
	static Object toSqliteValue(Class<?> fieldType, Object normalValue) {
		if (normalValue instanceof String || normalValue instanceof byte[]) {
			return normalValue;
		}
		if (normalValue instanceof Long || normalValue instanceof Integer || normalValue instanceof Short || normalValue instanceof Byte) {
			return ((Number) normalValue).longValue();
		}
		if (normalValue instanceof Float || normalValue instanceof Double) {
			return ((Number) normalValue).doubleValue();
		}

		if (normalValue instanceof Boolean) {
			return (Boolean) normalValue ? 1L : 0L;
		}
		if (normalValue instanceof Character) {
			return normalValue.toString();
		}
		if (normalValue instanceof Enum) {
			return ((Enum) normalValue).name();
		}
		return null;
	}

	static Object fromSqliteValue(Class<?> fieldType, Object sqliteValue) {
		//text, blob
		if (fieldType == String.class || fieldType == byte[].class || fieldType == Byte[].class) {
			return sqliteValue;
		}

		if (sqliteValue == null) {
			if (fieldType.isPrimitive()) {
				if (fieldType == boolean.class) {
					return false;
				} else {
					return 0;
				}
			}
			return null;
		}
		//not null
		if (sqliteValue instanceof Number) {
			Number num = (Number) sqliteValue;
			if (inSet(fieldType, int.class, short.class, byte.class, Integer.class, Short.class, Byte.class)) {
				return num.intValue();
			}
			if (fieldType == long.class || fieldType == Long.class) {
				return num.longValue();
			}
			if (fieldType == float.class || fieldType == Float.class) {
				return num.floatValue();
			}
			if (fieldType == double.class || fieldType == Double.class) {
				return num.doubleValue();
			}
			if (fieldType == boolean.class || fieldType == Boolean.class) {
				return num.intValue() == 0 ? false : true;
			}
		}

		if (fieldType == char.class || fieldType == Character.class) {
			return sqliteValue.toString().charAt(0);
		}

		if (subclassOf(fieldType, Enum.class)) {
			return Enum.valueOf((Class<? extends Enum>) fieldType, sqliteValue.toString());
		}
		return null;
	}
}
