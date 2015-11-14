package net.yet.orm.serial;

import net.yet.orm.SqliteType;

/**
 * Created by yangentao on 2015/11/9.
 * entaoyang@163.com
 */
public abstract class TextSerializer<T> implements TypeSerializer<T, String> {
	@Override
	final public SqliteType getSqliteType() {
		return SqliteType.TEXT;
	}
}
