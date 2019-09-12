package com.github.sergueik.testng;
/**
 * Copyright 2017-2019 Serguei Kouzmine
 */

import org.testng.annotations.Test;
import java.io.File;
import java.io.IOException;

// NOTE: needed to switch to hamcrest-all.jar and Matchers 
// just for resolving method 'containsInAnyOrder'
// then eclipse periodically forgets to include it 
public class ExcelProviderTest extends CommonTest {
// TODO:  document
//      element value must be a constant expression
// cannot use class variables in annotation value:
	private static final String osName = System.getProperty("os.name").toLowerCase();
	private static final String dataFilePath = (osName.startsWith("windows")) ? "${USERPROFILE}\\Desktop"
			: "${HOME}/Desktop";

	// cannot use File.separator in annotation value:
//	@Test(groups = { "excel" }, enabled = true, singleThreaded = true, threadPoolSize = 1, invocationCount = 1, description = "# of articless for specific keyword", dataProvider = "Excel 2003", dataProviderClass = ExcelParametersProvider.class)
//	@DataFileParameters(name = "data_2003.xls", path = "${USERPROFILE}" + File.separator  + "Desktop" , sheetName = "Employee Data")
	@Test(groups = {
			"excel" }, enabled = true, singleThreaded = true, threadPoolSize = 1, invocationCount = 1, description = "# of articless for specific keyword", dataProvider = "Excel 2003", dataProviderClass = ExcelParametersProvider.class)
	@DataFileParameters(name = "data_2003.xls", path = "${USERPROFILE}\\Desktop", sheetName = "Employee Data")
	public void testWithExcel2003(double rowNum, String searchKeyword, double expectedCount)
			throws InterruptedException {
		dataTest(searchKeyword, expectedCount);

	}

	@Test(groups = {
			"excel" }, enabled = true, singleThreaded = true, threadPoolSize = 1, invocationCount = 1, description = "# of articless for specific keyword", dataProvider = "Excel 2007", dataProviderClass = ExcelParametersProvider.class)
	@DataFileParameters(name = "data_2007.xlsx", path = ".", sheetName = "Employee Data", debug = true)
	public void testWithExcel2007(double rowNum, String searchKeyword, double expectedCount)
			throws InterruptedException {
		dataTest(searchKeyword, expectedCount);
	}

	/*
	 * to expore the interplay between conditional and data providers, branch from
	 * dd584d3341e3fe782d2d4c2d3ed6a8b77a6c21d2
	 */
	/*
	 * @WindowsOnly
	 * 
	 * @Test(groups = { "excel" }, enabled = true, singleThreaded = true,
	 * threadPoolSize = 1, invocationCount = 1, description =
	 * "# of articless for specific keyword", dataProvider = "Excel 2007",
	 * dataProviderClass = ExcelParametersProvider.class)
	 * 
	 * @DataFileParameters(name = "data_2007.xlsx", path = ".", sheetName =
	 * "Employee Data", debug = true) public void
	 * testDecoratedWithDataProvererAndConditionalMethodAnnotation( double rowNum,
	 * String searchKeyword, double expectedCount) throws InterruptedException {
	 * dataTest(searchKeyword, expectedCount); }
	 */
}
