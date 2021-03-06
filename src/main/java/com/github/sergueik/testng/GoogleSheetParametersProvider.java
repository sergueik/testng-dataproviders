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
	// store the spreadsheet id through the path parameter:
	// with Google Sheet it has no verbatim meaning
	private static String spreadsheetId = null;
	// use name parameter
	private static String applicationName = "Google Sheets Example";
	private static String sheetName = "*";
	private static String secretFilePath = null;

	@DataProvider(parallel = true, name = "Google Spreadsheet")
	public static Object[][] createDataFromGoogleSpreadsheet(
			final ITestContext context, final Method method) {

		DataFileParameters parameters = method
				.getAnnotation(DataFileParameters.class);
		if (parameters != null) {
			applicationName = parameters.name();
			spreadsheetId = parameters.path();
			sheetName = parameters.sheetName();
			debug = parameters.debug();
			secretFilePath = parameters.secretFilePath();
		} else {
			throw new RuntimeException(
					"Missing / invalid DataFileParameters annotation");
		}
		utils.setApplicationName(applicationName);
		utils.setSheetName(sheetName);
		utils.setDebug(debug);
		utils.setSecretFilePath(secretFilePath);

		List<Object[]> result = utils.createDataFromGoogleSpreadsheet(spreadsheetId,
				sheetName);

		Object[][] resultArray = new Object[result.size()][];
		result.toArray(resultArray);
		return resultArray;
	}
}
