package com.github.sergueik.dataprovider;

import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
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
	public static final String TEST_EXPECTED_COUNT = "Link #";
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
	@Test(enabled = false, singleThreaded = false, threadPoolSize = 1, invocationCount = 1, description = "# of articless for specific keyword", dataProvider = "Excel 2003", dataProviderClass = ExcelParametersProvider.class)
	public void test_with_Excel_2003(double rowNum, String search_keyword,
			double expected_count) throws InterruptedException {
		parseSearchResult(search_keyword, expected_count);
	}

	@Test(enabled = false, singleThreaded = false, threadPoolSize = 1, invocationCount = 1, description = "# of articless for specific keyword", dataProvider = "OpenOffice Spreadsheet", dataProviderClass = ExcelParametersProvider.class)
	public void test_with_OpenOffice_Spreadsheet(double rowNum,
			String search_keyword, double expected_count)
			throws InterruptedException {
		parseSearchResult(search_keyword, expected_count);
	}

	@Test(enabled = false, singleThreaded = false, threadPoolSize = 1, invocationCount = 1, description = "# of articless for specific keyword", dataProvider = "Excel 2007", dataProviderClass = ExcelParametersProvider.class)
	public void test_with_Excel_2007(double rowNum, String search_keyword,
			double expected_count) throws InterruptedException {
		parseSearchResult(search_keyword, expected_count);
	}

	@Test(enabled = false, singleThreaded = false, threadPoolSize = 1, invocationCount = 1, description = "# of articless for specific keyword", dataProvider = "JSON", dataProviderClass = JSONParametersProvider.class)
	public void test_with_JSON(String strCount, String strKeyword)
			throws InterruptedException {
		/*	System.err
			.println(String.format("Keyword: %s Count : %s", strKeyword, strCount));
		*/
		double expected_count = Double.valueOf(strCount);
		parseSearchResult(strKeyword, expected_count);
	}

	@Test(enabled = true, singleThreaded = false, threadPoolSize = 1, invocationCount = 1, description = "# of articless for specific keyword", dataProvider = "csv", dataProviderClass = CSVParametersProvider.class)
	@DataFileParameters(name = "data.csv", path = ".")
	public void test1_csv(String... args) {
		/*
		System.err.println(StringUtils.join(args, ","));
		System.err.println(String.join(",", (CharSequence[]) args));
		*/
		try {
			parseSearchResult(args[1], Double.valueOf(args[2]));
		} catch (InterruptedException e) {
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

	private void parseSearchResult(String search_keyword, double expected_count)
			throws InterruptedException {
		driver.get(baseUrl);

		System.err.println(String.format("Search keyword:'%s'\tLink #:%d",
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
			System.err.println("# of publications " + publicationsFound);
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

}
