package com.github.sergueik.testng;
/**
 * Copyright 2019 Serguei Kouzmine
 */

import java.lang.reflect.Method;

import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;
import org.testng.SkipException;

/**
 * Exercise popular practice of conditionally disabling the testng units (most frequentl methods)
 * through the class annotation interface syntax sugar
 * for data provider compatibility
 * based on: https://www.lenar.io/skip-testng-tests-based-condition-using-iinvokedmethodlistener/
 * no known github location
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

// added temporarily for testing only - pending removal
public class ConditionalMethodAnnotationListener
		implements IInvokedMethodListener {
	public void beforeInvocation(IInvokedMethod invokedMethod,
			ITestResult result) {
		Method method = result.getMethod().getConstructorOrMethod().getMethod();
		if (method == null) {
			return;
		}
		if (method.isAnnotationPresent(WindowsOnly.class)
				&& !getPropertyEnv("OS", "").matches("Windows_NT")) {
			throw new SkipException("These Tests should be run in Production only");
		}
		return;
	}

	public static String getPropertyEnv(String name, String defaultValue) {
		String value = System.getProperty(name);
		if (value == null) {
			value = System.getenv(name);
			if (value == null) {
				value = defaultValue;
			}
		}
		return value;
	}

	public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
	}
	
	
}