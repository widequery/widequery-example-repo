package com.widequery.simulator;

import com.widequery.PropertyHelper;
import com.widequery.client.WideQueryBuilder;
import com.widequery.client.table.ColumnValue;
import com.widequery.client.table.Row;
import com.widequery.client.table.WideTable;
import com.widequery.config.ColumnNameClassMaping;
import com.widequery.config.SelectQueryTemplateConfig;
import com.widequery.config.WideTableConfig;
import com.widequery.service.KeyValue;
import com.widequery.service.StoreService;
import com.widequery.service.impl.XIPCKeyValueStoreService;
import com.widequery.wql.SelectQuery;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;

public class WideTableDemoSimulator {

  private String wideTableName;
  private String demoExcelProfileFilename;

  private ExcelReaderWriter excelReaderWriter;

  public WideTableDemoSimulator(String wideTableName, String demoExcelProfileFilename) {
    this.wideTableName = wideTableName;
   this.demoExcelProfileFilename = demoExcelProfileFilename;
  }

  public void run() throws IOException {
    excelReaderWriter = new ExcelReaderWriter(demoExcelProfileFilename);

    excelReaderWriter.open();

    WideTableConfig tableConfig = excelReaderWriter.getTableSchema();
    for (ColumnNameClassMaping columnNameClassMaping: tableConfig.getColumnNameClassMapings()) {
      System.out.println(columnNameClassMaping.getColumnName() + " " + columnNameClassMaping.getClassType());
    }

    ArrayList<SelectQueryTemplateConfig> selectQueryTemplateConfigs = excelReaderWriter.getQueryTemplates();

    for (SelectQueryTemplateConfig selectQueryTemplateConfig : selectQueryTemplateConfigs) {
      ArrayList<String> selectList = selectQueryTemplateConfig.getSelectList();
      ArrayList<String> whereList = selectQueryTemplateConfig.getWhereList();

      for (String selectString: selectList) {
        System.out.println("Select " + selectString);
      }

      for (String whereString: whereList) {
        System.out.println("Where " + whereString);
      }

      System.out.println("--------");
    }

    excelReaderWriter.populateExcelSheet(10, tableConfig);

    //excelReaderWriter.close();
    //WideTable wideTable = WideQueryBuilder.createTable(wideTableName, tableConfig, new HashmapStoreService());
    StoreService storeService = new XIPCKeyValueStoreService(PropertyHelper.getPropValues().get("com.xipc.instanceName"), PropertyHelper.getPropValues().get("com.xipc.username"));
    WideTable wideTable = WideQueryBuilder.createTable(wideTableName, tableConfig, storeService);

    WideQueryBuilder.configureSelectQueryTemplates(wideTable, selectQueryTemplateConfigs);

    injectRowsIntoTable(wideTable);

    runQueries(wideTable);

  }

  private void injectRowsIntoTable(WideTable table) throws IOException {
    //excelReaderWriter.open();

    WideTableConfig tableConfig = excelReaderWriter.getTableSchema();

    ArrayList<ArrayList<ColumnValue>> rows = excelReaderWriter.getRows(tableConfig);

    for (ArrayList<ColumnValue> row : rows){
      Row.Builder builder = new Row.Builder(table);
      for (ColumnValue columnValue : row){
        builder.column(columnValue.getColumnType().getColumnName(), columnValue.getValue());
      }
      Row rowl = builder.build();
      table.insert(rowl);
    }
    excelReaderWriter.close();
  }

  private void runQueries(WideTable table) {
    SelectQuery selectQuery1 =
            new SelectQuery.Builder()
                    .selectFrom(table, table.getColumnType("col1"))
                    .where("col3", new BigDecimal("1.03"))
                    .where("col4", 104)
                    .build();

    SelectQuery selectQuery2 =
            new SelectQuery.Builder()
                    .selectFrom(table, table.getColumnType("col1"))
                    .where("col2", 202)
                    .where("col3", new BigDecimal("2.03"))
                    .build();

    ArrayList<SelectQuery> selectQueries = new ArrayList<>();
    selectQueries.add(selectQuery1);
    selectQueries.add(selectQuery2);

    ArrayList<KeyValue> keyValues = table.execute(selectQueries);

    for (KeyValue keyValue : keyValues){
      System.out.println(keyValue);
    }

  }
  public static void main(String[] args) throws IOException {
    String demoExcelProfileFilename = "src/main/resources/profile2/Table1.xlsx";
    //String demoExcelProfileFilename = "profile1/Table1.xlsx";
    String tableName = "Table1";

    WideTableDemoSimulator simulator = new WideTableDemoSimulator(tableName, demoExcelProfileFilename);
    simulator.run();
  }
  public static class HashmapStoreService implements StoreService {

    private HashMap<String, ColumnValue> cacheMap = new HashMap<>();

    @Override
    public void put(ArrayList<KeyValue> keyValues) {
      for (KeyValue keyValue : keyValues) {
        cacheMap.put(keyValue.getKey(), keyValue.getColumnValue());
        System.out.println("Key=" + keyValue.getKey() + " Value=" + keyValue.getColumnValue());
      }
    }

    @Override
    public ArrayList<KeyValue> get(ArrayList<String> keys) {
      ArrayList<KeyValue> keyValues = new ArrayList<>();

      for (String key: keys){
        ColumnValue columnValue = cacheMap.get(key);
        KeyValue keyValue = new KeyValue(key, columnValue);
        keyValues.add(keyValue);
      }

      return keyValues;
    }
  }
}
