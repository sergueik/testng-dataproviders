package com.github.sergueik.testng;
/**
 * Copyright 2017 Serguei Kouzmine
 */

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import org.testng.IAttributes;
import org.testng.ITestContext;
import org.testng.TestRunner;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import org.testng.Assert;

public class TestNgDataProviderTest {

	@Test(enabled = true, singleThreaded = true, threadPoolSize = 1, invocationCount = 1, description = "# of articless for specific keyword", dataProvider = "Excel 2003", dataProviderClass = ExcelParametersProvider.class)
	@DataFileParameters(name = "data_2003.xls", path = "${USERPROFILE}\\Desktop", sheetName = "Employee Data")
	public void test_with_Excel_2003(double rowNum, String searchKeyword,
			double expectedCount) throws InterruptedException {
		// parseSearchResult(searchKeyword, expectedCount);

		dataTest(searchKeyword, expectedCount);

	}

	@Test(enabled = true, singleThreaded = true, threadPoolSize = 1, invocationCount = 1, description = "# of articless for specific keyword", dataProvider = "OpenOffice Spreadsheet", dataProviderClass = ExcelParametersProvider.class)
	@DataFileParameters(name = "data.ods", path = ".")
	public void test_with_OpenOffice_Spreadsheet(double rowNum,
			String searchKeyword, double expectedCount) throws InterruptedException {
		dataTest(searchKeyword, expectedCount);
	}

	@Test(enabled = true, singleThreaded = true, threadPoolSize = 1, invocationCount = 1, description = "# of articless for specific keyword", dataProvider = "Excel 2007", dataProviderClass = ExcelParametersProvider.class)
	@DataFileParameters(name = "data_2007.xlsx", path = ".", sheetName = "Employee Data")
	public void test_with_Excel_2007(double rowNum, String searchKeyword,
			double expectedCount) throws InterruptedException {
		dataTest(searchKeyword, expectedCount);
	}

	@Test(enabled = true, singleThreaded = false, threadPoolSize = 1, invocationCount = 1, description = "# of articless for specific keyword", dataProvider = "JSON", dataProviderClass = JSONParametersProvider.class)
	@JSONDataFileParameters(name = "data.json", dataKey = "test", columns = "keyword,count"
	/* columns attribute should not be empty */)
	public void test_with_JSON(String expectedCount, String searchKeyword)
			throws InterruptedException {
		dataTest(searchKeyword, expectedCount);

	}

	@Test(enabled = true, singleThreaded = false, threadPoolSize = 1, invocationCount = 1, description = "# of articless for specific keyword", dataProvider = "csv", dataProviderClass = CSVParametersProvider.class)
	@DataFileParameters(name = "data.csv", path = ".")
	public void test1_csv(String... args) {
		System.err.println(StringUtils.join(args, ","));
		System.err.println(String.join(",", (CharSequence[]) args));
		System.err.println(String.format("Keyword: %s Count : %s", args[1],
				Double.valueOf(args[2])));

	}

	@Test(enabled = true, singleThreaded = false, threadPoolSize = 1, invocationCount = 1, description = "# of articless for specific keyword", dataProvider = "csv", dataProviderClass = CSVParametersProvider.class)
	@DataFileParameters(name = "data.csv", path = "")
	public void test2_csv(String column1, String column2, String column3)
			throws InterruptedException {
		System.err.println(column1 + " " + column2 + " " + column3);
	}

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

		System.err.println("BeforeMethod Suite: " + suiteName);
		System.err.println("BeforeMethod Test: " + testName);
		System.err.println("BeforeMethod Method: " + methodName);
		// String dataProvider = ((IDataProvidable)annotation).getDataProvider();
		// System.err.println("Data Provider: " + dataProvider);
		@SuppressWarnings("deprecation")
		final Map<String, String> parameters = (((TestRunner) context).getTest())
				.getParameters();
		final Set<String> keys = parameters.keySet();
		for (String key : keys) {
			System.out.println(
					"BeforeMethod Parameter: " + key + " = " + parameters.get(key));
		}
		final Set<java.lang.String> attributeNames = ((IAttributes) context)
				.getAttributeNames();
		if (attributeNames.size() > 0) {
			for (String attributeName : attributeNames) {
				System.out.print("BeforeMethod Attribute: " + attributeName + " = "
						+ ((IAttributes) context).getAttribute(attributeName));
			}
		}
	}

	@AfterClass(alwaysRun = true)
	public void cleanupSuite() {
	}

	// static disconnected data provider
	@DataProvider(parallel = true)
	public Object[][] dataProviderInline() {
		return new Object[][] { { "junit", 100.0 }, { "testng", 30.0 },
				{ "spock", 10.0 }, };
	}

	private void dataTest(String keyword, double count) {
		Assert.assertNotNull(keyword);
		Assert.assertTrue(keyword.matches("(?:junit|testng|spock)"));
		Assert.assertTrue(((int) count > 0));
		System.err.println(
				String.format("Search keyword:'%s'\tExpected minimum link count:%d",
						keyword, (int) count));
	}

	private void dataTest(String keyword, String strCount) {
		Assert.assertNotNull(keyword);
		Assert.assertTrue(keyword.matches("(?:junit|testng|spock)"));
		double count = Double.valueOf(strCount);
		Assert.assertTrue(((int) count > 0));
		System.err.println(
				String.format("Search keyword:'%s'\tExpected minimum link count: %s",
						keyword, strCount));
	}

}
