package com.github.sergueik.testng;

/**
 * Copyright 2019 Serguei Kouzmine
 */

import org.testng.ITestContext;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class FilterIndexTest extends CommonTest {

	@Test(enabled = true, dataProvider = "data provider with index", expectedExceptions = java.lang.AssertionError.class)
	public void testIndex(Object expectedCount, Object searchKeyword)
			throws InterruptedException {
		System.err.println(String.format(
				"Running data test with %s, %s, allowing it to fail the assertion ",
				searchKeyword.toString(), expectedCount.toString()));
		dataTest(searchKeyword.toString(), expectedCount.toString());
	}

	// data provider with index
	// https://stackoverflow.com/questions/21290122/testng-dataprovider-reading-test-data-from-the-testng-xml-config-
	// https://www.codota.com/code/java/methods/org.testng.xml.XmlTest/getLocalParameters
	// https://howtodoinjava.com/testng/testng-dataprovider/
	@DataProvider(parallel = true, name = "data provider with index")
	public Object[][] dataProviderInline(ITestContext context) {
		String indices = context.getCurrentXmlTest().getLocalParameters()
				.get("indices");
		// needs to be run with testng.xml or NPE
		System.err.println("Loaded the indices: " + indices);
		String[] ids = indices.split(":");
		Object[][] values = new Object[][] { { "junit", 100.0 },
				{ "junit-jupiter", 110.0 }, { "testng", 30.0 }, { "spock", 10.0 }, };
		Object[][] result = new Object[ids.length][];
		for (int i = 0; i < ids.length; i++) {
			System.err.println("Added to parameter data index " + i);
			result[i] = values[Integer.parseInt(ids[i])];
		}
		return result;
	}

}
