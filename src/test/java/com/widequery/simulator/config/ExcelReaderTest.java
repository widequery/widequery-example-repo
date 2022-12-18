package com.widequery.simulator.config;

import com.widequery.simulator.ExcelReader;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class ExcelReaderTest {
  @Test
  public void readTest() throws IOException {
    ExcelReader excelReader = new ExcelReader("src/test/resources/Table1.xlsx");
    excelReader.open();
    excelReader.getTableSchema();
    excelReader.close();
  }

}