package net.yet.orm;

import android.database.Cursor;
import android.util.Log;

import net.yet.util.Util;
import net.yet.util.db.Where;

import java.util.ArrayList;
import java.util.List;

/**
 * 只能执行一次查询!
 *
 * @author yangentao@gmail.com
 */
public class OrmQuery {
	protected TableInfo tableInfo;
	protected Class<?> cls;
	protected String orderStr;
	protected String limitStr;
	protected String whereStr;
	protected ArrayList<Object> argList = new ArrayList<Object>();

	private OrmQuery() {
	}

	static OrmQuery select(Class<?> cls) {
		OrmQuery q = new OrmQuery();
		q.tableInfo = TableInfo.findTable(cls);
		q.cls = cls;
		return q;
	}

	public OrmQuery where(Where w) {
		if (w != null) {
			this.whereStr = w.toString();
			this.argList.addAll(w.args());
		}
		return this;
	}

	public OrmQuery where(String where) {
		this.whereStr = where;
		return this;
	}

	public OrmQuery where(String where, String... args) {
		this.whereStr = where;
		if (args != null) {
			for (String s : args) {
				argList.add(s);
			}
		}
		return this;
	}

	public OrmQuery wherePk(Object pkValue) {
		limit(1);
		return where(Where.eq(tableInfo.pkName, pkValue));
	}

	public OrmQuery whereEq(String col, String val) {
		return where(Where.eq(col, val));
	}

	public OrmQuery whereEq(String col, long val) {
		return where(Where.eq(col, val));
	}

	public OrmQuery limit(int limit) {
		if (limit > 0) {
			limitStr = " LIMIT " + limit + " ";
		}
		return this;
	}

	public OrmQuery limit(int limit, int offset) {
		if (limit > 0 && offset >= 0) {
			limitStr = " LIMIT " + limit + " OFFSET " + offset + " ";
		}
		return this;
	}

	public OrmQuery orderBy(String sortOrder) {
		this.orderStr = sortOrder;
		return this;
	}

	public OrmQuery desc(String column) {
		this.orderStr = column + " DESC ";
		return this;
	}

	public OrmQuery asc(String column) {
		this.orderStr = column + " ASC ";
		return this;
	}

	public <T> T findOne() {
		limit(1);
		Cursor cursor = query();
		return ModelUtil.parseCursorOne(tableInfo, cursor);
	}

	public <T> List<T> findAll() {
		Cursor cursor = query();
		return ModelUtil.parseCursorAll(tableInfo, cursor);
	}

	public Cursor query() {
		String sql = buildSql();
		return Orm.query(sql, Util.toStringArray(argList));
	}

	public boolean exist() {
		limit(1);
		return count() > 0;
	}

	public int count() {
		String sql = buildCountSql();
		Cursor cursor = Orm.query(sql, Util.toStringArray(argList));
		try {
			if (cursor.moveToFirst()) {
				return cursor.getInt(0);
			}
		} catch (Exception e) {
			Log.e("orm", Util.toLogString(e));
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return 0;
	}

	private String buildCountSql() {
		StringBuilder sb = new StringBuilder(256);
		sb.append(" SELECT count(*) from ");
		sb.append(tableInfo.tableName);
		if (whereStr != null && whereStr.trim().length() > 0) {
			sb.append(" WHERE ");
			sb.append(whereStr);
		}
		if (limitStr != null && limitStr.length() > 0) {
			sb.append(" ");
			sb.append(limitStr);
		}
		String sql = sb.toString();
		return sql;
	}

	private String buildSql() {
		StringBuilder sb = new StringBuilder(256);
		sb.append(" SELECT * from ");
		sb.append(tableInfo.tableName);
		if (whereStr != null && whereStr.trim().length() > 0) {
			sb.append(" WHERE ");
			sb.append(whereStr);
		}
		if (orderStr != null && orderStr.length() > 0) {
			sb.append(" ORDER BY ");
			sb.append(orderStr);
		}
		if (limitStr != null && limitStr.length() > 0) {
			sb.append(" ");
			sb.append(limitStr);
		}
		String sql = sb.toString();
		return sql;
	}

}
