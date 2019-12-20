package com.github.sergueik.testng;
/**
 * Copyright 2019 Serguei Kouzmine
 */

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.testng.ITestContext;
import org.testng.annotations.DataProvider;

/**
 * @GoogleSheetParametersProvider container class for testng dataProvider method
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class GoogleSheetParametersProvider {

	private static Utils utils = Utils.getInstance();
	private static boolean debug = false;
	private static boolean loadEmptyColumns = false;
	private static String spreadsheetId = null;
	// meaningless verbatim, will store the spreadsheet id
	private static String sheetName = null;
	private static String columnNames = "*";
	private final static String testEnvironment = (System
			.getenv("TEST_ENVIRONMENT") != null) ? System.getenv("TEST_ENVIRONMENT")
					: "";
	private static String controlColumn = null;
	private static String withValue = null;

	@DataProvider(parallel = true, name = "Google Spreadsheet")
	public static Object[][] createDataFromGoogleSpreadsheet(
			final ITestContext context, final Method method) {

		DataFileParameters parameters = method
				.getAnnotation(DataFileParameters.class);
		if (parameters != null) {
			spreadsheetId = parameters.name();
			sheetName = parameters.sheetName();
		} else {
			throw new RuntimeException(
					"Missing / invalid DataFileParameters annotation");
		}
		utils.setSheetName(sheetName);
		utils.setColumnNames(columnNames);
		debug = parameters.debug();
		loadEmptyColumns = parameters.loadEmptyColumns();
		controlColumn = parameters.controlColumn();
		if (!controlColumn.isEmpty()) {
			utils.setControlColumn(controlColumn);
		}
		withValue = parameters.withValue();
		if (!withValue.isEmpty()) {
			utils.setWithValue(withValue);
		}

		utils.setLoadEmptyColumns(loadEmptyColumns);
		utils.setDebug(debug);

		List<Object[]> result = utils.createDataFromGoogleSpreadsheet(spreadsheetId,
				sheetName);

		Object[][] resultArray = new Object[result.size()][];
		result.toArray(resultArray);
		return resultArray;
	}
}
