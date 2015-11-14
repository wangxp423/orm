package net.yet.orm.serial;

import net.yet.orm.SqliteType;

/**
 * 参数中的值, 都不是null
 * SQLValue是Long, Double, String ,byte[]之一,  其他值不可以
 * 可以使用它的子类, IntegerSerializer, BlobSerializer, TextSerializer, RealSerializer
 */
public interface TypeSerializer<ModelValue, SQLValue> {
	SqliteType getSqliteType();

	/**
	 * @param modelFieldValue
	 * @return
	 */
	SQLValue toSqliteValue(Class<?> fieldType, ModelValue fieldValue);

	/**
	 * @param sqliteValue
	 * @return
	 */
	ModelValue fromSqliteValue(Class<?> fieldType, SQLValue sqliteValue);
}