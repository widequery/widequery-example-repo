package com.widequery;

import com.widequery.client.table.ColumnValue;
import com.widequery.client.table.Row;
import com.widequery.client.table.WideTable;
import com.widequery.service.KeyValue;
import com.widequery.service.StoreService;
import com.widequery.wql.SelectQuery;
import com.widequery.wql.template.SelectQueryTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;

public class WideQuerySimpleTest {
  WideTable table;

  WideQuerySimpleTest() {

  }

  private void createTable(String testTable, StoreService storeService) {
    table = new WideTable.Builder(testTable)
      .column("col1", Integer.class)
      .column("col2", Integer.class)
      .column("col3", BigDecimal.class)
      .column("col4", Integer.class)
      //.storeService(storeService)
      .build();
  }

  private void configureSelectQueryTemplates() {
    SelectQueryTemplate selectQueryTemplate1 =
      new SelectQueryTemplate.Builder()
        .selectFrom(table, "col4")
        .where("col1")
        .where("col2")
        .where("col3")
        .build();

    SelectQueryTemplate selectQueryTemplate2 =
      new SelectQueryTemplate.Builder()
        .selectFrom(table, "col1")
        .where("col2")
        .where("col4")
        .build();

    table.addSelectQueryTemplate(selectQueryTemplate1);
    table.addSelectQueryTemplate(selectQueryTemplate2);
  }

  private void injectRowsIntoTable() {
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

  private void runQueries() {
    SelectQuery selectQuery1 =
      new SelectQuery.Builder()
        .selectFrom(table, table.getColumnType("col4"))
        .where("col1", 210)
        .where("col2", 220)
        .where("col3", new BigDecimal("2.0003"))
        .build();

    SelectQuery selectQuery2 =
      new SelectQuery.Builder()
        .selectFrom(table, table.getColumnType("col1"))
        .where("col2", 320)
        .where("col4", 340)
        .build();

    ArrayList<SelectQuery> selectQueries = new ArrayList<>();
    selectQueries.add(selectQuery1);
    selectQueries.add(selectQuery2);

    ArrayList<KeyValue> keyValues = table.execute(selectQueries);

    for (KeyValue keyValue : keyValues){
      System.out.println(keyValue);
    }

  }

  private void test() {
    createTable("TestTable1", new HashmapStoreService());
    configureSelectQueryTemplates();
    injectRowsIntoTable();
    runQueries();
  }

  public static void main(String[] args){

    WideQuerySimpleTest wideQuerySimpleTest = new WideQuerySimpleTest();
    wideQuerySimpleTest.test();

  }

  /////////////////////////////////////////////////////////////////////////////////////////
  //  Key Value Store Service
  /////////////////////////////////////////////////////////////////////////////////////////

  public static class HashmapStoreService implements StoreService {

    private HashMap<String, ColumnValue> cacheMap = new HashMap<>();

    @Override
    public void put(ArrayList<KeyValue> keyValues) {
      for (KeyValue keyValue : keyValues) {
        cacheMap.put(keyValue.getKey(), keyValue.getColumnValue());
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
