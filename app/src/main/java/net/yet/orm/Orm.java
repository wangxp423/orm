package net.yet.orm;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;

import net.yet.orm.serial.TypeSerializer;
import net.yet.util.JsonUtil;
import net.yet.util.Util;
import net.yet.util.db.DBQuery;
import net.yet.util.db.Where;

import java.util.List;

/**
 * model类必须声明一个主键, 可以是字符串或数值类型.
 * <red>自增只在int和long类型的主键值是0的时候有效</red>
 * <p/>
 * 不支持外键. 主要是单表操作
 * 在使用之前, 在Application的onCreate中调用
 * Orm.init(application, 1, null);
 * Orm.finish();
 * <p/>
 * 不想被存储的变量, 可以使用final,transient来修饰, 或者使用Exclude注释
 * <p/>
 * Created by yangentao on 2015/11/7.
 * entaoyang@163.com
 */
public class Orm {

	private static DbHelper dbHelper;

	public static void init(Context context, int version, final UpgradeCallback callback) {
		dbHelper = new DbHelper(context, "orm.db", version) {
			@Override
			public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
				if (callback != null) {
					callback.onUpgrade(db, oldVersion, newVersion);
				}
			}
		};
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			dbHelper.getWritableDatabase().setForeignKeyConstraintsEnabled(false);
		}
	}

	public static void finish() {
		dbHelper.close();
	}

	public static void addTypeSerializer(Class<?> fieldType, TypeSerializer typeSerializer) {
		TableInfo.addTypeSerializer(fieldType, typeSerializer);
	}

	public static SQLiteDatabase database() {
		return dbHelper.getWritableDatabase();
	}

	public static void beginTransaction() {
		database().beginTransaction();
	}

	public static void endTransaction() {
		database().endTransaction();
	}

	public static void setTransactionSuccessful() {
		database().setTransactionSuccessful();
	}

	public static boolean inTransaction() {
		return database().inTransaction();
	}

	public static void execSQL(String sql) {
		database().execSQL(sql);
		Log.d("orm", sql);
	}

	public static void execSQL(String sql, Object... bindArgs) {
		database().execSQL(sql, bindArgs);
		Log.d("orm", sql + " " + Util.toLogString(bindArgs));
	}

	public static int delete(String table, String where, String... args) {
		return database().delete(table, where, args);
	}

	public static long insert(String table, ContentValues values) {
		try {
			return database().insertOrThrow(table, null, values);
		} catch (SQLException e) {
			e.printStackTrace();
			Log.e("orm", Util.toLogString(e));
			return -1;
		}
	}

	public static long replace(String table, ContentValues values) {
		return database().replace(table, null, values);
	}

	public static int update(String table, ContentValues values, String whereClause, String... whereArgs) {
		return database().update(table, values, whereClause, whereArgs);
	}

	public static Cursor query(String sql, String... args) {
		Log.e("orm", sql + "  " + Util.toLogString(args));
		return database().rawQuery(sql, args);
	}

	public static <T> List<T> querySql(Class<T> modelCls, String sql, String... args) {
		Cursor cursor = query(sql, args);
		return ModelUtil.parseCursorAll(TableInfo.findTable(modelCls), cursor);
	}

	public static String tableName(Class<?> modelClass) {
		TableInfo info = TableInfo.findTable(modelClass);
		return info.tableName;
	}

	public static boolean existTable(String tableName) {
		return DBQuery.existTable(database(), tableName);
	}

	public static void dropTable(String tableName) {
		execSQL("DROP TABLE IF EXISTS " + tableName);
	}

	public static void dropTable(Class<?> cls) {
		dropTable(TableInfo.findTable(cls).tableName);
	}

	public static void createTable(Class<?> cls) {
		TableDefUtil.createTable(TableInfo.findTable(cls));
	}

	/**
	 * model主键如果是int/long的自增类型, 值是0时, 会自动生成主键值.
	 * 其他类型的主键, 或值不是0的时候, 不会执行自增.
	 *
	 * @param model
	 * @return
	 */
	public static long insert(Object model) {
		return ModelUtil.insert(model);
	}

	public static long insert(Class<?> cls, ContentValues values) {
		return insert(TableInfo.findTable(cls).tableName, values);
	}

	/**
	 * model主键必须有值才行
	 *
	 * @param model
	 * @return
	 */
	public static int updateByPk(Object model) {
		return ModelUtil.updateByPk(model);
	}

	public static int updateByRowId(Object model, long rowid) {
		return ModelUtil.updateByRowId(model, rowid);
	}

	public static int updateWhere(Object model, String where, String... args) {
		return ModelUtil.updateWhere(model, where, args);
	}

	public static int update(Class<?> cls, ContentValues values, String where, String... args) {
		return update(TableInfo.findTable(cls).tableName, values, where, args);
	}

	public static int deleteByPk(Object model) {
		if (model != null) {
			TableInfo tableInfo = TableInfo.findTable(model.getClass());
			Object pkVal = ModelUtil.getPkSqliteValue(tableInfo, model);
			if (pkVal != null) {
				return delete(tableInfo.tableName, tableInfo.pkName + "=?", pkVal.toString());
			} else {
				//TODO by _id
			}
		}
		return 0;
	}
//TODO delete by _id;

	public static int deleteByPk(Class<?> modelClass, long pk) {
		TableInfo info = TableInfo.findTable(modelClass);
		return delete(info.tableName, info.pkName + "=" + pk);
	}

	public static int deleteByPk(Class<?> modelClass, String pk) {
		TableInfo info = TableInfo.findTable(modelClass);
		return delete(info.tableName, info.pkName + "=" + pk);
	}

	public static int delete(Class<?> modelClass, String where, String... args) {
		TableInfo info = TableInfo.findTable(modelClass);
		return delete(info.tableName, where, args);
	}

	public static int delete(Class<?> modelClass, Where w) {
		return delete(modelClass, w.toString(), w.argsArray());
	}

	public static int deleteEq(Class<?> modelClass, String key, String value) {
		return delete(modelClass, Where.eq(key, value));
	}

	public static int deleteEq(Class<?> modelClass, String key, long value) {
		return delete(modelClass, Where.eq(key, value));
	}

	public static <T> T findPk(Class<T> cls, long pk) {
		return select(cls).wherePk(pk).findOne();
	}

	public static <T> T findPk(Class<T> cls, String pk) {
		return select(cls).wherePk(pk).findOne();
	}

	public static <T> T findOne(Class<T> cls, Where w) {
		return select(cls).where(w).findOne();
	}

	public static <T> T findOne(Class<T> cls, String where, String... args) {
		return select(cls).where(where, args).findOne();
	}

	public static <T> T findOneEq(Class<T> cls, String key, String value) {
		return select(cls).whereEq(key, value).findOne();
	}

	public static <T> List<T> findAll(Class<T> cls) {
		return select(cls).findAll();
	}

	public static <T> List<T> findAll(Class<T> cls, Where w) {
		return select(cls).where(w).findAll();
	}

	public static <T> List<T> findAll(Class<T> cls, String where, String... args) {
		return select(cls).where(where, args).findAll();
	}

	public static <T> List<T> findAllEq(Class<T> cls, String key, String value) {
		return select(cls).whereEq(key, value).findAll();
	}

	public static OrmQuery select(Class<?> modelClass) {
		return OrmQuery.select(modelClass);
	}

	public static <T> void dumpTable(Class<T> cls) {
		List<T> ls = Orm.findAll(cls);
		for (T m : ls) {
			Log.e("orm", JsonUtil.toJson(m));
		}
	}

	public interface UpgradeCallback {
		void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);
	}

	private static abstract class DbHelper extends SQLiteOpenHelper {
		public DbHelper(Context context, String name, int version) {
			super(context, name, null, version);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {

		}

	}
}
