###  TestNg-DataProviders [![BuildStatus](https://travis-ci.org/sergueik/testng-dataproviders.svg?branch=master)](https://travis-ci.org/sergueik/testng-dataproviders.svg?branch=maste://travis-ci.org/sergueik/testng-dataproviders.svg?branch=master)

This project exercises [testng dataProviders](http://testng.org/doc/documentation-main.html#parameters-dataproviders)

  * Excel 2003 OLE documents - Horrible SpreadSheet Format [org.apache.poi.hssf.usermodel.*)](http://shanmugavelc.blogspot.com/2011/08/apache-poi-read-excel-for-use-of.html)
  * Excel 2007 OOXML (.xlsx) - XML SpreadSheet Format [org.apache.poi.xssf.usermodel.*](http://howtodoinjava.com/2013/06/19/readingwriting-excel-files-in-java-poi-tutorial/)
  * OpenOffice SpreadSheet (.ods) [example1](http://www.programcreek.com/java-api-examples/index.php?api=org.jopendocument.dom.spreadsheet.Sheet) ,[example 2]http://half-wit4u.blogspot.com/2011/05/read-openoffice-spreadsheet-ods.html
  * Custom JSON [org.json.JSON](http://www.docjar.com/docs/api/org/json/JSONObject.html)
  * csv [testnt csv file](http://stackoverflow.com/questions/26033985/how-to-pass-parameter-to-data-provider-in-testng-from-csv-file)
  * fillo [fillo](http://codoid.com/fillo/)

Unlike core TestNg data providers configurable through annotation constant parameters this Data provider class features runtime-flexible data file paths iparameterization enabling one running the jar with environment-specific test data without recompiling the java project. This feature was requested in one of the forums and was easy to implement - details in in __Extra Features__ section below.

### Testing

For example test case performs Selenium link count test with the data providers of the following supported data types:

* Excel 2003
* Excel 2007
* Open Office Spreadsheet
* JSON

The test inputs are defined as spreadsheet with columns

| ROWNUM |  SEARCH | COUNT |
|--------|---------|-------|
| 1      | junit   | 100   |

or a JSON file with the following structure:
```javascript
{
    "test": [{
        "keyword": "junit",
        "count": 101.0,
        "comment": "",
        "other unused column": "",
    }, {
        "comment": "",
        "keyword": "testng",
        "count": 31.0
    },
...
]
}
```

which represent the test `id`, the *seach term* and *expected minimum count* of articles found on the forum through title search.

The following annotations are provided to the test methods:

```java
@Test(enabled = true, singleThreaded = false, threadPoolSize = 1, invocationCount = 1, description = "searches publications for a keyword", dataProvider = "Excel 2003")
@DataFileParameters(name = "data_2003.xls", path = "${USERPROFILE}\\Desktop", sheetName = "Employee Data")
	public void test_with_Excel_2003(double rowNum, String search_keyword,
			double expected_count) throws InterruptedException {
		parseSearchResult(search_keyword, expected_count);
	}
```
or
```java
@Test(enabled = true, singleThreaded = false, threadPoolSize = 1, invocationCount = 1, description = "searches publications for a keyword", dataProvider = "Excel 2007")
@DataFileParameters(name = "data_2007.xlsx", path = ".")
	public void test_with_Excel_2007(double rowNum, String search_keyword,
			double expected_count) throws InterruptedException {
		parseSearchResult(search_keyword, expected_count);
	}
```
or
```java
@Test(enabled = true, singleThreaded = false, threadPoolSize = 1, invocationCount = 1, description = "searches publications for a keyword", dataProvider = "OpenOffice Spreadsheet")
@DataFileParameters(name = "data.ods", path = "src/main/resources") // when datafile path is relative assume it is under ${user.dir}
	public void test_with_OpenOffice_Spreadsheet(double rowNum,
			String search_keyword, double expected_count)
			throws InterruptedException {
		parseSearchResult(search_keyword, expected_count);
	}
```
or
```java
@Test(enabled = true, singleThreaded = false, threadPoolSize = 1, invocationCount = 1, description = "searches publications for a keyword", dataProvider = "JSON")
@JSONDataFileParameters(name = "data.json", dataKey = "test", columns = "keyword,count"
  // one need to list value columns explicitly with JSON due to the way org.json.JSONObject is implemented
	public void test_with_JSON(String search_keyword, double expected_count)
			throws InterruptedException {
		parseSearchResult(search_keyword, expected_count);
	}
```
The data provider class will load all columns from Excel 2003, Excel 2007 or OpenOffice spreadsheet respectively and columns defined for JSON data provider
and run test method with every row of data. It is up to the test developer to make the test method consume the correct number and type or parameters as the columns
in the spreadsheet.

To enable debug messages during the data loading, set the `debug` flag with `@DataFileParameters` attribute:
```java
	@Test(enabled = true, singleThreaded = true, threadPoolSize = 1, invocationCount = 1, description = "# of articless for specific keyword", dataProvider = "Excel 2007", dataProviderClass = ExcelParametersProvider.class)
	@DataFileParameters(name = "data_2007.xlsx", path = ".", sheetName = "Employee Data", debug = true)
	public void test_with_Excel_2007(double rowNum, String searchKeyword,
			double expectedCount) throws InterruptedException {
		dataTest(searchKeyword, expectedCount);
	}
```

this will show the following:
```shell
Data Provider Caller Suite: Suite 1
Data Provider Caller Test: Parse Search Result
Data Provider Caller Method: test_with_Excel_2007
0 = A ID
1 = B SEARCH
2 = C COUNT
Cell Value: 1.0 class java.lang.Double
Cell Value: junit class java.lang.String
Cell Value: 104.0 class java.lang.Double
...
row 0 : [1.0, junit, 104.0]
...
```
### Extra Features

This data provider overcomes the known difficulty of core TestNG or Junit parameter annotations: developer is
[not allowed](https://stackoverflow.com/questions/16509065/get-rid-of-the-value-for-annotation-attribute-must-be-a-constant-expression-me) to redefine the dataprovider attributes like for example the data source path:

```java
public static final String dataPath = "file:src/test/resources/data.json";
  @Test
  @ExcelParameters(filepath = dataPath)
  public void test( double rowNum, String keyword, double count) {
   // actual code ot the  test
  }
```
In the above, one is only allowed to initialize the `testDataPath` to a `String`(or `int`) primitive type, in particular even
declaring the same (pseudo-const) data in a separate class:

```java
public class ParamData {
  public final static String dataPath = "file:src/test/resources/data.json";
}
```
and assigning the result to the vatiable in the main test class,
```java
public class FileParamsTest {

  private final static String dataPath = ParamData.dataPath;
```
would fail to compile:
```sh
Compilation failure:
[ERROR] FileParamsTest.java: element value must be a constant expression
```
so it likely not doable.

However it is quite easy to allow such flexibility in the data provider class `ExcelParametersProvider` itself by adding an extra class variable named  e.g. `testEnvironment` that would receive its value from e.g. the environment variable named `TEST_ENVIRONMENT` that, when set, would override the data file paths
that in were specified through the `file://` protocol
and which therefore refer to the system files (not to data embedded in the jar):
so  the regular test data provider annotation
```java
  @Test
  @ExcelParameters(filepath = "file:src/test/resources/data.xlsx")
  public void test(double rowNum, String keyword, double count) {
    try {
    dataTest(keyword, count);
    } catch (IllegalStateException e) {
    System.err.println(String.format("keyword: %s , cound : %d ", keyword, count));
    }
  }

```
in the presence of the environment `TEST_ENVIRONMENT` with the value `dev` will make it read parameters of the test from `src/test/resources/dev/data.xlsx` dather then `src/test/resources/data.xlsx`.

It is implemented directly in the `ExcelParametersProvider` provider in a very basic fashion as shown below:

```java
public class ExcelParametersProvider
implements ParametersProvider<ExcelParameters> {

  private final static String testEnvironment = (System.getenv("TEST_ENVIRONMENT") != null) ? System.getenv("TEST_ENVIRONMENT") : "";

  private static String amendFilePath(String filePath) {
    if (debug) {
      System.err.print(
        String.format("Amending the %s with %s", filePath, testEnvironment));       }
    // Inject the directory into the file path
    String updatedFilePath = filePath.replaceAll("^(.*)/([^/]+)$",
    String.format("$1/%s/$2", testEnvironment));
    if (debug) {
      System.err.println(String.format(" => %s", updatedFilePath));
    }
    return updatedFilePath;
}
```

and take it into account to redefine the inputs during initialization:

```java
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
    if (testEnvironment != null && testEnvironment != "") {
      filePath = amendFilePath(filePath);
    }

```

therefore the test
```cmd
mkdir dev
mkdir src\main\resources\dev
copy src\main\resources\data.* src\main\resources\dev\
copy data*.* dev
set  TEST_ENVIRONMENT=dev
mvn test
```
works as expected (the example shows debug output for Open Office data file `data.ods` originally red from `src/main/resources`):

```cmd
Amending the c:\developer\sergueik\testng-dataproviders/src/main/resources/data.
ods with dev => c:\developer\sergueik\testng-dataproviders/src/main/resources/de
v/data.ods
BeforeMethod Suite: Suite 1
Reading Open Office Spreadsheet: Employee Data
Cell Value: "1.0" class java.lang.Double
Cell Value: "junit" class java.lang.String
Cell Value: "202.0" class java.lang.Double
Cell Value: "2.0" class java.lang.Double
```
One can easily make this behavior optional, turn the `TEST_ENVIRONMENT` envirnmant name a separate parameter or switch to store definitions of environment specifics into the property file (this is work in progress). Similar changes will be soon available to

### Links

 * [MySQL testng dataprovider](https://github.com/sskorol/selenium-camp-samples/tree/master/mysql-data-provider)
 * [xml testng DataProviders](http://testngtricks.blogspot.com/2013/05/how-to-provide-data-to-dataproviders.html)
 * [javarticles.com](http://javarticles.com/2015/03/example-of-testng-dataprovider.html)
 * [testng-users forum](https://groups.google.com/forum/#!topic/testng-users/J437qa5PSx8)
 * [passing parameters to provider via Method](http://stackoverflow.com/questions/666477/possible-to-pass-parameters-to-testng-dataprovider)
 * [JUnitParams](https://github.com/Pragmatists/JUnitParams) - TestNg-style `JUnitParamsRunner` and `ParametersProvider` classes.
 * [testng samples](https://habrahabr.ru/post/121234/)
 * [barancev/testng_samples](https://github.com/barancev/testng_samples)
 * [ahussan/DataDriven](https://github.com/ahussan/DataDriven)
 * [poi ppt](https://www.tutorialspoint.com/apache_poi_ppt/apache_poi_ppt_quick_guide.htm)
 * [paypal/SeLion data providers](https://github.com/paypal/SeLion/tree/develop/dataproviders/src/main/java/com/paypal/selion/platform/dataprovider)
 * [RestAPIFramework-TestNG/.../ExcelLibrary](https://github.com/hemanthsridhar/RestAPIFramework-TestNG/blob/master/src/main/java/org/framework/utils/ExcelLibrary.java)
 * [GladsonAntony/WebAutomation_Allure ExcelUtils.java](https://github.com/GladsonAntony/WebAutomation_Allure/blob/master/src/main/java/utils/ExcelUtils.java)
 * [XLS Test - Excel testing library](https://github.com/codeborne/xls-test)
 * [sskorol/tesst-data-supplier](https://github.com/sskorol/test-data-supplier)
 * [converting gradle to pom](https://stackoverflow.com/questions/12888490/gradle-build-gradle-to-maven-pom-xml)
 * [using gradle maven plugin to produce pom.xml](https://stackoverflow.com/questions/17281927/how-to-make-gradle-generate-a-valid-pom-xml-file-at-the-root-of-a-project-for-ma)
 * [Selenium data driven testing with Excel](https://www.swtestacademy.com/data-driven-excel-selenium/)
 * [Excel template-based report generating library](https://github.com/CourseOrchestra/xylophone)

### Maven Central

The snapshot versions are deployed to `https://oss.sonatype.org/content/repositories/snapshots/com/github/sergueik/dataprovider/`
The release versions status: [Release pending](https://issues.sonatype.org/browse/OSSRH-36773?page=com.atlassian.jira.plugin.system.issuetabpanels:all-tabpanel)

To use the snapshot version, add the following to `pom.xml`:
```xml
<dependency>
  <groupId>com.github.sergueik.testng</groupId>
  <artifactId>dataprovider</artifactId>
  <version>1.3-SNAPSHOT</version>
</dependency>
<repositories>
  <repository>
    <id>ossrh</id>
    <url>https://oss.sonatype.org/content/repositories/snapshots</url>
  </repository>
</repositories>
```
### Apache POI compatibility

  * The default version of the supported Apache POI is 3.17.
  * Older versions of the package require minor code refactoring. Note that you may also have to clear the other versions of poi and poi-ooxml jars from maven cache '~/.m2/repository'
  * Project can be built with Apache POI 4.0.0 by modifying `poi.version` to `4.0.0` within `&lt;properties&gt;` node in the `pom.xml` - the profile `poi40` does not work correctly.
  * Creating branches and tags is a work in progress.

### Author
[Serguei Kouzmine](kouzmine_serguei@yahoo.com)
