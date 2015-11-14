package net.yet.orm.serial;

import net.yet.orm.SqliteType;

/**
 * Created by yangentao on 2015/11/9.
 * entaoyang@163.com
 */
public abstract class RealSerializer<T> implements TypeSerializer<T, Double> {
	@Override
	final public SqliteType getSqliteType() {
		return SqliteType.REAL;
	}

}
