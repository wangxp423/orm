package net.yet.orm;

import net.yet.orm.annotation.Exclude;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashSet;

public final class ReflectionUtils {

	public static LinkedHashSet<Field> getDeclaredColumnFields(Class<?> type) {
		LinkedHashSet<Field> fieldSet = new LinkedHashSet<Field>(8);
		Field[] fields = type.getDeclaredFields();
		for (Field field : fields) {
			int mod = field.getModifiers();
			if (Modifier.isStatic(mod) || Modifier.isFinal(mod) || Modifier.isTransient(mod)) {
				continue;
			}
			if (field.isAnnotationPresent(Exclude.class)) {
				continue;
			}
			fieldSet.add(field);
		}
		Class<?> parentType = type.getSuperclass();
		if (parentType != null) {
			fieldSet.addAll(getDeclaredColumnFields(parentType));
		}
		return fieldSet;
	}

	public static boolean isSubclassOf(Class<?> type, Class<?> superClass) {
		if (type.getSuperclass() != null) {
			if (type.getSuperclass().equals(superClass)) {
				return true;
			}
			return isSubclassOf(type.getSuperclass(), superClass);
		}
		return false;
	}
}