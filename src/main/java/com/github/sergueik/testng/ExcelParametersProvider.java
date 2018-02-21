package com.github.sergueik.testng;
/**
 * Copyright 2017 Serguei Kouzmine
 */

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.testng.ITestContext;
import org.testng.annotations.DataProvider;

/**
 * @ExcelParametersProvider container class for testng dataProvider method
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class ExcelParametersProvider {

	private static Utils utils = Utils.getInstance();
	private static boolean debug = false;
	private static boolean loadEmptyColumns = false;
	private static String filePath = null;
	private static String sheetName = null;
	private static String columnNames = "*";

	// passed via ExcelParameters
	// public void setDebug(boolean debug) {
	// this.debug = debug;
	// }

	@DataProvider(parallel = false, name = "OpenOffice Spreadsheet")
	public static Object[][] createData_from_OpenOfficeSpreadsheet(
			final ITestContext context, final Method method) {
		DataFileParameters parameters = method
				.getAnnotation(DataFileParameters.class);
		if (parameters != null) {
			filePath = String.format("%s/%s",
					(parameters.path().isEmpty()
							|| parameters.path().equalsIgnoreCase("."))
									? System.getProperty("user.dir")
									: Utils.resolveEnvVars(parameters.path()),
					parameters.name());
			// if the path is relative assume it is under ${user.dir}
			if (!filePath.matches("^[/\\\\].*")
					&& !filePath.matches("^(?i:[A-Z]):.*")) {
				filePath = String.format("%s/%s", System.getProperty("user.dir"),
						filePath);
			}

			sheetName = parameters.sheetName();
		} else {
			throw new RuntimeException(
					"Missing / invalid DataFileParameters annotation");
		}
		utils.setSheetName(sheetName);
		utils.setColumnNames(columnNames);
		debug = parameters.debug();
		loadEmptyColumns = parameters.loadEmptyColumns();
		utils.setLoadEmptyColumns(loadEmptyColumns);
		utils.setDebug(debug);

		List<Object[]> result = utils.createDataFromOpenOfficeSpreadsheet(filePath);
		Object[][] resultArray = new Object[result.size()][];
		result.toArray(resultArray);
		return resultArray;
	}

	@DataProvider(parallel = false, name = "Excel 2007")
	public static Object[][] createDataFromExcel2007(final ITestContext context,
			final Method method) {
		DataFileParameters parameters = method
				.getAnnotation(DataFileParameters.class);
		if (parameters != null) {
			filePath = String.format("%s/%s",
					(parameters.path().isEmpty() || parameters.path().matches("^\\.$"))
							? System.getProperty("user.dir")
							: Utils.resolveEnvVars(parameters.path()),
					parameters.name());
			sheetName = parameters.sheetName();
		} else {
			throw new RuntimeException(
					"Missing / invalid DataFileParameters annotation");
		}

		utils.setSheetName(sheetName);
		utils.setColumnNames(columnNames);
		debug = parameters.debug();
		utils.setDebug(debug);
		loadEmptyColumns = parameters.loadEmptyColumns();
		utils.setLoadEmptyColumns(loadEmptyColumns);
		// String suiteName = context.getCurrentXmlTest().getSuite().getName();
		if (debug) {
			System.err.println("Data Provider Caller Suite: "
					+ context.getCurrentXmlTest().getSuite().getName());
			System.err.println("Data Provider Caller Test: "
					+ context.getCurrentXmlTest().getName());
			System.out.println("Data Provider Caller Method: " + method.getName());
		}
		// String testParam =
		// context.getCurrentXmlTest().getParameter("test_param");

		/*
		@SuppressWarnings("deprecation")
		Map<String, String> parameters = (((TestRunner) context).getTest())
				.getParameters();
		for (String key : parameters.keySet()) {
			System.out.println("Data Provider Caller Parameter: " + key + " = "
					+ parameters.get(key));
		}
		*/

		List<Object[]> result = utils.createDataFromExcel2007(filePath);
		if (debug) {
			int cnt = 0;
			for (Object[] row : result) {
				System.err
						.println(String.format("row %d : %s", cnt, Arrays.toString(row)));
				cnt++;
			}
		}
		Object[][] resultArray = new Object[result.size()][];
		result.toArray(resultArray);
		return resultArray;

	}

	@DataProvider(parallel = false, name = "Excel 2003")
	public static Object[][] createDataFromExcel2003(final ITestContext context,
			final Method method) {

		DataFileParameters parameters = method
				.getAnnotation(DataFileParameters.class);
		if (parameters != null) {
			filePath = String.format("%s/%s",
					(parameters.path().isEmpty() || parameters.path().matches("^\\.$"))
							? System.getProperty("user.dir")
							: Utils.resolveEnvVars(parameters.path()),
					parameters.name());
			sheetName = parameters.sheetName();
		} else {
			throw new RuntimeException(
					"Missing / invalid DataFileParameters annotation");
		}
		if (debug) {
			System.err.println("Reading file: " + filePath);
		}

		utils.setSheetName(sheetName);
		utils.setColumnNames(columnNames);
		debug = parameters.debug();
		utils.setDebug(debug);
		loadEmptyColumns = parameters.loadEmptyColumns();
		utils.setLoadEmptyColumns(loadEmptyColumns);
		List<Object[]> result = utils.createDataFromExcel2003(filePath);
		Object[][] resultArray = new Object[result.size()][];
		result.toArray(resultArray);
		return resultArray;
	}
}
