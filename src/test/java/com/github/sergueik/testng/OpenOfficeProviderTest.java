package com.github.sergueik.testng;
/**
 * Copyright 2017-2019 Serguei Kouzmine
 */

import java.lang.reflect.Method;
import org.testng.annotations.Test;

public class OpenOfficeProviderTest extends CommonTest {

	public final static String dataPath = "src/main/resources";
	// NOTE: cannot do
	// dataPath = param();

	public static final String param() {
		return "src/main/resources";
	}

	@Test(enabled = true, singleThreaded = true, threadPoolSize = 1, invocationCount = 1, description = "# of articless for specific keyword", dataProvider = "OpenOffice Spreadsheet", dataProviderClass = ExcelParametersProvider.class)
	@DataFileParameters(name = "data.ods", path = dataPath, debug = true)
	public void testWithOpenOfficeSpreadsheet(double rowNum, String searchKeyword,
			double expectedCount) throws InterruptedException {
		dataTest(searchKeyword, expectedCount);
	}

	// https://testng.org/doc/documentation-main.html
	@Test(enabled = true, singleThreaded = true, threadPoolSize = 1, invocationCount = 1, description = "# of articless for specific keyword", dataProvider = "OpenOffice Spreadsheet", dataProviderClass = ExcelParametersProvider.class)
	@DataFileParameters(name = "data.ods", path = dataPath, debug = true)
	public void testWithOpenOfficeSpreadsheetWithMethod(Method method,
			double rowNum, String searchKeyword, double expectedCount)
			throws InterruptedException {
		dataTestWithMethod(method, searchKeyword, expectedCount);
	}

	@Test(enabled = true, singleThreaded = true, threadPoolSize = 1, invocationCount = 1, description = "# of articless for specific keyword", dataProvider = "OpenOffice Spreadsheet", dataProviderClass = ExcelParametersProvider.class)
	@DataFileParameters(name = "filtered_data.ods", path = dataPath, sheetName = "Filtered Example", controlColumn = "ENABLED", withValue = "true", debug = true)
	public void testFilteredData(double rowNum, String searchKeyword,
			double expectedCount) throws InterruptedException {
		dataTest(searchKeyword, expectedCount);
	}

}
