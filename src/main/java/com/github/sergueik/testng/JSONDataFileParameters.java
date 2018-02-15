package com.github.sergueik.testng;
/**
 * Copyright 2017 Serguei Kouzmine
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface JSONDataFileParameters {

	String name();
	String path() default "";
	String encoding() default "UTF-8";
	String dataKey() default "test";
	String columns(); // cannot be null

}
