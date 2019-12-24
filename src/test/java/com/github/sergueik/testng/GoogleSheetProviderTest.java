package com.github.sergueik.testng;
/**
 * Copyright 2017-2019 Serguei Kouzmine
 */

import java.nio.file.Paths;

import org.testng.annotations.Test;

// NOTE: switched to hamcrest-all.jar for Matcher method 'containsInAnyOrder'
// eclipse periodically forgets to include

public class GoogleSheetProviderTest extends CommonTest {
	private static final String SECRET_FILEPATH_WILL_NOT_COMPILE = Paths
			.get(System.getProperty("user.home")).resolve(".secret")
			.resolve("client_secret.json").toAbsolutePath().toString();
	private static final String SECRET_FILEPATH = "C:/Users/Serguei/.secret/client_secret.json";

	@Test(dataProviderClass = GoogleSheetParametersProvider.class, dataProvider = "Google Spreadsheet")
	@DataFileParameters(name = "Google Sheets Example", path = "17ImW6iKSF7g-iMvPzeK4Zai9PV-lLvMsZkl6FEkytRg", sheetName = "Test Data", secretFilePath = SECRET_FILEPATH, debug = true)
	public void testWithGoogleSheet(String strRowNum, String searchKeyword,
			String strExpectedCount) throws InterruptedException {
		double expectedCount = Double.parseDouble(strExpectedCount);
		dataTest(searchKeyword, expectedCount);

	}

}
