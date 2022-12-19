package com.widequery.simulator.config;

import com.widequery.simulator.ExcelReaderWriter;

import java.io.IOException;

public class ExcelReaderTest {
  public void readTest() throws IOException {
    ExcelReaderWriter excelReader = new ExcelReaderWriter("src/test/resources/Table1.xlsx");
    excelReader.open();
    excelReader.getTableSchema();
    excelReader.close();
  }

}