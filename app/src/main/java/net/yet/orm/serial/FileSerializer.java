package net.yet.orm.serial;

import java.io.File;

public final class FileSerializer extends TextSerializer<File> {

	@Override
	public String toSqliteValue(Class<?> fieldType, File fieldValue) {
		return fieldValue.toString();
	}

	@Override
	public File fromSqliteValue(Class<?> fieldType, String sqliteValue) {
		return new File(sqliteValue);
	}
}
