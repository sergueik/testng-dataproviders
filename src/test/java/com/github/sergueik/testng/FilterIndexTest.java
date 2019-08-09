package com.github.sergueik.testng;

import org.testng.ITestContext;

import java.util.List;
import java.util.Map;
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
import org.testng.xml.XmlTest;

public class FilterIndexTest extends CommonTest {

	@Test(groups = {
			"indices" }, enabled = true, dataProvider = "data provider with index", expectedExceptions = java.lang.AssertionError.class /*, indices = "0:1" */)
	@Parameters({ "indices" }) // ignored
	// https://howtodoinjava.com/testng/testng-parameters/
	// https://stackoverflow.com/questions/666477/possible-to-pass-parameters-to-testng-dataprovider
	// 
	// @Test.dataProvider and @Parameters both apply to method arguments
	// so when both are set, only the @Test.dataProvider is used
	public void testIndex(Object expectedCount, Object searchKeyword)
			throws InterruptedException {
		dataTest(searchKeyword.toString(), expectedCount.toString());
	}

	// data provider with index
	// https://howtodoinjava.com/testng/testng-dataprovider/
	// https://stackoverflow.com/questions/666477/possible-to-pass-parameters-to-testng-dataprovider
	@DataProvider(parallel = true, name = "data provider with index")
	// @Parameters({ "indices" }) // ignored
	// cannot define dataProviderInline(String method, ITestContext context)
	// Some DataProvider public java.lang.Object[][]
	// com.github.sergueik.testng.FilterIndexTest.dataProviderInline(java.lang.String)
	// parameters unresolved: at 0 typeclass java.lang.String
	//
	public Object[][] dataProviderInline(ITestContext context) {

		// https://stackoverflow.com/questions/21290122/testng-dataprovider-reading-test-data-from-the-testng-xml-config-file
		System.err.println("All global Parameters:");
		context.getCurrentXmlTest().getAllParameters().keySet().stream()
				.forEach(System.err::println);
		String indices = context.getCurrentXmlTest().getParameter("indices");
		System.err.println("Parameter \"indices\": " + indices);
		// https://www.codota.com/code/java/methods/org.testng.xml.XmlTest/getLocalParameters
		// https://stackoverflow.com/questions/21290122/testng-dataprovider-reading-test-data-from-the-testng-xml-config-file
		Map<String, String> localParameters = context.getCurrentXmlTest()
				.getLocalParameters();
		localParameters.keySet().stream().forEach(System.err::println);
		System.err.println("Local Parameters: " + localParameters.keySet());
		String[] ids = indices.split(":");
		Object[][] values = new Object[][] { { "junit", 100.0 },
				{ "junit-jupiter", 110.0 }, { "testng", 30.0 }, { "spock", 10.0 }, };
		Object[][] result = new Object[ids.length][];
		for (int i = 0; i < ids.length; i++) {
			result[i] = values[Integer.parseInt(ids[i])];
		}
		return result;
	}
}
