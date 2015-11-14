package net.yet.orm;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import net.yet.orm.serial.TypeSerializer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by yangentao on 2015/11/8.
 * entaoyang@163.com
 */
public class ModelUtil {

	public static int updateByRowId(Object model, long rowid) {
		return updateWhere(model, "_ROWID_=" + rowid);
	}

	public static int updateWhere(Object model, String where, String... args) {
		TableInfo tableInfo = TableInfo.findTable(model.getClass());
		ContentValues values = makeValues(model, tableInfo);
		return Orm.update(tableInfo.tableName, values, where, args);
	}

	public static int updateExclude(Object model, String[] excludeClumns, String where, String... args) {
		TableInfo tableInfo = TableInfo.findTable(model.getClass());
		ContentValues values = makeValues(model, tableInfo);
		if (excludeClumns != null) {
			for (String col : excludeClumns) {
				values.remove(col);
			}
		}
		return Orm.update(tableInfo.tableName, values, where, args);
	}

//	public static int updateInclude(Object model, String[] includeClumns, String where, String... args) {
//		TableInfo tableInfo = TableInfo.findTable(model.getClass());
//		ContentValues values = makeValues(model, tableInfo, includeClumns == null ? null : Util.asSet(includeClumns));
//		return Orm.update(tableInfo.tableName, values, where, args);
//	}

	public static int updateByPk(Object model) {
		TableInfo tableInfo = TableInfo.findTable(model.getClass());
		if (tableInfo.pkName == null) {
			Log.e("orm", "没有找到主键" + model.getClass().getName());
			return 0;
		}
		ContentValues values = makeValues(model, tableInfo);
		Object pkValue = values.get(tableInfo.pkName);
		if (pkValue == null) {
			Log.e("orm", "主键没有值" + model.getClass().getName());
			return 0;
		}
		values.remove(tableInfo.pkName);
		return Orm.update(tableInfo.tableName, values, tableInfo.pkName + "=" + pkValue.toString());
	}

	public static long insert(Object model) {
		TableInfo tableInfo = TableInfo.findTable(model.getClass());
		ContentValues values = makeValues(model, tableInfo);
		boolean assignId = false;
		if (tableInfo.pkAutoInc) {//整形自增
			Object pkValue = values.get(tableInfo.pkName);
			if (pkValue == null || 0 == ((Number) pkValue).intValue()) {
				values.remove(tableInfo.pkName);
				assignId = true;
			}
		}
		long id = Orm.insert(tableInfo.tableName, values);
		if (assignId && id != -1) {
			try {
				Class<?> type = tableInfo.pkField.getType();
				if (type.equals(int.class) || type.equals(Integer.class)) {
					tableInfo.pkField.set(model, (int) id);
				} else if (type.equals(long.class) || type.equals(Long.class)) {
					tableInfo.pkField.set(model, id);
				}
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return id;
	}

	public static void loadFromCursor(TableInfo tableInfo, Cursor cursor, Object model) {
		for (int columnIndex = 0; columnIndex < cursor.getColumnCount(); ++columnIndex) {
			String colName = cursor.getColumnName(columnIndex);
			Field field = tableInfo.nameFieldMap.get(colName);
			if (field == null) {
				continue;
			}
			field.setAccessible(true);
			SqliteType sqliteType = tableInfo.sqlTypeMap.get(field);
			if (sqliteType == null) {//此列没有找到类型定义, 可能是 自定义的类, 又没有添加转换器, 因此忽略.
				continue;
			}
			Object value = null;
			if (cursor.isNull(columnIndex)) {
				value = null;
			} else if (sqliteType == SqliteType.TEXT) {
				value = cursor.getString(columnIndex);
			} else if (sqliteType == SqliteType.INTEGER) {
				value = cursor.getLong(columnIndex);
			} else if (sqliteType == SqliteType.REAL) {
				value = cursor.getDouble(columnIndex);
			} else if (sqliteType == SqliteType.BLOB) {
				value = cursor.getBlob(columnIndex);
			}
			//全局或自定义的转换器, 优先处理
			TypeSerializer typeSerializer = tableInfo.findTypeSerializer(field);
			if (value != null && typeSerializer != null) {
				value = typeSerializer.fromSqliteValue(field.getType(), value);
			} else {//没有转换器, 则使用默认转换
				value = ValueTypeUtil.fromSqliteValue(field.getType(), value);
			}
			try {
				field.set(model, value);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public static <T> T parseCursorOne(TableInfo tableInfo, Cursor cursor) {
		try {
			Constructor<?> entityConstructor = tableInfo.tableType.getConstructor();
			if (cursor.moveToFirst()) {
				T entity = (T) entityConstructor.newInstance();
				loadFromCursor(tableInfo, cursor, entity);
				return entity;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static <T> List<T> parseCursorAll(TableInfo tableInfo, Cursor cursor) {
		List<T> entityList = new ArrayList<>();
		try {
			Constructor<?> entityConstructor = tableInfo.tableType.getConstructor();
			if (cursor.moveToFirst()) {
				do {
					T entity = (T) entityConstructor.newInstance();
					loadFromCursor(tableInfo, cursor, entity);
					entityList.add(entity);
				} while (cursor.moveToNext());
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return entityList;
	}

	/**
	 * 查找主键的值, 对应的是sqlite的类型
	 *
	 * @param tableInfo
	 * @param obj
	 * @return
	 */
	public static Object getPkSqliteValue(TableInfo tableInfo, Object obj) {
		return toSqliteValue(tableInfo, obj, tableInfo.pkField);
	}

	private static Object toSqliteValue(TableInfo tableInfo, Object model, Field field) {
		if (model != null && field != null) {
			try {
				field.setAccessible(true);
				Object value = field.get(model);//原始值
				if (value != null) {
					//非空时, 先找转换器, 如果没有转换器, 则使用默认的转换.
					TypeSerializer typeSerializer = tableInfo.findTypeSerializer(field);
					if (typeSerializer != null) {
						value = typeSerializer.toSqliteValue(field.getType(), value);
					} else {
						value = ValueTypeUtil.toSqliteValue(field.getType(), value);
					}
				}
				return value;
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static ContentValues makeValues(Object model) {
		return makeValues(model, TableInfo.findTable(model.getClass()));
	}

	public static ContentValues makeValues(Object model, TableInfo tableInfo) {
		return makeValues(model, tableInfo, null);
	}

	public static ContentValues makeValues(Object model, TableInfo tableInfo, Set<String> columns) {
		ContentValues values = new ContentValues(12);
		for (Map.Entry<String, Field> entry : tableInfo.nameFieldMap.entrySet()) {
			String fieldName = entry.getKey();
			if (columns != null && !columns.contains(fieldName)) {
				continue;
			}

			Object value = toSqliteValue(tableInfo, model, entry.getValue());
			if (value == null) {
				values.putNull(fieldName);
			} else if (value instanceof String) {
				values.put(fieldName, (String) value);
			} else if (value instanceof Long) {
				values.put(fieldName, (Long) value);
			} else if (value instanceof Double) {
				values.put(fieldName, (Double) value);
			} else if (value instanceof byte[]) {
				values.put(fieldName, (byte[]) value);
			} else {
				Log.e("orm", "错误的数据类型 " + fieldName + " 只能是String,long,double,byte[]之一");
			}
		}
		return values;
	}

}
