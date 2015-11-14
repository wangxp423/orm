package net.yet.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by yangentao on 2015/11/14.
 * entaoyang@163.com
 */
public class Util {
	public static String toLogString(Object obj) {
		if (obj == null) {
			return "null";
		}
		if (obj instanceof String) {
			return (String) obj;
		}
		if (obj instanceof Long) {
			return obj.toString() + "L";
		}
		if (obj instanceof Float) {
			return obj.toString() + "F";
		}
		if (obj instanceof Throwable) {
			Throwable tr = (Throwable) obj;
			StringWriter sw = new StringWriter(256);
			PrintWriter pw = new PrintWriter(sw);
			tr.printStackTrace(pw);
			return sw.toString();
		}
		if (obj.getClass().isArray()) {
			int len = Array.getLength(obj);
			StringBuilder sb = new StringBuilder(100);
			sb.append("[");
			for (int i = 0; i < len; ++i) {
				if (i != 0) {
					sb.append(",");
				}
				Object ele = Array.get(obj, i);
				String s = toLogString(ele);
				sb.append(s);
			}
			sb.append("]");
			return sb.toString();
		}
		if (obj instanceof List<?>) {
			List<?> ls = (List<?>) obj;
			StringBuilder sb = new StringBuilder(256);
			sb.append("LIST[");
			for (int i = 0; i < ls.size(); ++i) {
				if (i != 0) {
					sb.append(",");
				}
				Object val = ls.get(i);
				String s = toLogString(val);
				sb.append(s);
			}
			sb.append("]");
			return sb.toString();
		}
		if (obj instanceof Map<?, ?>) {
			StringBuilder sb = new StringBuilder(256);
			sb.append("{");
			@SuppressWarnings("rawtypes") Map map = (Map) obj;
			boolean first = true;
			for (Object key : map.keySet()) {
				if (!first) {
					sb.append(",");
				}
				first = false;
				Object value = map.get(key);
				sb.append(toLogString(key));
				sb.append("=");
				sb.append(toLogString(value));
			}
			sb.append("}");
			return sb.toString();
		}

		return obj.toString();
	}
	public static <T> String[] toStringArray(Collection<T> ls) {
		if (ls == null || ls.size() == 0) {
			return new String[0];
		}
		String[] arr = new String[ls.size()];
		int index = 0;
		for (T val : ls) {
			arr[index++] = val == null ? null : val.toString();
		}
		return arr;

	}
	public static boolean notEmpty(String s) {
		return !isEmpty(s);
	}
	public static boolean notEmpty(Collection<?> c) {
		return !isEmpty(c);
	}

	public static boolean isEmpty(String str) {
		return str == null || str.length() == 0;
	}

	public static boolean empty(String str) {
		return str == null || str.length() == 0;
	}

	public static boolean emptyTrim(String s) {
		return s == null || s.trim().length() == 0;
	}

	public static boolean isEmpty(Collection<?> list) {
		return list == null || list.isEmpty();
	}

	public static boolean empty(Collection<?> list) {
		return list == null || list.isEmpty();
	}

	public static boolean empty(Map<?, ?> map) {
		return map == null || map.size() == 0;
	}

	public static boolean notEmpty(Map<?, ?> map) {
		return !empty(map);
	}
	public static int trimLength(String s) {
		return s == null ? 0 : s.trim().length();
	}
}
