package net.yet.orm.serial;

import java.util.Calendar;

public final class CalendarSerializer extends IntegerSerializer<Calendar> {

	@Override
	public Long toSqliteValue(Class<?> fieldType, Calendar fieldValue) {
		return fieldValue.getTimeInMillis();
	}

	@Override
	public Calendar fromSqliteValue(Class<?> fieldType, Long sqliteValue) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(sqliteValue);
		return calendar;
	}
}