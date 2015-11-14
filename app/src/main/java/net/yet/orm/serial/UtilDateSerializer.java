package net.yet.orm.serial;

import java.util.Date;

public final class UtilDateSerializer extends IntegerSerializer<Date> {

	@Override
	public Long toSqliteValue(Class<?> fieldType, Date fieldValue) {
		return fieldValue.getTime();
	}

	@Override
	public Date fromSqliteValue(Class<?> fieldType, Long sqliteValue) {
		return new Date(sqliteValue);
	}
}