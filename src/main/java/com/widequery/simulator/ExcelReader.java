package com.widequery.simulator;

import com.widequery.config.KeyValueConfig;
import com.widequery.config.SelectQueryTemplateConfig;
import com.widequery.config.WideTableConfig;
import com.widequery.service.KeyValue;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFTable;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class ExcelReader {

  private String filename;
  private XSSFWorkbook workbook;

  private FileInputStream fis;

  private static String TABLE_SCHEMA_SHEET = "TableSchema";
  private static String QUERY_TEMPLATE_SHEET = "QueryTemplate";

  public ExcelReader(String filename) {
    this.filename = filename;
  }

  public void open() throws IOException {

    File file = new File(filename);

    if (!file.exists())
      throw new RuntimeException(filename + " does not exist");

    if (!file.isFile())
      throw new RuntimeException(filename + " can't be opened");

    fis = new FileInputStream(file);

    // Getting the workbook instance for XLSX file
    this.workbook = new XSSFWorkbook(fis);

    System.out.println(filename + " open");
  }

  public void close() throws IOException {
    fis.close();
  }

  public WideTableConfig getTableSchema() {
    XSSFSheet sheet = workbook.getSheet(TABLE_SCHEMA_SHEET);

    List<XSSFTable> tables = sheet.getTables();

    XSSFTable table = tables.get(0);

    int startRowIndex = table.getStartRowIndex() + 1;
    int endRowIndex = table.getEndRowIndex();

    int startColumnIndex = table.getStartColIndex();

    ArrayList<KeyValueConfig> colNameTypeKeyValues = new ArrayList<>();

    for (int i = startRowIndex; i <= endRowIndex; i++) {
      Row row = sheet.getRow(i);
      String colname = row.getCell(startColumnIndex).getStringCellValue();
      String type = row.getCell(startColumnIndex + 1).getStringCellValue();
      colNameTypeKeyValues.add(new KeyValueConfig(colname, type));
    }

    return new WideTableConfig(table.getName(), colNameTypeKeyValues);
  }

  public ArrayList<SelectQueryTemplateConfig> getQueryTemplates() {
    XSSFSheet sheet = workbook.getSheet(QUERY_TEMPLATE_SHEET);

    List<XSSFTable> tables = sheet.getTables();

    XSSFTable table = tables.get(0);
    int rowCount = table.getRowCount();
    System.out.println("Num Rows = " + rowCount);

    int startRowIndex = table.getStartRowIndex();
    int endRowIndex = table.getEndRowIndex();

    int startColumnIndex = table.getStartColIndex();
    int endColumnIndex = table.getEndColIndex();

    Map<Integer, String> colIndexColNameMap = new HashMap<>();
    ArrayList<String> columnNames = new ArrayList<>();
    Row columnNamesRow = sheet.getRow(startRowIndex);

    for (int colnameIndex = (startColumnIndex + 1); colnameIndex <= endColumnIndex; colnameIndex++) {
      String colName = columnNamesRow.getCell(colnameIndex).getStringCellValue();
      columnNames.add(colName);
      colIndexColNameMap.put(colnameIndex, colName);
    }

    ArrayList<SelectQueryTemplateConfig> selectQueryTemplateConfigs = new ArrayList<>();

    for (int i = (startRowIndex + 1); i <= endRowIndex; i++) {
      Row queryRow = sheet.getRow(i);
      ArrayList<String> selectList = new ArrayList<>();
      ArrayList<String> whereList = new ArrayList<>();
      for (int queryColIndex = (startColumnIndex + 1); queryColIndex <= endColumnIndex; queryColIndex++) {
        Cell cell = queryRow.getCell(queryColIndex);
        if (cell == null) continue;
        if (cell.getStringCellValue().equals("SELECT"))
          selectList.add(cell.getStringCellValue() + "=" + colIndexColNameMap.get(queryColIndex));
        if (cell.getStringCellValue().equals("WHERE"))
          whereList.add(cell.getStringCellValue() + "=" + colIndexColNameMap.get(queryColIndex));
      }
      selectQueryTemplateConfigs.add(new SelectQueryTemplateConfig(selectList, whereList));
    }

    return selectQueryTemplateConfigs;
  }

  public static void main(String[] args) throws IOException {
    ExcelReader excelReader = new ExcelReader("src/main/resources/profile1/Table1.xlsx");
    excelReader.open();
    excelReader.getTableSchema();
    excelReader.getQueryTemplates();
    excelReader.close();
  }
}
