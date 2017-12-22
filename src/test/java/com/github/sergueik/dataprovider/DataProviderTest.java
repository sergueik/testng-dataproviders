package com.github.sergueik.dataprovider;
/**
 * Copyright 2017 Serguei Kouzmine
 */

import java.lang.reflect.Method;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import org.testng.IAttributes;
import org.testng.ITestContext;
import org.testng.TestRunner;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.Assert.assertTrue;

import org.apache.commons.lang3.StringUtils;

public class DataProviderTest extends BaseTest {

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

	@Test(enabled = true, singleThreaded = false, threadPoolSize = 1, invocationCount = 1, description = "# of articless for specific keyword", dataProvider = "Excel 2003", dataProviderClass = ExcelParametersProvider.class)
	@DataFileParameters(name = "data_2003.xls", path = "${USERPROFILE}\\Desktop", sheetName = "Employee Data")
	public void test_with_Excel_2003(double rowNum, String searchKeyword,
			double expectedCount) throws InterruptedException {
		parseSearchResult(searchKeyword, expectedCount);
	}

	@Test(enabled = true, singleThreaded = false, threadPoolSize = 1, invocationCount = 1, description = "# of articless for specific keyword", dataProvider = "OpenOffice Spreadsheet", dataProviderClass = ExcelParametersProvider.class)
	@DataFileParameters(name = "data.ods", path = ".")
	public void test_with_OpenOffice_Spreadsheet(double rowNum,
			String searchKeyword, double expectedCount) throws InterruptedException {
		if (env.containsKey("TRAVIS") && env.get("TRAVIS").equals("true")) {
			// temporarily stub under Travis
			System.err.println(String.format("Keyword: %s Count : %s", searchKeyword,
					expectedCount));
		} else {
			parseSearchResult(searchKeyword, expectedCount);
		}
	}

	@Test(enabled = true, singleThreaded = false, threadPoolSize = 1, invocationCount = 1, description = "# of articless for specific keyword", dataProvider = "Excel 2007", dataProviderClass = ExcelParametersProvider.class)
	@DataFileParameters(name = "data_2007.xlsx", path = ".", sheetName = "Employee Data")
	public void test_with_Excel_2007(double rowNum, String searchKeyword,
			double expectedCount) throws InterruptedException {
		if (env.containsKey("TRAVIS") && env.get("TRAVIS").equals("true")) {
			// temporarily stub under Travis
			System.err.println(String.format("Keyword: %s Count : %s", searchKeyword,
					expectedCount));
		} else {
			parseSearchResult(searchKeyword, expectedCount);
		}
	}

	@Test(enabled = true, singleThreaded = false, threadPoolSize = 1, invocationCount = 1, description = "# of articless for specific keyword", dataProvider = "JSON", dataProviderClass = JSONParametersProvider.class)
	@DataFileParameters(name = "data.json")
	public void test_with_JSON(String strCount, String strKeyword)
			throws InterruptedException {

		if (env.containsKey("TRAVIS") && env.get("TRAVIS").equals("true")) {
			// temporarily stub under Travis
			System.err.println(
					String.format("Keyword: %s Count : %s", strKeyword, strCount));

		} else {
			parseSearchResult(strKeyword, Double.valueOf(strCount));
		}
	}

	@Test(enabled = true, singleThreaded = false, threadPoolSize = 1, invocationCount = 1, description = "# of articless for specific keyword", dataProvider = "csv", dataProviderClass = CSVParametersProvider.class)
	@DataFileParameters(name = "data.csv", path = ".")
	public void test1_csv(String... args) {
		if (env.containsKey("TRAVIS") && env.get("TRAVIS").equals("true")) {
			// temporarily stub under Travis
			System.err.println(StringUtils.join(args, ","));
			System.err.println(String.join(",", (CharSequence[]) args));
		} else {
			try {
				parseSearchResult(args[1], Double.valueOf(args[2]));
			} catch (InterruptedException e) {
			}
		}
	}

	@Test(enabled = true, singleThreaded = false, threadPoolSize = 1, invocationCount = 1, description = "# of articless for specific keyword", dataProvider = "csv", dataProviderClass = CSVParametersProvider.class)
	@DataFileParameters(name = "data.csv", path = "")
	public void test2_csv(String column1, String column2, String column3)
			throws InterruptedException {
		// System.err.println(column1 + " " + column2 + " " + column3);
		try {
			parseSearchResult(column2, Double.valueOf(column3));
		} catch (InterruptedException e) {
		}
	}

	@AfterClass(alwaysRun = true)
	public void cleanupSuite() {
		if (driver != null) {
			driver.close();
			driver.quit();
		}
	}

	// static disconnected data provider
	@DataProvider(parallel = true)
	public Object[][] dataProviderInline() {
		return new Object[][] { { "junit", 100.0 }, { "testng", 30.0 },
				{ "spock", 10.0 }, };
	}

}
