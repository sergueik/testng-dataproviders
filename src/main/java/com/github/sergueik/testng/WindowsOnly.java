package com.github.sergueik.testng;
/**
 * Copyright 2019 Serguei Kouzmine
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

// aded temporarily for testing only - pending removal
// base on: https://www.lenar.io/skip-testng-tests-based-condition-using-iinvokedmethodlistener/
// no known github location

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface WindowsOnly {

}
