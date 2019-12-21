package com.github.sergueik.testng;
/**
 * Copyright 2017-2019 Serguei Kouzmine
 */

import org.testng.annotations.Test;

// NOTE: switched to hamcrest-all.jar for Matcher method 'containsInAnyOrder'
// eclipse periodically forgets to include

public class GoogleSheetProviderTest extends CommonTest {
	@Test(dataProviderClass = GoogleSheetParametersProvider.class, dataProvider = "Google Spreadsheet")
	@DataFileParameters(name = "Google Sheets Example", path = "17ImW6iKSF7g-iMvPzeK4Zai9PV-lLvMsZkl6FEkytRg", sheetName = "Test Data", secretFilePath = "/home/sergueik/.secret/client_secret.json", debug = true)
	public void testWithGoogleSheet(String strRowNum, String searchKeyword, String strExpectedCount)
			throws InterruptedException {
		double rowNum = Double.parseDouble(strRowNum);
		double expectedCount = Double.parseDouble(strExpectedCount);
		dataTest(searchKeyword, expectedCount);

	}

}
