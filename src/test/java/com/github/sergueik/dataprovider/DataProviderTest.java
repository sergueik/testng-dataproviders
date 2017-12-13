package com.github.sergueik.dataprovider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import java.lang.reflect.Method;
import java.nio.charset.Charset;

import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import java.util.concurrent.TimeUnit;

import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
// OLE2 Office Documents
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
// conflicts with org.jopendocument.dom.spreadsheet.Cell;
// import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.util.CellReference;

// Office 2007+ XML
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

// open office
import org.jopendocument.dom.ODDocument;
import org.jopendocument.dom.ODPackage;
import org.jopendocument.dom.ODValueType;
import org.jopendocument.dom.spreadsheet.Cell;
import org.jopendocument.dom.spreadsheet.Sheet;
import org.jopendocument.dom.spreadsheet.SpreadSheet;

// NOTE: cannot import org.apache.poi.ss.usermodel.Cell:
// a type with the same simple name is already defined by the single-type-import of org.jopendocument.dom.spreadsheet.Cell
// import org.apache.poi.ss.usermodel.Cell;

// JSON
import org.json.JSONArray;
import org.json.JSONObject;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
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

public class DataProviderTest {

	public RemoteWebDriver driver = null;

	// for grid testing
	public String seleniumHost = null;
	public String seleniumPort = null;
	public String seleniumBrowser = null;

	public String baseUrl = "http://habrahabr.ru/search/?";

	public static final String TEST_ID_STR = "Row ID";
	public static final String TEST_EXPECTED_COUNT = "Expected minimum link count";
	public static final String TEST_DESC_STR = "Search keyword";

	private static long implicit_wait_interval = 3;
	private static int page_load_timeout_interval = 10;

	@BeforeClass(alwaysRun = true)
	public void setupBeforeClass(final ITestContext context)
			throws InterruptedException {

		DesiredCapabilities capabilities = DesiredCapabilities.chrome();
		LoggingPreferences logging_preferences = new LoggingPreferences();
		logging_preferences.enable(LogType.BROWSER, Level.ALL);
		capabilities.setCapability(CapabilityType.LOGGING_PREFS,
				logging_preferences);
		driver = new ChromeDriver(capabilities);
		try {
			driver.manage().window().setSize(new Dimension(600, 800));
			driver.manage().timeouts().pageLoadTimeout(page_load_timeout_interval,
					TimeUnit.SECONDS);
			driver.manage().timeouts().implicitlyWait(implicit_wait_interval,
					TimeUnit.SECONDS);
		} catch (Exception ex) {
			System.err.println(ex.toString());
		}
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
		String suiteName = context.getCurrentXmlTest().getSuite().getName();
		System.err.println("BeforeMethod Suite: " + suiteName);
		String testName = context.getCurrentXmlTest().getName();
		System.err.println("BeforeMethod Test: " + testName);
		String methodName = method.getName();
		System.err.println("BeforeMethod Method: " + methodName);
		// String dataProvider = ((IDataProvidable)annotation).getDataProvider();
		// System.err.println("Data Provider: " + dataProvider);
		@SuppressWarnings("deprecation")
		Map<String, String> parameters = (((TestRunner) context).getTest())
				.getParameters();
		Set<String> keys = parameters.keySet();
		for (String key : keys) {
			System.out.println(
					"BeforeMethod Parameter: " + key + " = " + parameters.get(key));
		}
		Set<java.lang.String> attributeNames = ((IAttributes) context)
				.getAttributeNames();
		if (attributeNames.size() > 0) {
			for (String attributeName : attributeNames) {
				System.out.print("BeforeMethod Attribute: " + attributeName + " = "
						+ ((IAttributes) context).getAttribute(attributeName));
			}
		}
	}

	// NOTE: sporadically fails with
	// Timeout in parseSearchResult when run together with other tests
	@Test(enabled = true, singleThreaded = false, threadPoolSize = 1, invocationCount = 1, description = "searches publications for a keyword", dataProvider = "Excel 2003", dataProviderClass = ExcelParametersProvider.class)
	public void test_with_Excel_2003(double rowNum, String search_keyword,
			double expected_count) throws InterruptedException {
		parseSearchResult(search_keyword, expected_count);
	}

	@Test(enabled = true, singleThreaded = false, threadPoolSize = 1, invocationCount = 1, description = "searches publications for a keyword", dataProvider = "OpenOffice Spreadsheet", dataProviderClass = ExcelParametersProvider.class)
	public void test_with_OpenOffice_Spreadsheet(double rowNum,
			String search_keyword, double expected_count)
			throws InterruptedException {
		parseSearchResult(search_keyword, expected_count);
	}

	@Test(enabled = true, singleThreaded = false, threadPoolSize = 1, invocationCount = 1, description = "searches publications for a keyword", dataProvider = "Excel 2007", dataProviderClass = ExcelParametersProvider.class)
	public void test_with_Excel_2007(double rowNum, String search_keyword,
			double expected_count) throws InterruptedException {
		parseSearchResult(search_keyword, expected_count);
	}

	@Test(enabled = true, singleThreaded = false, threadPoolSize = 1, invocationCount = 1, description = "searches publications for a keyword", dataProvider = "JSON")
	public void test_with_JSON(String strCount, String strKeyword)
			throws InterruptedException {
		double expected_count = Double.valueOf(strCount);
		parseSearchResult(strKeyword, expected_count);
	}

	@AfterClass(alwaysRun = true)
	public void cleanupSuite() {
		if (driver != null) {
			driver.close();
			driver.quit();
		}
	}

	private void parseSearchResult(String search_keyword, double expected_count)
			throws InterruptedException {
		driver.get(baseUrl);

		System.err.println(
				String.format("Search keyword:'%s'\tExpected minimum link count:%d",
						search_keyword, (int) expected_count));

		WebDriverWait wait = new WebDriverWait(driver, 30);
		String search_input_name = null;
		wait.until(ExpectedConditions
				.visibilityOfElementLocated(By.id("inner_search_form")));
		wait.until(ExpectedConditions
				.visibilityOfElementLocated(By.className("search-field__input")));
		search_input_name = "q";
		String search_input_xpath = String.format(
				"//form[@id='inner_search_form']/div[@class='search-field__wrap']/input[@name='%s']",
				search_input_name);
		wait.until(
				ExpectedConditions.elementToBeClickable(By.xpath(search_input_xpath)));
		WebElement element = driver.findElement(By.xpath(search_input_xpath));
		element.clear();
		element.sendKeys(search_keyword);
		element.sendKeys(Keys.RETURN);

		String pubsFoundCssSelector = "span[class*='tabs-menu__item-counter'][class*='tabs-menu__item-counter_total']";
		wait.until(ExpectedConditions
				.visibilityOfElementLocated(By.cssSelector(pubsFoundCssSelector)));
		element = driver.findElement(By.cssSelector(pubsFoundCssSelector));
		Pattern pattern = Pattern.compile("(\\d+)");
		Matcher matcher = pattern.matcher(element.getText());
		int publicationsFound = 0;
		if (matcher.find()) {
			publicationsFound = Integer.parseInt(matcher.group(1));
			System.err.println("Publication count " + publicationsFound);
		} else {
			System.err.println("No publications");
		}
		assertTrue(publicationsFound >= expected_count);
	}

	// static disconnected data provider
	@DataProvider(parallel = true)
	public Object[][] dataProviderInline() {
		return new Object[][] { { "junit", 100.0 }, { "testng", 30.0 },
				{ "spock", 10.0 }, };
	}

	@DataProvider(parallel = false, name = "JSON")
	public Object[][] createData_from_JSON(final ITestContext context,
			final Method method) throws org.json.JSONException {

		String fileName = "data.json";
		String testName = "test";
		Boolean debug = true;

		List<String> columns = new ArrayList<>();

		JSONObject obj = new JSONObject();
		List<Object[]> testData = new ArrayList<>();
		List<Object> testDataRow = new LinkedList<>();
		List<String> hashes = new ArrayList<>();

		JSONArray rows = new JSONArray();

		try {
			byte[] encoded = Files.readAllBytes(Paths.get(fileName));
			obj = new JSONObject(new String(encoded, Charset.forName("UTF-8")));
		} catch (org.json.JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		assertTrue(obj.has(testName));
		String dataString = obj.getString(testName);

		// possible org.json.JSONException
		try {
			rows = new JSONArray(dataString);
		} catch (org.json.JSONException e) {
			e.printStackTrace();
		}
		for (int i = 0; i < rows.length(); i++) {
			String entry = rows.getString(i);
			hashes.add(entry);
		}
		assertTrue(hashes.size() > 0);

		String firstRow = hashes.get(0);

		// NOTE: apparently after invoking org.json.JSON library the order of keys
		// inside the firstRow will be non-deterministic
		// https://stackoverflow.com/questions/4515676/keep-the-order-of-the-json-keys-during-json-conversion-to-csv
		firstRow = firstRow.replaceAll("\n", " ").substring(1,
				firstRow.length() - 1);
		if (debug)
			System.err.println("row: " + firstRow);

		String[] pairs = firstRow.split(",");

		for (String pair : pairs) {
			String[] values = pair.split(":");

			String column = values[0].substring(1, values[0].length() - 1).trim();
			if (debug) {
				System.err.println("column: " + column);
			}
			columns.add(column);
		}

		for (String entry : hashes) {
			JSONObject entryObj = new JSONObject();
			testDataRow = new LinkedList<>();
			try {
				entryObj = new JSONObject(entry);
			} catch (org.json.JSONException e) {
				e.printStackTrace();
			}
			for (String column : columns) {
				testDataRow.add(entryObj.get(column).toString());
			}
			testData.add(testDataRow.toArray());

			/*
			@SuppressWarnings("unchecked")
			Iterator<String> entryKeyIterator = entryObj.keys();
			
			while (entryKeyIterator.hasNext()) {
				String entryKey = entryKeyIterator.next();
				String entryData = entryObj.get(entryKey).toString();
				// System.err.println(entryKey + " = " + entryData);
				switch (entryKey) {
				case "keyword":
					search_keyword = entryData;
					break;
				case "count":
					expected_count = Double.valueOf(entryData);
					break;
				}
			}
			testData.add(new Object[] { search_keyword, expected_count });
			*/
		}

		Object[][] testDataArray = new Object[testData.size()][];
		testData.toArray(testDataArray);
		return testDataArray;
	}

	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface DataFileParameters {
		String path();

		String encoding() default "UTF-8";
	}

	@Test(singleThreaded = false, threadPoolSize = 1, invocationCount = 1, description = "searches publications for a keyword", dataProvider = "csv")
	@DataFileParameters(path = "data.csv")
	public void testSomething(Object... args) {
		// ...
	}

	@DataProvider(parallel = false, name = "csv")
	public String[][] createData_from_csv(final ITestContext context,
			final Method method) {
		Scanner scanner = null;
		List<String[]> testData = new ArrayList<>();
		String[] data = null;
		String separator = "|";
		String fileName = null;
		String encoding = null;
		DataFileParameters parameters = method
				.getAnnotation(DataFileParameters.class);
		if (parameters != null) {

			fileName = parameters.path();
			encoding = parameters.encoding();
		} else {
			throw new RuntimeException("Missing DataFileParameters annotation");
		}
		System.err
				.println(String.format("Reading configuration file: '%s'", fileName));
		try {
			scanner = new Scanner(new File(fileName));
			while (scanner.hasNext()) {
				String line = scanner.next();
				data = line.split(Pattern.compile("(\\||\\|/)").matcher(separator)
						.replaceAll("\\\\$1"));
				testData.add(data);
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			System.err.println(String.format("File was not found: '%s'", fileName));
			e.printStackTrace();
		}
		String[][] testDataArray = new String[testData.size()][];
		testData.toArray(testDataArray);
		return testDataArray;
	}

	static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}

}
