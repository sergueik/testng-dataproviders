package com.github.sergueik.testng;
/**
 * Copyright 2017-2019 Serguei Kouzmine
 */

import org.testng.annotations.Test;

// NOTE: switched to hamcrest-all.jar for Matcher method 'containsInAnyOrder'
// eclipse periodically forgets to include

public class ExcelProviderTest extends CommonTest {

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
	 * to explore interplay between conditional and data providers, branch from
	 * dd584d3341e3fe782d2d4c2d3ed6a8b77a6c21d2
	 */
	/*
	 * @WindowsOnly
	 * 
	 * @Test(groups = { "excel" }, enabled = true, singleThreaded = true,
	 * threadPoolSize = 1, invocationCount = 1, description =
	 * "# of articles for specific keyword", dataProvider = "Excel 2007",
	 * dataProviderClass = ExcelParametersProvider.class)
	 * 
	 * @DataFileParameters(name = "data_2007.xlsx", path = ".", sheetName =
	 * "Employee Data", debug = true) public void
	 * testDecoratedWithDataProvererAndConditionalMethodAnnotation( double rowNum,
	 * String searchKeyword, double expectedCount) throws InterruptedException {
	 * dataTest(searchKeyword, expectedCount); }
	 */
}
