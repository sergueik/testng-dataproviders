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

import io.github.bonigarcia.wdm.Architecture;
import io.github.bonigarcia.wdm.BrowserManager;
import io.github.bonigarcia.wdm.Downloader;
import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.FirefoxDriverManager;
import io.github.bonigarcia.wdm.ChromeDriverManager;

public class BaseTest {

	public WebDriver driver = null;
	private WebDriverWait wait;

	// for grid testing
	public String seleniumHost = null;
	public String seleniumPort = null;
	public String seleniumBrowser = null;

	protected static Map<String, String> env = System.getenv();
	public String baseUrl = "http://habrahabr.ru/search/?";
	private String searchInputSelector = "form[id='inner_search_form'] div[class='search-field__wrap'] input[name='q']";
	
	// NOTE: xpath does not work well with CDP
	private String searchInputXPath = "//form[@id='inner_search_form']/div[@class='search-field__wrap']/input[@name='q']";
	private String pubsCountSelector = "span[class*='tabs-menu__item-counter'][class*='tabs-menu__item-counter_total']";

	private static Pattern pattern;
	private static Matcher matcher;

	public static final String TEST_ID_STR = "Row ID";
	public static final String TEST_expectedCount = "Link #";
	public static final String TEST_DESC_STR = "Search keyword";

	private static long implicit_wait_interval = 3;
	private static int page_load_timeout_interval = 30;

	// NOTE: Firefox sporadically fails with
	// org.openqa.selenium.WebDriverException:
	// Timed out waiting 45 seconds for Firefox to start.
	// when all tests are enabled.
	// This has nothing to do with parameter provider:
	// https://github.com/SeleniumHQ/selenium/issues/5056
	// trying to start too new Firefox version that can't run in legacy mode
	// (marionette=false)

	@BeforeClass(alwaysRun = true)
	public void setupBeforeClass(final ITestContext context)
			throws InterruptedException {
		if (env.containsKey("TRAVIS") && env.get("TRAVIS").equals("true")) {
			// use DriverManager under Travis
			// https://github.com/bonigarcia/webdrivermanager-examples
			ChromeDriverManager.getInstance().setup();
			driver = new ChromeDriver();
		} else {
			// https://github.com/eviltester/selenium-driver-manager-example/blob/master/pom.xml
			DesiredCapabilities capabilities = DesiredCapabilities.firefox();
			// only set when supported
			// org.openqa.selenium.WebDriverException: WebDriver Missing
			// 'marionetteProtocol'
			capabilities.setCapability("marionette", false);
			LoggingPreferences logging_preferences = new LoggingPreferences();
			logging_preferences.enable(LogType.BROWSER, Level.ALL);
			capabilities.setCapability(CapabilityType.LOGGING_PREFS,
					logging_preferences);
			driver = new FirefoxDriver(capabilities);
		}
		wait = new WebDriverWait(driver, 30);
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
		if (driver != null) {
			driver.close();
			driver.quit();
		}
	}

	// CDP (WIP)
	/*
	private int searchTimeout = 10000;
		
	private void parseSearchResult(String searchKeyword, double expectedCount) {
		session.navigate(baseUrl);
	
		System.err
				.println(String.format("Search keyword: \"%s\"\tMinimal Link #: \"%d\"",
						searchKeyword, (int) expectedCount));
		session.waitDocumentReady(searchTimeout);
		session.waitUntil(o -> o.matches("#inner_search_form"), 1000, 100);
		session.waitUntil(o -> o.matches(searchInputSelector), searchTimeout,
				searchTimeout / 10);
		assertThat(session.getObjectId(searchInputSelector), notNullValue());
		highlight(searchInputSelector);
		// TODO: investigate
		// session.sendKeys(session.SPECIAL_KEYS...)
		super.clear(searchInputXPath);
		session.focus(searchInputSelector);
		session.sendKeys(searchKeyword);
		session.sendEnter();
	
		session.waitUntil(o -> o.matches(pubsCountSelector), searchTimeout,
				searchTimeout / 10);
		highlight(pubsCountSelector, 1000);
		Pattern pattern = Pattern.compile("(\\d+)");
		Matcher matcher = pattern.matcher(session.getText(pubsCountSelector));
		int publicationsFound = 0;
		if (matcher.find()) {
			publicationsFound = Integer.parseInt(matcher.group(1));
			System.err.println("# of publications " + publicationsFound);
		} else {
			System.err.println("No publications");
		}
		assertTrue(publicationsFound >= expectedCount);
		sleep(1000);
	}
	
	 */
	// Selenium
	protected void parseSearchResult(String searchKeyword, double expectedCount)
			throws InterruptedException {
		driver.get(baseUrl);
		System.err
				.println(String.format("Search keyword: \"%s\"\tMinimal Link #: \"%d\"",
						searchKeyword, (int) expectedCount));

		wait.until(ExpectedConditions
				.visibilityOfElementLocated(By.id("inner_search_form")));
		WebElement element = wait.until(
				ExpectedConditions.elementToBeClickable(By.xpath(searchInputXPath)));
		element.clear();
		element.sendKeys(searchKeyword);
		element.sendKeys(Keys.RETURN);

		element = wait.until(ExpectedConditions
				.visibilityOfElementLocated(By.cssSelector(pubsCountSelector)));
		pattern = Pattern.compile("(\\d+)");
		matcher = pattern.matcher(element.getText());
		int publicationsFound = 0;
		if (matcher.find()) {
			publicationsFound = Integer.parseInt(matcher.group(1));
			System.err.println("# of publications " + publicationsFound);
		} else {
			System.err.println("No publications");
		}
		assertTrue(publicationsFound >= expectedCount);
	}

}
