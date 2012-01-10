package sqlreduce.server;

import com.google.appengine.api.rdbms.AppEngineDriver;

import sqlreduce.shared.Constants;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Util {
  static DateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyyMMdd HH:mm:ss");

  static final String RDBMS_CONNECT_STRING = "jdbc:google:rdbms://google.com:sqlreduce/sqlreduce";

  private static final Logger log = Logger.getLogger(Util.class.getName());

  static {
    try {
      DriverManager.registerDriver(new AppEngineDriver());
    } catch (SQLException e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  public static void closeConnection(Connection connection) {
    try {
      connection.close();
    } catch (SQLException e) {
      log.log(Level.SEVERE, "failed to close database connection", e);
    }
  }

  public static Connection getConnection() {
    try {
      Connection c = DriverManager.getConnection(Util.RDBMS_CONNECT_STRING, "sa", null);

      try {
        Statement stmt = c.createStatement();
        stmt.executeUpdate("create database fred");
      } catch (Exception ignore) {
      }

      try {
        c.setCatalog("fred");
      } catch (Exception ignore) {
      }

      return c;
    } catch (SQLException e) {
      log.log(Level.SEVERE, "failed to open database connection", e);
      throw new RuntimeException(e);
    }
  }
  public static String propertyValueToString(Object v) {
    String value1;
    if (v instanceof Date) {
      value1 = sizeValue(20, DATE_TIME_FORMAT.format((Date) v));
    } else if (v instanceof Integer) {
      value1 = sizeValue(5, "" + v);
    } else {
      value1 = sizeValue(Constants.LEN, "" + v);
    }
    String value = value1;
    return formatValue(value);
  }

  static String formatHeader(int len, String value) {
    value = sizeValue(len, value);
    return "<span class='header value'>" + value + "</span>";
  }

  static String formatValue(int len, String value) {
    value = sizeValue(len, value);
    return formatValue(value);
  }

  private static String formatValue(String value) {
    return "<span class='value'>" + value + "</span>";
  }

  private static String sizeValue(int len, String value) {
    if (value == null) {
      value = "null";
    }
    if (value.length() > len) {
      value = value.substring(0, len - 3) + "...";
    }
    value += "                                                    ";
    value = value.substring(0, len);
    return value;
  }

}
