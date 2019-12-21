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
	// store the spreadsheet id through the path parameter:
	// with Google Sheet it has no verbatim meaning
	private static String spreadsheetId = null;
	// use name parameter
	private static String applicationName = "Google Sheets Example";
	private static String sheetName = "*";
	private static String secretFilePath = null;
	private static String columnNames = "*";
	private final static String testEnvironment = (System.getenv("TEST_ENVIRONMENT") != null)
			? System.getenv("TEST_ENVIRONMENT")
			: "";
	private static String controlColumn = null;
	private static String withValue = null;

	@DataProvider(parallel = true, name = "Google Spreadsheet")
	public static Object[][] createDataFromGoogleSpreadsheet(final ITestContext context, final Method method) {

		DataFileParameters parameters = method.getAnnotation(DataFileParameters.class);
		if (parameters != null) {
			applicationName = parameters.name();
			spreadsheetId = parameters.path();
			sheetName = parameters.sheetName();
			debug = parameters.debug();
			secretFilePath = parameters.secretFilePath();
		} else {
			throw new RuntimeException("Missing / invalid DataFileParameters annotation");
		}
		utils.setApplicationName(applicationName);
		utils.setSheetName(sheetName);
		utils.setColumnNames(columnNames);
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

		List<Object[]> result = utils.createDataFromGoogleSpreadsheet(spreadsheetId, sheetName);

		Object[][] resultArray = new Object[result.size()][];
		result.toArray(resultArray);
		return resultArray;
	}
}
