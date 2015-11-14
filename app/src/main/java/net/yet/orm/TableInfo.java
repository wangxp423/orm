package net.yet.orm;


import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.yet.orm.annotation.Column;
import net.yet.orm.annotation.PrimaryKey;
import net.yet.orm.annotation.Serializer;
import net.yet.orm.annotation.Table;
import net.yet.orm.serial.BigDecimalSerializer;
import net.yet.orm.serial.BlobSerializer;
import net.yet.orm.serial.CalendarSerializer;
import net.yet.orm.serial.FileSerializer;
import net.yet.orm.serial.GsonSerializer;
import net.yet.orm.serial.IntegerSerializer;
import net.yet.orm.serial.JSONArraySerializer;
import net.yet.orm.serial.JSONObjectSerializer;
import net.yet.orm.serial.RealSerializer;
import net.yet.orm.serial.SqlDateSerializer;
import net.yet.orm.serial.TextSerializer;
import net.yet.orm.serial.TypeSerializer;
import net.yet.orm.serial.UUIDSerializer;
import net.yet.orm.serial.UtilDateSerializer;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.UUID;

public final class TableInfo {
	//model类和表信息map
	static HashMap<Class<?>, TableInfo> tableMap = new HashMap<>();
	//全局转换器
	static Map<Class<?>, TypeSerializer> typeSerializerMap = new HashMap<Class<?>, TypeSerializer>() {
		{
			put(BigDecimal.class, new BigDecimalSerializer());
			put(Calendar.class, new CalendarSerializer());
			put(File.class, new FileSerializer());
			put(java.sql.Date.class, new SqlDateSerializer());
			put(java.util.Date.class, new UtilDateSerializer());
			put(UUID.class, new UUIDSerializer());
			put(JsonObject.class, new GsonSerializer());
			put(JsonArray.class, new GsonSerializer());
			put(JSONArray.class, new JSONArraySerializer());
			put(JSONObject.class, new JSONObjectSerializer());
		}
	};
	public String tableName;//表明
	Field pkField;//主键字段
	String pkName;//主键名
	boolean pkAutoInc = false;//主键是否自增
	Class<?> tableType;//model类
	Map<Field, String> fieldMap = new LinkedHashMap<>();//field, 字段名字
	Map<String, Field> nameFieldMap = new LinkedHashMap<>();//字段名字=>Field
	LinkedHashSet<Field> fieldSet;//字段集合
	Map<Field, SqliteType> sqlTypeMap = new HashMap<>();//字段对应的sqlite类型
	Map<Field, TypeSerializer> fieldSerializerMap = null;//在每个字段上的转换器-- 如果有的话

	private TableInfo(Class<?> cls) {
		tableType = cls;
		tableName = makeTableName(cls);
		fieldSet = ReflectionUtils.getDeclaredColumnFields(cls);
		for (Field field : fieldSet) {
			//处理名字
			String fieldName = makeFieldName(field);
			fieldMap.put(field, fieldName);
			nameFieldMap.put(fieldName, field);

			//类型转换
			SqliteType sqliteType = ValueTypeUtil.TYPE_MAP.get(field.getType());
			if (sqliteType == null) {
				if (ValueTypeUtil.subclassOf(field.getType(), Enum.class)) {
					sqliteType = SqliteType.TEXT;
				}
			}
			if (sqliteType != null) {
				sqlTypeMap.put(field, sqliteType);
			}
			if (!field.getType().isPrimitive()) {//原是类型byte, char, short, int, long, float, double, boolean 不可转换
				//字段上的转换器
				Serializer serializer = field.getAnnotation(Serializer.class);
				if (serializer != null) {
					Class<? extends TypeSerializer> ts = serializer.value();
					try {
						TypeSerializer tsObj = ts.newInstance();
						if (fieldSerializerMap == null) {
							fieldSerializerMap = new HashMap<Field, TypeSerializer>();
						}
						fieldSerializerMap.put(field, tsObj);
						sqlTypeMap.put(field, tsObj.getSqliteType());
					} catch (Exception ex) {
						ex.printStackTrace();
						Log.e("orm", ex.getLocalizedMessage());
					}
				}
			}
		}

		if (pkField != null) {
			pkName = fieldMap.get(pkField);
		}
	}

	private static String makeTableName(Class<?> cls) {
		Table tableAnnotation = cls.getAnnotation(Table.class);
		if (tableAnnotation != null) {
			String name = tableAnnotation.name();
			if (name != null && name.trim().length() > 0) {
				return name.trim();
			}
		}
		return cls.getSimpleName();
	}

	public TypeSerializer findTypeSerializer(Field field) {
		if (field.getType().isPrimitive()) {//原始类型, 返回null
			return null;
		}
		if (fieldSerializerMap != null) {
			TypeSerializer s = fieldSerializerMap.get(field);
			if (s != null) {
				return s;
			}
		}
		return typeSerializerMap.get(field.getType());
	}

	static void addTypeSerializer(Class<?> fieldType, TypeSerializer typeSerializer) {
		if (typeSerializer instanceof TextSerializer || typeSerializer instanceof IntegerSerializer || typeSerializer instanceof BlobSerializer || typeSerializer instanceof RealSerializer) {
			typeSerializerMap.put(fieldType, typeSerializer);
		} else {
			throw new IllegalArgumentException("转化器不能直接实现TypeSerializer,  只能继承TextSerializer, LongSerializer, BlobSerializer,  RealSerializer之一, 并且提供默认构造函数");
		}
	}

	synchronized public static TableInfo findTable(Class<?> type) {
		TableInfo tableInfo = tableMap.get(type);
		if (tableInfo == null) {
			tableInfo = new TableInfo(type);
			TableDefUtil.createOrModifyTable(tableInfo);
			tableMap.put(type, tableInfo);

		}
		return tableInfo;
	}

	private String makeFieldName(Field field) {
		if (field.isAnnotationPresent(PrimaryKey.class)) {
			if (pkField != null) {
				throw new IllegalArgumentException("主键已经存在");
			}
			PrimaryKey pk = field.getAnnotation(PrimaryKey.class);
			pkField = field;
			if (pk.autoIncrease()) {
				Class<?> type = field.getType();
				if (type.equals(Integer.class) || type.equals(int.class) || type.equals(Long.class) || type.equals(long.class)) {
					pkAutoInc = true;
				}
			}

			String name = pk.name();
			if (name != null && name.trim().length() > 0) {
				return name.trim();
			}
		} else if (field.isAnnotationPresent(Column.class)) {
			Column columnAnnotation = field.getAnnotation(Column.class);
			String name = columnAnnotation.name();
			if (name != null && name.trim().length() > 0) {
				return name.trim();
			}
		}
		return field.getName();
	}

}
