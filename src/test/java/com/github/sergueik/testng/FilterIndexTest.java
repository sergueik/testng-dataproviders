package com.github.sergueik.testng;

import org.testng.ITestContext;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.apache.commons.codec.binary.Base64;
/**
 * Copyright 2019 Serguei Kouzmine
 */
import org.testng.annotations.DataProvider;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

public class FilterIndexTest extends CommonTest {
	@Test(enabled = true, dataProvider = "data provider with index", expectedExceptions = java.lang.AssertionError.class /*, indices = "0:1" */)
	public void testIndex(Object expectedCount, Object searchKeyword)
			throws InterruptedException {
		dataTest(searchKeyword.toString(), expectedCount.toString());
	}

	// data provider with index
	// Some DataProvider public java.lang.Object[][]
	// com.github.sergueik.testng.FilterIndexTest.dataProviderInline(java.lang.String)
	// parameters unresolved: at 0 typeclass java.lang.String
	// https://howtodoinjava.com/testng/testng-dataprovider/
	@DataProvider(parallel = true, name = "data provider with index")
	@Parameters({ "indices" }) // global
	public Object[][] dataProviderInline(ITestContext context) {

		// https://stackoverflow.com/questions/21290122/testng-dataprovider-reading-test-data-from-the-testng-xml-config-file
		context.getCurrentXmlTest().getAllParameters().keySet().stream()
				.forEach(System.err::println);
		String indices = context.getCurrentXmlTest().getParameter("indices");
		String[] ids = indices.split(":");
		System.err.println("Indices: " + indices);
		Object[][] values = new Object[][] { { "junit", 100.0 },
				{ "junit-jupiter", 110.0 }, { "testng", 30.0 }, { "spock", 10.0 }, };
		Object[][] result = new Object[ids.length][];
		for (int i = 0; i < ids.length; i++) {
			result[i] = values[Integer.parseInt(ids[i])];
		}
		return result;
	}
}
