package net.yet.orm.serial;

import java.util.UUID;

public final class UUIDSerializer extends TextSerializer<UUID> {

	@Override
	public String toSqliteValue(Class<?> fieldType, UUID fieldValue) {
		return fieldValue.toString();
	}

	@Override
	public UUID fromSqliteValue(Class<?> fieldType, String sqliteValue) {
		return UUID.fromString(sqliteValue);
	}
}