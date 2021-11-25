package com.github.sergueik.testng;
/**
 * Copyright 2017-2019,2021 Serguei Kouzmine
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.lang.reflect.Method;
import java.nio.charset.Charset;

import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.testng.ITestContext;
import org.testng.annotations.DataProvider;

/**
 * @CSVParametersProvider container class for testng dataProvider defining
 *                        methods for csv
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */
public class CSVParametersProvider {

	private static Scanner scanner = null;
	private static List<String[]> testData = new ArrayList<>();
	private static String[] data = null;
	private static String separator = "|";
	private static String filePath = null;
	private static String encoding = null;
	private static boolean skipHeader = true;
	private static boolean debug = false;

	@DataProvider(parallel = false, name = "csv")
	public static String[][] createData_from_csv(final ITestContext context, final Method method) {
		if (debug) {
			System.err.println(String.format("Providing data to method: '%s' of test '%s'", method.getName(),
					context.getCurrentXmlTest().getName()));
		}
		DataFileParameters parameters = method.getAnnotation(DataFileParameters.class);
		if (parameters != null) {
			filePath = String.format("%s/%s",
					(parameters.path().isEmpty() || parameters.path().matches("^\\.$")) ? System.getProperty("user.dir")
							: Utils.resolveEnvVars(parameters.path()),
					parameters.name());
			encoding = parameters.encoding().isEmpty() ? "UTF-8" : parameters.encoding();
		} else {
			throw new RuntimeException("Missing / invalid DataFileParameters annotation");
		}
		int linenum = 0;
		if (debug) {
			System.err.println(String.format("Reading configuration file: '%s'", filePath));
		}
		try {
			scanner = new Scanner(new File(filePath));
			while (scanner.hasNext()) {
				linenum++;
				String line = scanner.next();
				if (skipHeader && linenum == 1) {
					if (debug) {
						System.err.println(String.format("Skipping headers: '%s'", line));
					}
					continue;
				}
				data = line.split(Pattern.compile("(\\||\\|/)").matcher(separator).replaceAll("\\\\$1"));
				testData.add(data);
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			System.err.println(String.format("File was not found: '%s'", filePath));
			e.printStackTrace();
		}
		String[][] testDataArray = new String[testData.size()][];
		testData.toArray(testDataArray);
		return testDataArray;
	}

	private static String readFile(String path, Charset encoding) {
		try {
			byte[] encoded = Files.readAllBytes(Paths.get(path));
			return new String(encoded, encoding);
		} catch (IOException e) {
			return null;
		}
	}

}
