package com.github.sergueik.testng;
/**
 * Copyright 2017-2019 Serguei Kouzmine
 */

// import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

// JSON
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.ITestContext;

import org.testng.Assert;
import org.testng.annotations.DataProvider;

/**
 * @JSONParametersProvider container class for testng dataProvider method/ dataProviderClass
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */
public class JSONParametersProvider {

	private static boolean debug = false;
	private static String filePath = null;
	private static String dataKey = "test";
	private static String encoding = null;
	private static List<String> columns = new ArrayList<>();

	// TODO: will break with versions of JSON newer than 20080701
	// [Utils] [ERROR] [Error] java.lang.Error: Unresolved compilation problems:
	// JSONArray cannot be resolved to a type
	// JSONObject cannot be resolved to a type
	// org.json cannot be resolved to a type

	@DataProvider(parallel = false, name = "JSON")
	public static Object[][] createDataFromJSON(final ITestContext context,
			final Method method) throws org.json.JSONException {

		JSONDataFileParameters parameters = method
				.getAnnotation(JSONDataFileParameters.class);
		if (parameters != null) {
			filePath = String.format("%s/%s",
					(parameters.path().isEmpty()
							|| parameters.path().equalsIgnoreCase("."))
									? System.getProperty("user.dir")
									: Utils.resolveEnvVars(parameters.path()),
					parameters.name());
			encoding = parameters.encoding().isEmpty() ? "UTF-8"
					: parameters.encoding();
			dataKey = parameters.dataKey();
			columns = Arrays.asList(parameters.columns().split("(?:\\||,| )"));
			debug = parameters.debug();
			if (debug) {
				System.err.println("file path: " + filePath);
				System.err.println("data key: " + dataKey);
				System.err
						.println("columns: " + Arrays.deepToString(columns.toArray()));
			}
		} else {
			throw new RuntimeException(
					"Missing / invalid JSONDataFileParameters annotation");
		}

		JSONObject obj = new JSONObject();
		List<Object[]> testData = new ArrayList<>();
		List<Object> testDataRow = new LinkedList<>();
		List<String> hashes = new ArrayList<>();

		JSONArray rows = new JSONArray();

		try {
			byte[] encoded = Files.readAllBytes(Paths.get(filePath));
			obj = new JSONObject(new String(encoded, Charset.forName("UTF-8")));
		} catch (org.json.JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Assert.assertTrue(obj.has(dataKey));
		// With JSON >= 20170516
		// observed org.json.JSONException: JSONObject[&quot;test&quot;] not a
		// string.
		String dataString = obj.getString(dataKey);
		try {
			rows = new JSONArray(dataString);
		} catch (org.json.JSONException e) {
			e.printStackTrace();
		}
		for (int i = 0; i < rows.length(); i++) {
			String entry = rows.getString(i);
			hashes.add(entry);
		}
		Assert.assertTrue(hashes.size() > 0);

		String firstRow = hashes.get(0);

		// NOTE: apparently after invoking org.json.JSON library the order of keys
		// inside the firstRow will be non-deterministic
		// https://stackoverflow.com/questions/4515676/keep-the-order-of-the-json-keys-during-json-conversion-to-csv
		firstRow = firstRow.replaceAll("\n", " ").substring(1,
				firstRow.length() - 1);
		if (debug)
			System.err.println("1st row: " + firstRow);

		List<String> actualColumns = new ArrayList<>();
		String[] pairs = firstRow.split(",");

		for (String pair : pairs) {
			String[] values = pair.split(":");

			String column = values[0].substring(1, values[0].length() - 1).trim();
			if (debug) {
				System.err.println("column: " + column);
			}
			actualColumns.add(column);
		}

		for (String entry : hashes) {
			JSONObject entryObj = new JSONObject();
			testDataRow = new LinkedList<>();
			try {
				entryObj = new JSONObject(entry);
			} catch (org.json.JSONException e) {
				e.printStackTrace();
			}
			// actualColumns is ignored
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
}
