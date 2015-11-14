package net.yet.util.db;

import android.text.TextUtils;

import net.yet.util.Util;

import java.util.ArrayList;

public class Where {
	public static Where condition(String condition) {
		Where w = new Where();
		if (!TextUtils.isEmpty(condition)) {
			w.where.append(condition);
		}
		return w;
	}

	public static Where eq(String column, int value) {
		return op(column, "=", value);
	}

	public static Where eq(String column, long value) {
		return op(column, "=", value);
	}

	public static Where eq(String column, Object arg) {
		return opArg(column, "=", arg);
	}

	public static Where eq(String column, String value) {
		return op(column, "=", value);
	}

	public static Where eqArg(String column, String value) {
		return opArg(column, "=", value);
	}

	public static Where zero(String column) {
		return op(column, "=", 0);
	}

	public static Where notZero(String column) {
		return op(column, "!=", 0);
	}

	public static Where notNull(String column) {
		return condition(column + " IS NOT NULL ");
	}

	public static Where isNull(String column) {
		return condition(column + " IS NULL ");
	}

	public static Where ge(String column, long value) {
		return op(column, ">=", value);
	}

	public static Where great(String column, long value) {
		return op(column, ">", value);
	}

	public static Where _id(long value) {
		return op("_id", "=", value);
	}

	public static Where le(String column, long value) {
		return op(column, "<=", value);
	}

	public static Where less(String column, long value) {
		return op(column, "<", value);
	}

	public static Where op(String column, String op, long value) {
		Where w = new Where();
		w.where.append(column);
		w.where.append(op);
		w.where.append(value);
		w.where.append(" ");
		return w;
	}

	public static Where op(String column, String op, String value) {
		Where w = new Where();
		w.where.append(column);
		w.where.append(op);
		w.where.append("'");
		w.where.append(value);
		w.where.append("' ");
		return w;
	}

	public static Where opArg(String column, String op, Object arg) {
		Where w = new Where();
		w.where.append(column);
		w.where.append(op);
		w.where.append("? ");
		w.addArg(arg);
		return w;
	}

	protected StringBuilder where = new StringBuilder(256);

	protected ArrayList<Object> argList = new ArrayList<Object>();

	private void addArg(Object arg) {
		argList.add(arg);
	}

	public Where and(String condition) {
		this.where.append(" AND (");
		where.append(condition);
		where.append(")");
		return this;
	}

	// key-value
	public Where andEq(String column, long value) {
		return andOp(column, "=", value);
	}

	// key-value
	public Where andEq(String column, String value) {
		return andOp(column, "=", value);
	}

	public Where andFalse(String column) {
		return andOp(column, "=", 0);
	}

	public Where andGE(String column, long value) {
		return andOp(column, ">=", value);
	}

	public Where andGreat(String column, long value) {
		return andOp(column, ">", value);
	}

	public Where andLess(String column, long value) {
		return andOp(column, "<", value);
	}

	public Where andLE(String column, long value) {
		return andOp(column, "<=", value);
	}

	public Where andOp(String column, String op, long value) {
		this.where.append(" AND (");
		this.where.append(column);
		this.where.append(" ");
		this.where.append(op);
		this.where.append(" ");
		this.where.append(value);
		this.where.append(")");
		return this;
	}

	public Where andOp(String column, String op, String value) {
		this.where.append(" AND (");
		this.where.append(column);
		this.where.append(" ");
		this.where.append(op);
		this.where.append(" '");
		this.where.append(value);
		this.where.append("')");
		return this;
	}

	/**
	 * 带问号的
	 *
	 * @param column
	 * @param op
	 * @return
	 */
	public Where andOpArg(String column, String op, Object arg) {
		this.where.append(" AND (");
		this.where.append(column);
		this.where.append(" ");
		this.where.append(op);
		this.where.append(" ?)");
		this.addArg(arg);
		return this;
	}

	public Where andTrue(String column) {
		return andOp(column, "!=", 0);
	}

	public Where andNotNull(String column) {
		return and(column + " IS NOT NULL ");
	}

	public Where andIsNull(String column) {
		return and(column + " IS NULL ");
	}

	public ArrayList<Object> args() {
		return argList;
	}

	public String[] argsArray() {
		return Util.toStringArray(argList);
	}

	// protected void cleanWhere() {
	// where.clear();
	// argList.clear();
	// }

	public Where or(String condition) {
		this.where.append(" OR (");
		this.where.append(condition);
		this.where.append(") ");
		return this;
	}

	// key-value
	public Where orEq(String column, long value) {
		this.where.append(" OR (");
		this.where.append(column);
		this.where.append("=");
		this.where.append(value);
		this.where.append(") ");
		return this;
	}

	// key-value
	public Where orEq(String column, String value) {
		this.where.append(" OR (");
		this.where.append(column);
		this.where.append("='");
		this.where.append(value);
		this.where.append("') ");
		return this;
	}

	public Where orOpArg(String column, String op, Object arg) {
		this.where.append(" OR (");
		this.where.append(column);
		this.where.append(" ");
		this.where.append(op);
		this.where.append(" ?)");
		this.addArg(arg);
		return this;
	}

	@Override
	public String toString() {
		return where.toString();
	}

}
