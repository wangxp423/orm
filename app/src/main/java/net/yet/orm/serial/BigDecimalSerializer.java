package net.yet.orm.serial;

import java.math.BigDecimal;

public final class BigDecimalSerializer extends TextSerializer<BigDecimal> {

	@Override
	public String toSqliteValue(Class<?> fieldType, BigDecimal fieldValue) {
		return fieldValue.toString();
	}

	@Override
	public BigDecimal fromSqliteValue(Class<?> fieldType, String sqliteValue) {
		return new BigDecimal(sqliteValue);
	}
}