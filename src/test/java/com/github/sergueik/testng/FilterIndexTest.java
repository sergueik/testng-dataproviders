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
		System.err.println("All global Parameters:");
		context.getCurrentXmlTest().getAllParameters().keySet().stream()
				.forEach(System.err::println);
		String indices = context.getCurrentXmlTest().getParameter("indices");
		System.err.println("Parameter \"indices\": " + indices);
		XmlTest copyTest = clone(context);
		System.err.println(copyTest.toXml("\t"));
		// https://www.codota.com/code/java/methods/org.testng.xml.XmlTest/getLocalParameters
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

	// @Override
	public XmlTest clone(ITestContext context) {
		XmlTest currentXMLTest = context.getCurrentXmlTest();
		XmlTest result = new XmlTest(currentXMLTest.getSuite());
		result.setName(currentXMLTest.getName());
		result.setIncludedGroups(currentXMLTest.getIncludedGroups());
		result.setExcludedGroups(currentXMLTest.getExcludedGroups());
		result.setJUnit(currentXMLTest.isJUnit());
		result.setParallel(currentXMLTest.getParallel());
		result.setVerbose(currentXMLTest.getVerbose());
		result.setParameters(currentXMLTest.getLocalParameters());
		result.setXmlPackages(currentXMLTest.getXmlPackages());
		// protected
		// result.setTimeOut(currentXMLTest.getTimeOut());
		Map<String, List<String>> metagroups = currentXMLTest.getMetaGroups();
		for (Map.Entry<String, List<String>> group : metagroups.entrySet()) {
			result.addMetaGroup(group.getKey(), group.getValue());
		}
		return result;
	}

}
