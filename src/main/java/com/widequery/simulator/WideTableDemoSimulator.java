package com.widequery.simulator;

import com.widequery.client.WideQueryBuilder;
import com.widequery.client.table.Row;
import com.widequery.client.table.WideTable;
import com.widequery.config.SelectQueryTemplateConfig;
import com.widequery.config.WideTableConfig;

import java.math.BigDecimal;
import java.util.ArrayList;

public class WideTableDemoSimulator {

  private String wideTableName;
  private String demoExcelProfileFilename;

  public WideTableDemoSimulator(String wideTableName, String demoExcelProfileFilename) {
    this.wideTableName = wideTableName;
   this.demoExcelProfileFilename = demoExcelProfileFilename;
  }

  public void run() {
    ExcelReader excelReader = new ExcelReader(demoExcelProfileFilename);

    WideTableConfig tableConfig = excelReader.getTableSchema();
    ArrayList<SelectQueryTemplateConfig> selectQueryTemplateConfigs = excelReader.getQueryTemplates();

    WideTable wideTable = WideQueryBuilder.createTable(wideTableName, tableConfig);
    WideQueryBuilder.configureSelectQueryTemplates(wideTable, selectQueryTemplateConfigs);

    injectRowsIntoTable(wideTable);
  }

  private void injectRowsIntoTable(WideTable table) {
    Row row = new Row.Builder(table)
      .column("col1", 110)
      .column("col2", 120)
      .column("col3", new BigDecimal("1.0003"))
      .column("col4", 140)
      .build();

    table.insert(row);

    row = new Row.Builder(table)
      .column("col1", 210)
      .column("col2", 220)
      .column("col3", new BigDecimal("2.0003"))
      .column("col4", 240)
      .build();

    table.insert(row);

    row = new Row.Builder(table)
      .column("col1", 310)
      .column("col2", 320)
      .column("col3", new BigDecimal("3.0003"))
      .column("col4", 340)
      .build();

    table.insert(row);

    row = new Row.Builder(table)
      .column("col1", 410)
      .column("col2", 420)
      .column("col3", new BigDecimal("4.0004"))
      .column("col4", 440)
      .build();

    table.insert(row);
  }

  public static void main(String[] args){
    String demoExcelProfileFilename = "src/main/resources/profile1/Table1.xlsx";
    String tableName = "Table1";

    WideTableDemoSimulator simulator = new WideTableDemoSimulator(tableName, demoExcelProfileFilename);
    simulator.run();
  }
}
