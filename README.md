###  TestNg-DataProviders [![BuildStatus](https://travis-ci.org/sergueik/testng-dataproviders.svg?branch=master)](https://travis-ci.org/sergueik/testng-dataproviders.svg?branch=maste://travis-ci.org/sergueik/testng-dataproviders.svg?branch=master)

This project exercises [testng dataProviders](http://testng.org/doc/documentation-main.html#parameters-dataproviders)
backed by various Office formats

  * Excel 2003 OLE documents - Horrible SpreadSheet Format [org.apache.poi.hssf.usermodel.*)](http://shanmugavelc.blogspot.com/2011/08/apache-poi-read-excel-for-use-of.html)
  * Excel 2007 OOXML (.xlsx) - XML SpreadSheet Format [org.apache.poi.xssf.usermodel.*](http://howtodoinjava.com/2013/06/19/readingwriting-excel-files-in-java-poi-tutorial/)
  * OpenOffice SpreadSheet (.ods) [example1](http://www.programcreek.com/java-api-examples/index.php?api=org.jopendocument.dom.spreadsheet.Sheet), [example 2](http://half-wit4u.blogspot.com/2011/05/read-openoffice-spreadsheet-ods.html)
  * Custom JSON [org.json.JSON](http://www.docjar.com/docs/api/org/json/JSONObject.html)
  * csv [testng csv file](http://stackoverflow.com/questions/26033985/how-to-pass-parameter-to-data-provider-in-testng-from-csv-file)
  * fillo [fillo](http://codoid.com/fillo/)
  * [Google sheet](https://www.google.com/sheets/about/) (experimental).

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
  public static final String dataPath = "src/main/resources";

@Test(enabled = true, dataProvider = "OpenOffice Spreadsheet", dataProviderClass = ExcelParametersProvider.class)
  @DataFileParameters(name = "data.ods", path = dataPath, debug = false)
  public void test(double rowNum,
    String searchKeyword, double expectedCount) throws InterruptedException {
    // actual code ot the  test
  }
```
In the above, one is only allowed to initialize the `dataPath` to a `String` (or `int`) primitive type, in particular even
declaring the same (pseudo-const) value in a separate class:

```java
public class ParamData {
  public final static String dataPath = "src/main/resources";
}
```
and assigning the result to the vatiable in the main test class,
```java
public class FileParamsTest {
  private final static String dataPath = ParamData.dataPath;
```
or assigning the method rerturn value to the parameter:
```
  public final static String dataPath = param();

  public static final String param() {
    return "src/main/resources";
  }
```
would fail to compile:
```sh
Compilation failure:
[ERROR]TestNgDataProviderTest.java: element value must be a constant expression
```
so it likely not doable.

However it is quite easy to allow such flexibility
in the data provider class `ExcelParametersProvider` itself by
adding an extra class variable e.g. `testEnvironment` which would
load its value from the environment variable named `TEST_ENVIRONMENT`
```java
private final static String testEnvironment = (System
    .getenv("TEST_ENVIRONMENT") != null) ? System.getenv("TEST_ENVIRONMENT")
        : "";
```
and override the datafile path value provided in the test method annotation:
```java
@Test(enabled = true, dataProvider = "OpenOffice Spreadsheet", dataProviderClass = ExcelParametersProvider.class)
@DataFileParameters(name = "data.ods", path = "src/main/resources", debug = false)
public void test(double rowNum, String searchKeyword, double expectedCount) throws InterruptedException {
  dataTest(searchKeyword, expectedCount);
}
```
in the presence of the environment `TEST_ENVIRONMENT` with the value `dev` will make it read parameters of the test from `src/main/resources/dev/data.ods` rather then `src/main/resources/data.ods`:
```java
if (testEnvironment != null && testEnvironment != "") {
  filePath = amendFilePath(filePath);
}
```

This functionaliy is implemented directly in the `ExcelParametersProvider` provider class
in a very basic fashion as shown below:

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


### Filtering Data Rows for JUnitParams

In addition to using *every row* of spreadsheet as test parameter one may create a designated column which value
would be indicating to use or skip that row of data, like:

| ROWNUM |    SEARCH    | COUNT |ENABLED
|--------|--------------|-------|-------
| 1      | __junit__    | 100   | true
| 2      | __testng__   | 30    | true
| 3      | __spock__    | 20    | false
| 4      | __mockito__  | 41    | true

and annotate the method like

```java
@Test(enabled = false, singleThreaded = true, threadPoolSize = 1, invocationCount = 1, description = "# of articless for specific keyword", dataProvider = "OpenOffice Spreadsheet", dataProviderClass = ExcelParametersProvider.class)
@DataFileParameters(name = "filtered_data.ods", path = dataPath, sheetName = "Filtered Example" , controlColumn = "ENABLED", withValue = "true", debug = true)
public void testWithFilteredParameters(double rowNum,
    String searchKeyword, double expectedCount) throws InterruptedException {
  dataTest(searchKeyword, expectedCount);
}
```

with this data setting only rows 1,2 and 4 from the data extract above would be used as `testWithFilteredParameters` test method parameters.
The control column itself is not passed to the subject test method.
Currently this functionality is implemented for __OpenOffice__ spreadsheet only.
Remaining format is a work in progress.

This feature of storing more then one set of tests in one spreadsheet and picking the ones which column is set to a specified value
 has been inspired by some python [post](https://docs.pytest.org/en/latest/fixture.html#parametrizing-fixtures) and the [forum](http://software-testing.ru/forum/index.php?/topic/37870-kastomizatciia-parametrizatcii-v-pytest/)(in Russian)

### Note

When outside the project directory
it is common to place the test datafile (like Excel spreadsheet) on Desktop, Downloads and other directories of the current user. However the dataprovider annotation parametes must be constant expressions and test method cannot use class variables or static methods like `File.separator` in annotation value, code like below will not compile:
```java
Test( dataProvider = "Excel", dataProviderClass = ExcelParametersProvider.class)
@DataFileParameters(name = "data_2003.xls", path = (osName.startsWith("windows")) ? "${USERPROFILE}" : "${HOME}" + File.separator + "Desktop" )
```

To workaround this inconvenienve, the __TestNg Data Providers__ internally converts between `${USERPRFILE}` and `${HOME}` and vise versa on Linux and Mac computers therefore the expressions `path = "${USERPROFILE}\\Desktop"` or `path = "${HOMEDIR}/Downloads"` work across OS.

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

  * The default version of the supported Apache POI is __3.17__.
  * Older versions of the package require minor code refactoring. Note that you may also have to clear the other versions of __poi__ and __poi-ooxml__ jars from maven cache '~/.m2/repository'
  * Project can be built with Apache POI 4.0.0 by modifying `poi.version` to `4.0.0` within `&lt;properties&gt;` node in the `pom.xml` - the profile `poi40` does not work correctly.
  * Creating branches and tags is a work in progress.

### Google Sheet Data Provider

This is an experimental provider based on [blog](http://www.seleniumeasy.com/selenium-tutorials/read-data-from-google-spreadsheet-using-api) how to use Google Sheets API to read data from Spreadsheet.

The test method that is about to load the parameters from Google sheet is annotated in a similar fashion as with other providers developed in this project:
```java
@Test(dataProviderClass = GoogleSheetParametersProvider.class, dataProvider = "Google Spreadsheet")
@DataFileParameters(name = "Google Sheets Example", path = "17ImW6iKSF7g-iMvPzeK4Zai9PV-lLvMsZkl6FEkytRg", sheetName = "Test Data", secretFilePath = "/home/sergueik/.secret/client_secret.json", debug = true)
public void testWithGoogleSheet(String strRowNum, String searchKeyword, String strExpectedCount)
    throws InterruptedException {
  double rowNum = Double.parseDouble(strRowNum);
  double expectedCount = Double.parseDouble(strExpectedCount);
  dataTest(searchKeyword, expectedCount);
	}
```

Here the `name` attibute stores the name of the application,
`path` is for the `id` part of the data spreadsheet URL: `https://docs.google.com/spreadsheets/d/${id}`, and optional `sheetName` stores  the name of the sheet.

![Google Sheet](https://raw.githubusercontent.com/sergueik/testng-dataproviders/master/screenshots/google_sheet.png)


The path to secret file that is required to access the API, is to be defined through the `secretFilePath` attribute. Note: like with other attributes, the
the value for annotation attribute `DataFileParameters.secretFilePath` must be a constant expression.
The following would not compile:
```java
  private static final String SECRET_FILEPATH = Paths
    .get(System.getProperty("user.home")).resolve(".secret")
    .resolve("client_secret.json").toAbsolutePath().toString();

  @Test(dataProviderClass = GoogleSheetParametersProvider.class, dataProvider = "Google Spreadsheet")
  @DataFileParameters(name = "Google Sheets Example", secretFilePath = SECRET_FILEPATH, ...)
```
but the following will:
```java
  private static final String SECRET_FILEPATH = "C:/Users/Serguei/.secret/client_secret.json";
```
The secret file:
```js
{
  "installed": {
    "client_id": "XXXXXXXXXXXX-XXXXXXXXXXXXXXXXXXXXXXXXXXX.apps.googleusercontent.com",
    "project_id": "gogle-sheet-api-test-xxxxxxx",
    "auth_uri": "https://accounts.google.com/o/oauth2/auth",
    "token_uri": "https://oauth2.googleapis.com/token",
    "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
    "client_secret": "xxxxxxxxxxxxxxx",
    "redirect_uris": [
      "urn:ietf:wg:oauth:2.0:oob",
      "http://localhost"
    ]
  }
}
```
can be stored anywhere on disk outside of source control e.g. under `~/.secret/client_secret.json`.
It will be loaded once and the credential obtained from oauth would be cached and reused until expiration.
The credential appears to be valid for approximately one hour.
Currently the test opens the browser window prompting the user to confirm the access:

![running test with Google Sheet Data Provider](https://raw.githubusercontent.com/sergueik/testng-dataproviders/master/screenshots/running_google_sheet_example.png)

In the future versions, parallel execution of Google Sheet parameterized tests and a more flexible caching of the access credentials is planned.

### See Also
  * [TestNg Excel Data Provider example](https://www.seleniumeasy.com/testng-tutorials/import-data-from-excel-and-pass-to-data-provider)
  * [JUnit4, JUnit5, TestNG comparison](https://www.baeldung.com/junit-vs-testng), covers test parameteterization amond other features
  * [dataprovider basics](https://howtodoinjava.com/testng/testng-dataprovider/)
  * JUnit4,JUnit5, TestNG comparison, covers [parameteterized tests](https://www.baeldung.com/junit-vs-testng)
  * [poire](https://github.com/ssirekumar/POIRE) - one other API on top of Apache POI to deal with office files
  * parallel testing ["best practices"](https://docs.experitest.com/display/TE/Parallel+Tests+-+Best+Practices)
  * [skip TestNG tests based on condition](https://www.lenar.io/skip-testng-tests-based-condition-using-iinvokedmethodlistener/) interface syntax sugar
  * [reading data from google spreadsheet tutorial](https://www.seleniumeasy.com/selenium-tutorials/read-data-from-google-spreadsheet-using-api)
  * another blog on [testng data providers backed by excel](https://www.uvdesk.com/en/blog/passing-data-dataprovider-excel-sheet-testng/)
  * [apache JMeter Data-Driven Testing](https://dzone.com/articles/implementing-data-driven-testing-using-google-shee) (naturally, in groovy)
  * [Google spreadsheet (older) read method using JAVA](https://dzone.com/articles/reading-data-google)
  * [stackoverflow](https://stackoverflow.com/questions/32860225/read-data-from-google-spreadsheets)
  * [stackoverflow](https://stackoverflow.com/questions/7566836/read-data-from-google-docs-spreadsheets)
  * [Interact with Google Sheets from Java](https://www.baeldung.com/google-sheets-java-client)
  * very detaled [publication](https://gist.github.com/zmts/802dc9c3510d79fd40f9dc38a12bccfc) on Token-Based Authentication and JSON Web Tokens (JWT) (in Russian)
  * POI-backed Excel row/cell generic class member [serialization annotation](https://github.com/ozlerhakan/poiji) support
  * Python resources for interacting with Office Excel file (unverified):
     + https://xlsxwriter.readthedocs.io/getting_started.html
     + https://www.geeksforgeeks.org/reading-excel-file-using-python/
     + https://www.marsja.se/your-guide-to-reading-excel-xlsx-files-in-python/
     + https://www.python-excel.org/
  * [about Compound File Binary format](https://habr.com/ru/post/534126/) (in Rusian)
  * [About Word files](https://habr.com/ru/post/110019/) (in Russian)
  *  https://www.baeldung.com/java-thread-safety
  * .net  [openmcdf](https://github.com/ironfede/openmcdf) assembly to manipulate the OLE structured storage at low level
### TODO

on Linux develpment machine, seem to not be able to launch google tests. After authenticaling o Windows machine, issue disappears
```sh
[ERROR] org.testng.TestNGException:
[ERROR] Cannot find class in classpath: com.github.sergueik.testng.ExcelProviderTest
```
### Author
[Serguei Kouzmine](kouzmine_serguei@yahoo.com)
