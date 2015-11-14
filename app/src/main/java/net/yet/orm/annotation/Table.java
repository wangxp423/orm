package net.yet.orm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Table {
	String name() default "";

	/**
	 * 自动根据后添加model增加的成员alter数据库表, 只处理新增的成员
	 * @return
	 */
	boolean autoAddField() default true;
}
