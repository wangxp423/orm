package net.yet.orm;

import net.yet.orm.annotation.PrimaryKey;
import net.yet.util.JsonUtil;

/**
 * Created by yangentao on 2015/11/9.
 * entaoyang@163.com
 */
public class IdModel {
	public static final String ID = "_id";
	@PrimaryKey(autoIncrease = true)
	private long _id = 0;

	public long getId() {
		return _id;
	}

	public long insert() {
		return Orm.insert(this);
	}

	public int update() {
		return Orm.updateByPk(this);
	}

	public boolean save() {
		if (_id == 0) {
			return -1 != insert();
		} else {
			return update() > 0;
		}
	}

	public boolean delete() {
		return Orm.deleteByPk(getClass(), getId()) > 0;
	}

	public String getTableName() {
		return Orm.tableName(getClass());
	}

	@Override
	public String toString() {
		return JsonUtil.toJson(this);
	}

	@Override
	public int hashCode() {
		long tabHash = getTableName().hashCode();
		return _id == 0 ? super.hashCode() : Long.valueOf(tabHash << 16 + _id).hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (_id != 0 && o != null && getClass().equals(o.getClass())) {
			return _id == ((IdModel) o)._id;
		}
		return super.equals(o);
	}
}
