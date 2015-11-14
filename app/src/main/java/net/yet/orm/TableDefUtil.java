package net.yet.orm;

import android.database.Cursor;
import android.text.TextUtils;

import net.yet.orm.annotation.Column;
import net.yet.orm.annotation.PrimaryKey;
import net.yet.orm.annotation.Table;
import net.yet.util.db.DBQuery;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class TableDefUtil {

	public static void createOrModifyTable(TableInfo tableInfo) {
		boolean hasTable = DBQuery.existTable(Orm.database(), tableInfo.tableName);
		if (!hasTable) {
			TableDefUtil.createTable(tableInfo);
		} else {
			final Table tableAnnotation = tableInfo.tableType.getAnnotation(Table.class);
			if (tableAnnotation == null || tableAnnotation.autoAddField()) {
				TableDefUtil.mayAddField(tableInfo);
			}
		}
	}

	static void mayAddField(TableInfo tableInfo) {
		Cursor c = DBQuery.select(Orm.database()).from(tableInfo.tableName).limit(1).query();
		if (c.moveToFirst()) {
			Map<String, Integer> colMap = new HashMap<>();
			for (int i = 0; i < c.getColumnCount(); ++i) {
				String name = c.getColumnName(i);
				int type = c.getType(i);
				colMap.put(name, type);
			}
			c.close();

			for (Map.Entry<String, Field> e : tableInfo.nameFieldMap.entrySet()) {
				Integer type = colMap.get(e.getKey());
				if (type == null) {//add field
					String s = defColumn(tableInfo, e.getValue(), e.getKey());
					if (s != null && s.length() > 0) {
						String sql = "ALTER TABLE " + tableInfo.tableName + " ADD COLUMN " + s;
						Orm.execSQL(sql);
					}
				}
			}
		} else {
			c.close();
			Orm.execSQL("DROP TABLE IF EXISTS " + tableInfo.tableName);
			createTable(tableInfo);
		}

	}

	public static void createTable(TableInfo tableInfo) {
		Orm.beginTransaction();
		try {
			defTable(tableInfo);
			defIndex(tableInfo);
			Orm.setTransactionSuccessful();
		} finally {
			Orm.endTransaction();
		}
	}

	private static void defIndex(TableInfo tableInfo) {
		for (Field field : tableInfo.fieldSet) {
			String name = tableInfo.fieldMap.get(field);
			if (field.isAnnotationPresent(PrimaryKey.class)) {
				continue;
			}
			Column column = field.getAnnotation(Column.class);
			if (column == null || column.unique() ||  !column.index()) {
				continue;
			}
			String s = String.format("CREATE INDEX IF NOT EXISTS index_%s on %s(%s);", tableInfo.tableName + "_" + name, tableInfo.tableName, name);
			Orm.execSQL(s);
		}
	}

	private static void defTable(TableInfo tableInfo) {
		final ArrayList<String> definitions = new ArrayList<String>();
		for (Field field : tableInfo.fieldSet) {
			String name = tableInfo.fieldMap.get(field);
			String definition = defColumn(tableInfo, field, name);
			if (!TextUtils.isEmpty(definition)) {
				definitions.add(definition);
			}
		}

		String s = String.format("CREATE TABLE IF NOT EXISTS %s ( %s);", tableInfo.tableName,
				TextUtils.join(", ", definitions));
		Orm.execSQL(s);
	}

	@SuppressWarnings("unchecked")
	private static String defColumn(TableInfo tableInfo, Field field, String name) {
		StringBuilder definition = new StringBuilder();
		Class<?> type = field.getType();
		SqliteType sqlType = tableInfo.sqlTypeMap.get(field);
		if (sqlType == null) {
			return null;
		}
		definition.append(name);
		definition.append(" ");
		definition.append(sqlType.toString());

		PrimaryKey pk = field.getAnnotation(PrimaryKey.class);
		if (pk != null) {
			if (pk.length() > 0) {
				definition.append("(");
				definition.append(pk.length());
				definition.append(")");
			}
			if (pk.autoIncrease() && sqlType == SqliteType.INTEGER) {
				definition.append(" PRIMARY KEY AUTOINCREMENT");
			} else {
				definition.append(" PRIMARY KEY ");
			}
		} else {
			Column column = field.getAnnotation(Column.class);
			if (column != null) {
				if (column.length() > -1) {
					definition.append("(");
					definition.append(column.length());
					definition.append(")");
				}
				if (column.notNull()) {
					definition.append(" NOT NULL ");
				}
				if (column.unique()) {
					definition.append(" UNIQUE  ");
				}
			}
		}
		return definition.toString();
	}

}
