package com.widequery.simulator;

import com.widequery.client.table.ColumnType;
import com.widequery.client.table.ColumnValue;
import com.widequery.config.ColumnNameClassMaping;
import com.widequery.config.KeyValueConfig;
import com.widequery.config.SelectQueryTemplateConfig;
import com.widequery.config.WideTableConfig;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

public class ExcelReaderWriter {

  private String filename;
  private File file;
  private XSSFWorkbook workbook;

  private FileInputStream fis;
  private FileOutputStream fos;

  private static String TABLE_SCHEMA_SHEET = "TableSchema";
  private static String QUERY_TEMPLATE_SHEET = "QueryTemplate";

  public ExcelReaderWriter(String filename) {
    this.filename = filename;
  }

  public void open() throws IOException {

    file = new File(filename);

    if (!file.exists())
      throw new RuntimeException(filename + " does not exist");

    if (!file.isFile())
      throw new RuntimeException(filename + " can't be opened");

    fis = new FileInputStream(file);

    // Getting the workbook instance for XLSX file
    this.workbook = new XSSFWorkbook(fis);

    System.out.println(filename + " open");
    //fos = new FileOutputStream(file);

  }

  public void close() throws IOException {
    if (fis != null)
      fis.close();
    if (fos != null)
      fos.close();
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

  public void populateExcelSheet(int numRows, WideTableConfig wideTableConfig) throws IOException {
    XSSFSheet sheet = workbook.getSheet("Data");
    if(sheet != null) return;

    fos = new FileOutputStream(file);


    sheet = workbook.createSheet("Data");

    ArrayList<ColumnNameClassMaping> columnNameClassMapings = wideTableConfig.getColumnNameClassMapings();

    for (int rowNum=0; rowNum < numRows; rowNum++){
      int colNum = 0;
      XSSFRow row = sheet.createRow(rowNum);

      for (ColumnNameClassMaping columnNameClassMaping : columnNameClassMapings){
        Class classType = columnNameClassMaping.getClassType();

        if (classType.equals(Integer.class)) {
          Integer integer = Integer.valueOf((rowNum+1)*100 + (colNum+1));
          XSSFCell cell = row.createCell(colNum, XSSFCell.CELL_TYPE_NUMERIC);
          cell.setCellValue(integer);
        } else if (classType.equals(BigDecimal.class)){
          BigDecimal bigDecimal = new BigDecimal((rowNum+1) + ".0" + (colNum+1));
          XSSFCell cell = row.createCell(colNum, XSSFCell.CELL_TYPE_STRING);
          CellStyle cellStyle = workbook.createCellStyle();
          cellStyle.setAlignment(CellStyle.ALIGN_RIGHT);
          cell.setCellStyle(cellStyle);
          cell.setCellValue(bigDecimal.toString());
        }
        colNum++;
      }
    }

    workbook.write(fos);
  }

  public ArrayList<ArrayList<ColumnValue>> getRows(WideTableConfig wideTableConfig) {

    XSSFSheet sheet = workbook.getSheet("Data");
    ArrayList<ArrayList<ColumnValue>> rows = new ArrayList<>();

    ArrayList<ColumnNameClassMaping> columnNameClassMapings = wideTableConfig.getColumnNameClassMapings();

    for (int rowNum = sheet.getFirstRowNum(); rowNum <= sheet.getLastRowNum(); rowNum++){
      XSSFRow xRow = sheet.getRow(rowNum);

      int colNum = 0;
      ArrayList<ColumnValue> columnValues = new ArrayList<>();

      for (ColumnNameClassMaping columnNameClassMaping : columnNameClassMapings){
        String columnName = columnNameClassMaping.getColumnName();
        Class classType = columnNameClassMaping.getClassType();
        ColumnType columnType = new ColumnType(columnName, classType);

        if (classType.equals(Integer.class)) {
          int value = (int) xRow.getCell(colNum++).getNumericCellValue();
          ColumnValue columnValue = new ColumnValue(columnType, Integer.valueOf(value));
          columnValues.add(columnValue);
        } else  if (classType.equals(BigDecimal.class)) {
          String value = xRow.getCell(colNum++).getStringCellValue();
          BigDecimal bigDecimal = new BigDecimal(value);
          ColumnValue columnValue = new ColumnValue(columnType, bigDecimal);
          columnValues.add(columnValue);
        }
      }

      rows.add(columnValues);
    }

    return rows;
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
          selectList.add(colIndexColNameMap.get(queryColIndex));
        if (cell.getStringCellValue().equals("WHERE"))
          whereList.add(colIndexColNameMap.get(queryColIndex));
      }
      selectQueryTemplateConfigs.add(new SelectQueryTemplateConfig(selectList, whereList));
    }

    return selectQueryTemplateConfigs;
  }

  public static void main(String[] args) throws IOException {
    ExcelReaderWriter excelReader = new ExcelReaderWriter("src/main/resources/profile1/Table1.xlsx");
    excelReader.open();
    excelReader.getTableSchema();
    excelReader.getQueryTemplates();
    excelReader.close();
  }
}
