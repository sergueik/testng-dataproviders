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
public @interface DataFileParameters {
	String name();
	String path() default "";
	// NOTE:      element value must be a constant expression
	// test method cannot use class variables or static methods like File.separator in annotation value:

	boolean debug() default false;
	boolean loadEmptyColumns() default false;
	String encoding() default "UTF-8";
	String sheetName() default "test";
	String controlColumn() default "";
	String withValue() default "";
}
