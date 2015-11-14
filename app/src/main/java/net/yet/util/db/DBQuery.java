package net.yet.util.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import net.yet.util.Util;

import java.util.ArrayList;

/**
 * 只能执行一次查询!
 *
 * @author yangentao@gmail.com
 */
public class DBQuery {
	private SQLiteDatabase db;
	protected String[] columns;
	protected String[] fromStr;
	protected String orderStr;
	protected String limitStr;
	protected String whereStr;
	protected ArrayList<Object> argList = new ArrayList<Object>();
	protected boolean distinct = false;

	private DBQuery() {
	}

	public static boolean existTable(SQLiteDatabase db, String tableName) {
		return select(db).from("sqlite_master").where(Where.eq("type", "table").andEq("name", tableName)).queryExist();
	}

	public static DBQuery selectDistinct(SQLiteDatabase db, String... cols) {
		DBQuery q = select(db, cols);
		q.distinct = true;
		return q;
	}

	public static DBQuery select(SQLiteDatabase db, String... cols) {
		DBQuery q = new DBQuery();
		q.db = db;
		q.columns = cols;
		return q;
	}

	public DBQuery from(String... tables) {
		this.fromStr = tables;
		return this;
	}

	public DBQuery where(Where w) {
		this.whereStr = w.toString();
		this.argList.addAll(w.args());
		return this;
	}

	public DBQuery whereEq(String col, String val) {
		return where(Where.eq(col, val));
	}

	public DBQuery whereEq(String col, long val) {
		return where(Where.eq(col, val));
	}

	public DBQuery limit(int limit) {
		if (limit > 0) {
			limitStr = " LIMIT " + limit + " ";
		}
		return this;
	}

	public DBQuery limit(int limit, int offset) {
		if (limit > 0 && offset >= 0) {
			limitStr = " LIMIT " + limit + " OFFSET " + offset + " ";
		}
		return this;
	}

	public DBQuery orderBy(String sortOrder) {
		this.orderStr = sortOrder;
		return this;
	}

	public DBQuery desc(String column) {
		this.orderStr = column + " DESC ";
		return this;
	}

	public DBQuery asc(String column) {
		this.orderStr = column + " ASC ";
		return this;
	}

	public int queryCount() {
		String sql = buildCountSql();
		Cursor c = db.rawQuery(sql, Util.toStringArray(argList));
		if (c != null) {
			try {
				if (c.moveToFirst()) {
					return c.getInt(0);
				}
			} finally {
				c.close();
				db = null;
			}
		}
		return 0;
	}

	public boolean queryExist() {
		limit(1);
		return queryCount() > 0;
	}

	public Cursor query() {
		String sql = buildSql();
		Cursor c = db.rawQuery(sql, Util.toStringArray(argList));
		db = null;
		return c;
	}

	public String buildSql() {
		StringBuilder sb = new StringBuilder(256);
		sb.append(" SELECT ");
		if (distinct) {
			sb.append(" DISTINCT ");
		}
		if (columns == null || columns.length == 0) {
			sb.append(" * ");
		} else {
			String cs = TextUtils.join(",", columns);
			sb.append(cs);
		}
		sb.append(" FROM ");
		sb.append(TextUtils.join(",", fromStr));
		if (Util.trimLength(whereStr) > 0) {
			sb.append(" WHERE ");
			sb.append(whereStr);
		}
		if (Util.notEmpty(orderStr)) {
			sb.append(" ORDER BY ");
			sb.append(orderStr);
		}
		if (Util.notEmpty(limitStr)) {
			sb.append(" ");
			sb.append(limitStr);
		}
		String sql = sb.toString();
		return sql;
	}

	protected Cursor _queryOne() {
		limit(1);
		return query();
	}

	private String buildCountSql() {
		StringBuilder sb = new StringBuilder(256);
		sb.append(" SELECT ");
//		if (distinct) {//TODO ???
//			sb.append(" DISTINCT ");
//		}
		sb.append(" count(*) ");
		sb.append(" FROM ");
		sb.append(TextUtils.join(",", fromStr));
		if (Util.trimLength(whereStr) > 0) {
			sb.append(" WHERE ");
			sb.append(whereStr);
		}
		if (Util.notEmpty(limitStr)) {
			sb.append(" ");
			sb.append(limitStr);
		}
		String sql = sb.toString();
		return sql;
	}

}
