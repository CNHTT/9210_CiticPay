package com.android.citic.appmanager.master;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 导航条的资源配置，标题、左边图标、右边图标、mask
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface NavigationConfig {

	int leftIconId() default -1;

	int rightIconId() default -1;

	/**
	 * resource id
	 * @return
	 */
	int titleId() default -1;

	/**
	 * 普通String
	 */
	String titleValue() default "";

	String mask() default "";
	
}
