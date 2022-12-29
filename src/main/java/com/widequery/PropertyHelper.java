package com.widequery;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class PropertyHelper {

  private static InputStream inputStream;

  public static Map<String, String> getPropValues() throws IOException {
    Map<String, String> properties = new HashMap<>();
    FileInputStream fis = null;

    try {
      String rootPath = Thread.currentThread().getContextClassLoader().getResource("config/").getPath();
      System.out.println(rootPath);
      String appConfigPath = rootPath + "config.properties";

      Properties appProps = new Properties();
      fis = new FileInputStream(appConfigPath);
      appProps.load(fis);

      String xipcInstanceName = appProps.getProperty("com.xipc.instanceName");
      String username = appProps.getProperty("com.xipc.username");

      properties.put("com.xipc.instanceName", xipcInstanceName);
      properties.put("com.xipc.username", username);

    } catch (Exception e) {
      System.out.println("Exception: " + e);
    } finally {
      fis.close();
    }
    return properties;
  }
}
