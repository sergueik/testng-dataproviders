package com.github.sergueik.dataprovider;
/**
 * Copyright 2017 Serguei Kouzmine
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellReference;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.jopendocument.dom.ODDocument;
import org.jopendocument.dom.ODPackage;
import org.jopendocument.dom.ODValueType;
import org.jopendocument.dom.spreadsheet.Cell;
import org.jopendocument.dom.spreadsheet.Sheet;
import org.jopendocument.dom.spreadsheet.SpreadSheet;

/**
 * Common utilities class for testng dataProviders
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class Utils {

	private static Utils instance = new Utils();

	private Utils() {
	}

	public static Utils getInstance() {
		return instance;
	}

	private String sheetName;
	private String columnNames = "*";
	private boolean debug = false;

	public void setSheetName(String sheetName) {
		this.sheetName = sheetName;
	}

	public void setColumnNames(String columnNames) {
		this.columnNames = columnNames;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public static String resolveEnvVars(String input) {
		if (null == input) {
			return null;
		}
		Pattern p = Pattern.compile("\\$(?:\\{(\\w+)\\}|(\\w+))");
		Matcher m = p.matcher(input);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			String envVarName = null == m.group(1) ? m.group(2) : m.group(1);
			String envVarValue = getPropertyEnv(envVarName, null);
			m.appendReplacement(sb,
					null == envVarValue ? "" : envVarValue.replace("\\", "\\\\"));
		}
		m.appendTail(sb);
		return sb.toString();
	}

	// origin:
	// https://github.com/TsvetomirSlavov/wdci/blob/master/code/src/main/java/com/seleniumsimplified/webdriver/manager/EnvironmentPropertyReader.java
	public static String getPropertyEnv(String name, String defaultValue) {
		String value = System.getProperty(name);
		if (value == null) {
			value = System.getenv(name);
			if (value == null) {
				value = defaultValue;
			}
		}
		return value;
	}

	public List<Object[]> createDataFromOpenOfficeSpreadsheet(
			SpreadSheet spreadSheet) {
		HashMap<String, String> columns = new HashMap<>();
		List<Object[]> result = new LinkedList<>();
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
			String columnHeader = sheet.getImmutableCellAt(columnIndex, 0).getValue()
					.toString();
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
					.isNotBlank(sheet.getImmutableCellAt(columnIndex, rowIndex).getValue()
							.toString()); columnIndex++) {
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
		return result;
	}

	public List<Object[]> createDataFromOpenOfficeSpreadsheet(
			InputStream inputStream) {
		List<Object[]> result = new LinkedList<>();
		try {
			// https://www.programcreek.com/java-api-examples/index.php?api=org.jopendocument.dom.spreadsheet.Sheet
			SpreadSheet spreadSheet = SpreadSheet.get(new ODPackage(inputStream));
			result = createDataFromOpenOfficeSpreadsheet(spreadSheet);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		return result;
	}

	public List<Object[]> createDataFromOpenOfficeSpreadsheet(String filePath) {

		List<Object[]> result = new LinkedList<>();

		try {
			File file = new File(filePath);
			SpreadSheet spreadSheet = SpreadSheet.createFromFile(file);
			result = createDataFromOpenOfficeSpreadsheet(spreadSheet);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		return result;
	}

	public List<Object[]> createDataFromExcel2003(HSSFWorkbook workBook) {
		List<Object[]> result = new LinkedList<>();

		Iterator<org.apache.poi.ss.usermodel.Cell> cells;
		Map<String, String> columnHeaders = new HashMap<>();
		HSSFSheet sheet = (sheetName.isEmpty()) ? workBook.getSheetAt(0)
				: workBook.getSheet(sheetName);
		if (debug) {
			System.err.println("Reading Excel 2003 sheet : " + sheet.getSheetName());
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
						System.err
								.println(columnIndex + " = " + columnName + " " + columnHeader);
					}
				}
				// skip the header
				continue;
			}

			cells = row.cellIterator();
			List<Object> resultRow = new LinkedList<>();
			if (cells.hasNext()) {
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
		}
		return result;
	}

	public List<Object[]> createDataFromExcel2003(String filePath) {

		List<Object[]> result = new LinkedList<>();
		HSSFWorkbook workBook = null;

		try {
			InputStream ExcelFileToRead = new FileInputStream(filePath);
			workBook = new HSSFWorkbook(ExcelFileToRead);
			result = createDataFromExcel2003(workBook);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (workBook != null) {
				try {
					workBook.close();
				} catch (IOException e) {
				}
			}
		}
		return result;
	}

	public List<Object[]> createDataFromExcel2003(InputStream inputStream) {

		List<Object[]> result = new LinkedList<>();
		HSSFWorkbook workBook = null;

		try {
			workBook = new HSSFWorkbook(inputStream);
			result = createDataFromExcel2003(workBook);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (workBook != null) {
				try {
					workBook.close();
				} catch (IOException e) {
				}
			}
		}
		return result;
	}

	public List<Object[]> createDataFromExcel2007(XSSFWorkbook workBook) {
		List<Object[]> result = new LinkedList<>();
		Map<String, String> columns = new HashMap<>();
		XSSFSheet sheet = (sheetName.isEmpty()) ? workBook.getSheetAt(0)
				: workBook.getSheet(sheetName);

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
					columns.put(columnName, columnHeader);
					if (debug) {
						System.err
								.println(columnIndex + " = " + columnName + " " + columnHeader);
					}
				}
				// skip the header
				continue;
			}
			List<Object> resultRow = new LinkedList<>();
			cells = row.cellIterator();
			if (cells.hasNext()) {
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
		}
		return result;
	}

	public List<Object[]> createDataFromExcel2007(String filePath) {
		List<Object[]> result = new LinkedList<>();
		XSSFWorkbook workBook = null;
		try {
			workBook = new XSSFWorkbook(filePath);
			result = createDataFromExcel2007(workBook);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (workBook != null) {
				try {
					workBook.close();
				} catch (IOException e) {
				}
			}
		}
		return result;
	}

	public List<Object[]> createDataFromExcel2007(InputStream inputStream) {

		List<Object[]> result = new LinkedList<>();
		XSSFWorkbook workBook = null;

		try {
			workBook = new XSSFWorkbook(inputStream);
			result = createDataFromExcel2007(workBook);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (workBook != null) {
				try {
					workBook.close();
				} catch (IOException e) {
				}
			}
		}
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

}
