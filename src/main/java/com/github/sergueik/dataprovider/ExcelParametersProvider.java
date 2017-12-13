package com.github.sergueik.dataprovider;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
// OLE2 Office Documents
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
// conflicts with org.jopendocument.dom.spreadsheet.Cell;
// import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.util.CellReference;
// Office 2007+ XML
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
// open office
import org.jopendocument.dom.ODDocument;
import org.jopendocument.dom.ODValueType;
import org.jopendocument.dom.spreadsheet.Cell;
import org.jopendocument.dom.spreadsheet.Sheet;
import org.jopendocument.dom.spreadsheet.SpreadSheet;
import org.testng.ITestContext;
import org.testng.annotations.DataProvider;

/**
 * @ExcelParametersProvider container class for testng dataProvider method
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class ExcelParametersProvider {
	private static boolean debug = false;

	@DataProvider(parallel = false, name = "OpenOffice Spreadsheet")
	public static Object[][] createData_from_OpenOfficeSpreadsheet() {

		HashMap<String, String> columns = new HashMap<>();
		List<Object[]> result = new LinkedList<>();

		String fileName = "data.ods";
		String sheetName = "Employee Data";

		try {
			File file = new File(fileName);
			SpreadSheet spreadSheet = SpreadSheet.createFromFile(file);
			// https://www.programcreek.com/java-api-examples/index.php?api=org.jopendocument.dom.spreadsheet.Sheet
			// SpreadSheet spreadSheet = SpreadSheet.get(new ODPackage(inputStream));
			Sheet sheet = (sheetName.isEmpty()) ? spreadSheet.getFirstSheet()
					: spreadSheet.getSheet(sheetName);
			if (debug) {
				System.err
						.println("Reading Open Office Spreadsheet : " + sheet.getName());
			}

			int columnCount = sheet.getColumnCount();
			int rowCount = sheet.getRowCount();
			@SuppressWarnings("rawtypes")
			Cell cell = null;
			for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
				String columnHeader = sheet.getImmutableCellAt(columnIndex, 0)
						.getValue().toString();
				if (StringUtils.isBlank(columnHeader)) {
					break;
				}
				String columnName = CellReference.convertNumToColString(columnIndex);
				columns.put(columnName, columnHeader);
				if (debug) {
					System.err
							.println(columnIndex + " = " + columnName + " " + columnHeader);
				}
			}
			// NOTE: often there may be no ranges defined
			Set<String> rangeeNames = sheet.getRangesNames();
			Iterator<String> rangeNamesIterator = rangeeNames.iterator();

			while (rangeNamesIterator.hasNext()) {
				if (debug) {
					System.err.println("Range = " + rangeNamesIterator.next());
				}
			}
			// isCellBlank has protected access in Table
			for (int rowIndex = 1; rowIndex < rowCount && StringUtils.isNotBlank(sheet
					.getImmutableCellAt(0, rowIndex).getValue().toString()); rowIndex++) {
				List<Object> resultRow = new LinkedList<>();
				for (int columnIndex = 0; columnIndex < columnCount && StringUtils
						.isNotBlank(sheet.getImmutableCellAt(columnIndex, rowIndex)
								.getValue().toString()); columnIndex++) {
					cell = sheet.getImmutableCellAt(columnIndex, rowIndex);
					// TODO: column selection
					/*
					String cellName = CellReference.convertNumToColString(columnIndex);
					if (columns.get(cellName).equals("COUNT")) {
						assertEquals(cell.getValueType(), ODValueType.FLOAT);
						expected_count = Double.valueOf(cell.getValue().toString());
					}
					if (columns.get(cellName).equals("SEARCH")) {
						assertEquals(cell.getValueType(), ODValueType.STRING);
						search_keyword = cell.getTextValue();
					}
					if (columns.get(cellName).equals("ID")) {
						System.err.println("Column: " + columns.get(cellName));
						assertEquals(cell.getValueType(), ODValueType.FLOAT);
						id = Integer.decode(cell.getValue().toString());
					}
					*/
					@SuppressWarnings("unchecked")
					Object cellValue = safeOOCellValue(cell);
					if (debug) {
						System.err.println("Cell Value: " + cellValue.toString() + " "
								+ cellValue.getClass());
					}
					resultRow.add(cellValue);
				}
				result.add(resultRow.toArray());
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		Object[][] resultArray = new Object[result.size()][];
		result.toArray(resultArray);
		return resultArray;
	}

	@DataProvider(parallel = false, name = "Excel 2007")
	public static Object[][] createDataFromExcel2007(final ITestContext context,
			final Method method) {

		// String suiteName = context.getCurrentXmlTest().getSuite().getName();
		System.err.println("Data Provider Caller Suite: "
				+ context.getCurrentXmlTest().getSuite().getName());
		System.err.println(
				"Data Provider Caller Test: " + context.getCurrentXmlTest().getName());
		System.out.println("Data Provider Caller Method: " + method.getName());
		// String testParam =
		// context.getCurrentXmlTest().getParameter("test_param");

		/*
		@SuppressWarnings("deprecation")
		Map<String, String> parameters = (((TestRunner) context).getTest())
				.getParameters();
		for (String key : parameters.keySet()) {
			System.out.println("Data Provider Caller Parameter: " + key + " = "
					+ parameters.get(key));
		}
		*/
		Map<String, String> columns = new HashMap<>();
		List<Object[]> result = new LinkedList<>();
		XSSFWorkbook wb = null;
		Map<String, String> columnHeaders = new HashMap<>();
		String fileName = "data_2007.xlsx";
		String sheetName = "Employee Data";
		try {

			wb = new XSSFWorkbook(fileName);
			XSSFSheet sheet = (sheetName.isEmpty()) ? wb.getSheetAt(0)
					: wb.getSheet(sheetName);

			Iterator<Row> rows = sheet.rowIterator();
			Iterator<org.apache.poi.ss.usermodel.Cell> cells;
			while (rows.hasNext()) {
				XSSFRow row = (XSSFRow) rows.next();
				XSSFCell cell;
				if (row.getRowNum() == 0) {
					cells = row.cellIterator();
					while (cells.hasNext()) {

						cell = (XSSFCell) cells.next();
						int columnIndex = cell.getColumnIndex();
						String columnHeader = cell.getStringCellValue();
						String columnName = CellReference
								.convertNumToColString(cell.getColumnIndex());
						columnHeaders.put(columnName, columnHeader);
						if (debug) {
							System.err.println(
									columnIndex + " = " + columnName + " " + columnHeader);
						}
					}
					// skip the header
					continue;
				}
				List<Object> resultRow = new LinkedList<>();
				cells = row.cellIterator();
				while (cells.hasNext()) {
					cell = (XSSFCell) cells.next();
					// TODO: column selection
					/*
					if (columns.get(cellColumn).equals("ID")) {
						assertEquals(cell.getCellType(), XSSFCell.CELL_TYPE_NUMERIC);
						// id = (int) cell.getNumericCellValue();
					}
					*/
					Object cellValue = safeUserModeCellValue(cell);
					if (debug) {
						System.err.println("Cell Value: " + cellValue.toString() + " "
								+ cellValue.getClass());
					}
					resultRow.add(cellValue);
				}
				result.add(resultRow.toArray());
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (wb != null) {
				try {
					wb.close();
				} catch (IOException e) {
				}
			}
		}
		Object[][] resultArray = new Object[result.size()][];
		result.toArray(resultArray);
		return resultArray;
	}

	@DataProvider(parallel = false, name = "Excel 2003")
	public static Object[][] createDataFromExcel2003() {

		List<Object[]> result = new LinkedList<>();

		String fileName = "data_2003.xls";
		String sheetName = "Employee Data";
		HSSFWorkbook wb = null;
		Iterator<org.apache.poi.ss.usermodel.Cell> cells;
		Map<String, String> columnHeaders = new HashMap<>();

		try {
			InputStream ExcelFileToRead = new FileInputStream(fileName);
			wb = new HSSFWorkbook(ExcelFileToRead);
			HSSFSheet sheet = (sheetName.isEmpty()) ? wb.getSheetAt(0)
					: wb.getSheet(sheetName);
			if (debug) {
				System.err
						.println("Reading Excel 2003 sheet : " + sheet.getSheetName());
			}
			Iterator<Row> rows = sheet.rowIterator();
			while (rows.hasNext()) {
				HSSFRow row = (HSSFRow) rows.next();
				HSSFCell cell;

				if (row.getRowNum() == 0) {
					cells = row.cellIterator();
					while (cells.hasNext()) {

						cell = (HSSFCell) cells.next();
						int columnIndex = cell.getColumnIndex();
						String columnHeader = cell.getStringCellValue();
						String columnName = CellReference
								.convertNumToColString(cell.getColumnIndex());
						columnHeaders.put(columnName, columnHeader);
						if (debug) {
							System.err.println(
									columnIndex + " = " + columnName + " " + columnHeader);
						}
					}
					// skip the header
					continue;
				}

				cells = row.cellIterator();
				List<Object> resultRow = new LinkedList<>();
				while (cells.hasNext()) {
					cell = (HSSFCell) cells.next();
					Object cellValue = safeUserModeCellValue(cell);
					if (debug) {
						System.err.println("Cell Value: " + cellValue.toString() + " "
								+ cellValue.getClass());
					}
					resultRow.add(cellValue);
				}
				result.add(resultRow.toArray());
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (wb != null) {
				try {
					wb.close();
				} catch (IOException e) {
				}
			}
		}
		Object[][] resultArray = new Object[result.size()][];
		result.toArray(resultArray);
		return resultArray;
	}

	// https://www.jopendocument.org/docs/org/jopendocument/dom/ODValueType.html
	public static Object safeOOCellValue(
			org.jopendocument.dom.spreadsheet.Cell<ODDocument> cell) {
		if (cell == null) {
			return null;
		}
		Object result;
		ODValueType type = cell.getValueType();
		switch (type) {
		case FLOAT:
			result = Double.valueOf(cell.getValue().toString());
			break;
		case STRING:
			result = cell.getTextValue();
			break;
		case TIME:
			result = null; // TODO
			break;
		case BOOLEAN:
			result = Boolean.getBoolean(cell.getValue().toString());
			break;
		default:
			throw new IllegalStateException("Can't evaluate cell value");
		}
		// return (result == null) ? null : result.toString();
		return result;
	}

	// Safe conversion of type Excel cell object to Object / String value
	public static Object safeUserModeCellValue(
			org.apache.poi.ss.usermodel.Cell cell) {
		if (cell == null) {
			return null;
		}
		CellType type = cell.getCellTypeEnum();
		Object result;
		switch (type) {
		case _NONE:
			result = null;
			break;
		case NUMERIC:
			result = cell.getNumericCellValue();
			break;
		case STRING:
			result = cell.getStringCellValue();
			break;
		case FORMULA:
			throw new IllegalStateException("The formula cell is not supported");
		case BLANK:
			result = null;
			break;
		case BOOLEAN:
			result = cell.getBooleanCellValue();
			break;
		case ERROR:
			throw new RuntimeException("Cell has an error");
		default:
			throw new IllegalStateException(
					"Cell type: " + type + " is not supported");
		}
		return result;
		// return (result == null) ? null : result.toString();
	}

}
