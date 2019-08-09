package com.github.sergueik.testng;
/**
 * Copyright 2019 Serguei Kouzmine
 */

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

import org.testng.IAttributes;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.TestRunner;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import java.lang.reflect.Parameter;
// import org.testng.internal.reflect.Parameter;
// https://www.programcreek.com/java-api-examples/org.testng.Assert
import org.testng.Assert;

//NOTE: needed to switch to hamcrest-all.jar and Matchers 
//just for resolving method 'containsInAnyOrder'
import static org.hamcrest.Matchers.*;

import static org.hamcrest.MatcherAssert.assertThat;

public class CommonTest {

	// NOTE: cannot change signature of the method to include annotation:
	// handleTestMethodInformation(final ITestContext context, final Method
	// method, IDataProviderAnnotation annotation )
	// runtime TestNGException:
	// Method handleTestMethodInformation requires 3 parameters but 0 were
	// supplied in the @Configuration annotation.
	@BeforeMethod
	public void handleTestMethodInformation(final ITestContext context,
			final Method method) {
		final String suiteName = context.getCurrentXmlTest().getSuite().getName();
		final String methodName = method.getName();
		final String testName = context.getCurrentXmlTest().getName();

		System.err.println("BeforeMethod: " + "\t" + "Suite: " + suiteName + "\t"
				+ "Test: " + testName + "\t" + "Method: " + methodName);
		// String dataProvider = ((IDataProvidable)annotation).getDataProvider();
		// System.err.println("Data Provider: " + dataProvider);
		@SuppressWarnings("deprecation")
		final Map<String, String> parameters = (((TestRunner) context).getTest())
				.getParameters();
		final Set<String> keys = parameters.keySet();
		System.err.print("BeforeMethod Parameters:");
		for (String key : keys) {
			System.err.print("\t" + key + " = " + parameters.get(key));
		}
		System.err.println("");
		final Set<String> attributeNames = ((IAttributes) context)
				.getAttributeNames();
		if (attributeNames.size() > 0) {
			for (String attributeName : attributeNames) {
				System.err.print("BeforeMethod Attribute: " + attributeName + " = "
						+ ((IAttributes) context).getAttribute(attributeName));
			}
		}
		// not useful: produces
		// Method testIndex Parameters:Method testIndex Parameters:
		// arg0 = java.lang.Object arg0
		// arg1 = java.lang.Object arg1
		// ..
		// those are obtained via reflection from method signature:
		// private void dataTest(String keyword, String strCount)
		/*
		final Parameter[] methodParameters = method.getParameters();
		if (methodParameters.length > 0) {
			System.err.print("Method " + method.getName() + " Parameters:");
			for (int cnt = 0; cnt != methodParameters.length; cnt++) {
				Parameter methodParameter = methodParameters[cnt];
				System.err.print("\t" + methodParameter.getName() + " = "
						+ methodParameter.toString());
			}
			System.err.println("");
		}
		*/
	}

	@AfterClass(alwaysRun = true)
	public void cleanupSuite() {
	}

	protected void dataTest(String keyword, String strCount) {
		Assert.assertNotNull(keyword);
		// System.err.println("verifying keyword: " + keyword);
		Assert.assertTrue(keyword.matches("(?:junit|testng|spock)"));
		double count = Double.valueOf(strCount);
		Assert.assertTrue((int) count > 0);
		System.err.println(
				String.format("Search keyword:'%s'\tExpected minimum link count: %s",
						keyword, strCount));
	}

	protected void dataTestWithMethod(Method method, String keyword,
			double count) {
		System.err.println("Method name: " + method.getName() + " Parameter count: "
				+ method.getParameterCount());

		Test testMethodTestAnnotation = method.getAnnotation(Test.class);
		String dataProviderName = testMethodTestAnnotation.dataProvider();
		if (dataProviderName != null && !dataProviderName.isEmpty()) {
			System.err.println("Method name: " + method.getName()
					+ " DataProvider name: " + dataProviderName);
			DataProvider dataProviderAnnotation = method
					.getAnnotation(DataProvider.class);
			if (dataProviderAnnotation != null) {
				String thisDataProviderName = dataProviderAnnotation.name();
				System.err.println("Method name: " + method.getName()
						+ " DataProvider name: " + thisDataProviderName + " "
						+ dataProviderAnnotation.toString());
			}
		}
		dataTest(keyword, count);
	}

	protected void dataTest(String keyword, double count) {
		Assert.assertNotNull(keyword);
		System.err.println("keyword: " + keyword);
		// NOTE: remove one of the alternatives e.g. the "whatever" to trigger
		// assertionError
		Assert.assertTrue(keyword.matches("(?:junit|testng|spock|whatever)"));
		/*
		Object[] expected = new Object[] { "junit", "testng", "spock" };
		HashSet<Object> resultHashset = new HashSet<Object>();
		resultHashset.add(keyword);
		assertThat(resultHashset, containsInAnyOrder(expected));
		*/
		// NOTE: remove one of the alternatives e.g. the "whatever" to trigger
		// assertionError
		assertThat(keyword,
				org.hamcrest.Matchers.isOneOf("junit", "testng", "spock", "whatever"));
		// NOTE: remove one of the alternatives e.g. the "whatever" to trigger
		// assertionError

		// NOTE: change to greater to trigger assertionError
		Assert.assertTrue(((int) count >= 0));
		System.err.println(
				String.format("Search keyword:'%s'\tExpected minimum link count:%d",
						keyword, (int) count));
	}

	// based on: https://gist.github.com/ae6rt/3805639
	// @Override
	public void onTestStart(ITestResult iTestResult) {
		// Attempt to count invocations of a DataProvider-instrumented test
		Object instance = iTestResult.getInstance();
		ITestNGMethod testNGMethod = iTestResult.getMethod();
		Method testMethod = testNGMethod.getMethod();
		if (testMethod.isAnnotationPresent(Test.class)
		/*
		&& testMethod.isAnnotationPresent(Count.class)*/) {
			Test testMethodTestAnnotation = testMethod.getAnnotation(Test.class);
			String dataProviderName = testMethodTestAnnotation.dataProvider();
			if (dataProviderName != null && !dataProviderName.isEmpty()) {
				Class<?> aClass = instance.getClass();
				Method[] allTestClassMethods = aClass.getMethods();
				for (Method m : allTestClassMethods) {
					/*
					Counting will silently fail for Test classes using a DataProvider defined outside the test class instance itself.
					The reason is that the following code does not look outside the test class instance for the DataProvider method.
					 */
					if (m.isAnnotationPresent(DataProvider.class)) {
						DataProvider dataProviderAnnotation = m
								.getAnnotation(DataProvider.class);
						String thisDataProviderName = dataProviderAnnotation.name();
						if (dataProviderName.equals(thisDataProviderName)) {
							try {
								Object[][] theData = (Object[][]) m.invoke(instance);
								Integer numberOfDataProviderRows = theData.length;
								System.out.printf("Executing %s %d / %d\n",
										iTestResult.getName(),
										testNGMethod.getCurrentInvocationCount() + 1,
										numberOfDataProviderRows);
							} catch (IllegalAccessException e) {
								e.printStackTrace();
							} catch (InvocationTargetException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		}
	}

}
