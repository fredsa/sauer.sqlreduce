package sqlreduce.server;

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.FetchOptions.Builder;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import sqlreduce.client.RpcService;
import sqlreduce.shared.Constants;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@SuppressWarnings("serial")
public class RpcServiceImpl extends RemoteServiceServlet implements RpcService {

  private static final String SELECT_STAR_FROM = "SELECT * FROM ";

  private DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

  private String t;

  public String executeDatastoreQueries(String namespace, String sql) {
    t = "";
    NamespaceManager.set(namespace);

    String[] queries = sql.split(";");
    for (String query : queries) {
      query = query.trim();
      if (query.length() > 0) {
        try {
          doDatastoreQuery(query);
        } catch (SQLException e) {
          logError(e);
        }
      } else {
        t += "<br>";
      }
    }
    return t;
  }

  public String executeRelationalQueries(String sql) {
    t = "";
    String[] queries = sql.split(";");
    for (String query : queries) {
      query = query.trim();
      if (query.length() > 0) {
        try {
          doRelationalQuery(query);
        } catch (SQLException e) {
          logError(e);
        }
      } else {
        t += "<br>";
      }
    }
    return t;
  }

  public String initRelational() {
    return executeRelationalQueries(Constants.INIT_SQL);
  }

  public String seedDatastore(String namespace) {
    t = "";
    NamespaceManager.set(namespace);

    int ROWS = 20;
    for (int i = 0; i < ROWS; i++) {
      String kind = Constants.KIND;
      Entity entity = new Entity(kind);

      int cust_id = (int) (Math.random() * Constants.MAX_CUST_ID) + 1;
      long time = (long) (System.currentTimeMillis() - Math.random() * 86400 * 30);
      int download = (int) (Math.random() * 30);
      int upload = (int) (Math.random() * 5);

      entity.setProperty("download", download);
      entity.setProperty("upload", upload);
      entity.setProperty("cust_id", cust_id);
      entity.setProperty("time", new Date(time));

      ds.put(entity);
      logResult(dumpEntity(entity));
    }
    logStatus(ROWS + " entities created");
    return t;
  }

  private void doDatastoreQuery(String sql) throws SQLException {
    logQuery(sql);
    sql = sql.replaceAll("\\s+", " ").trim().toUpperCase();
    if (!sql.startsWith(SELECT_STAR_FROM)) {
      logError("Unrecognized GQL query");
      return;
    }
    String kind = sql.substring(SELECT_STAR_FROM.length());
    PreparedQuery prepared = ds.prepare(new Query(kind));
    List<Entity> results = prepared.asList(Builder.withDefaults());
    for (Entity entity : results) {
      String propList = dumpEntity(entity);
      logResult(propList);
    }
    logStatus(results.size() + " results");
  }

  private void doRelationalQuery(String sql) throws SQLException {
    Connection c = Util.getConnection();

    logQuery(sql);
    if (sql.trim().toLowerCase().startsWith("select")) {
      ResultSet query = c.createStatement().executeQuery(sql);
      ResultSetMetaData metaData = query.getMetaData();

      {
        String tt = "";
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
          tt += Util.formatHeader(Constants.LEN, metaData.getColumnName(i));
        }
        logResult(tt);
      }

      int count = 0;
      while (query.next()) {
        count++;
        String tt = "";
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
          tt += Util.formatValue(Constants.LEN, query.getString(i));
        }
        logResult(tt);
      }
      logStatus(count + " results.");
    } else {
      Statement statement = c.createStatement();
      statement.execute(sql);
      logStatus("update count = " + statement.getUpdateCount());
    }
    c.close();
  }

  private String dumpEntity(Entity entity) {
    Map<String, Object> props = entity.getProperties();
    String propList = "Kind=" + entity.getKind() + "(";
    for (Entry<String, Object> entry : props.entrySet()) {
      String name = entry.getKey();
      Object v = entry.getValue();
      propList += name + ": " + Util.propertyValueToString(v);
    }
    propList += ")";
    return propList;
  }

  private void executeUpdate(Connection c, String sql) {
    logQuery(sql);
    try {
      Statement stmt = c.createStatement();
      stmt.executeUpdate(sql);
      logStatus(stmt.getUpdateCount() + " rows updated.");
    } catch (Exception e) {
      logError(e);
    }
  }

  private void log(String className, String message) {
    t += "<div class='" + className + "'>" + message + "</div>";
  }

  private void logError(Exception e) {
    logError(e.getMessage());
  }

  private void logError(String message) {
    log("error", message);
  }

  private void logQuery(String message) {
    log("query", message);
  }

  private void logResult(String message) {
    log("results", message);
  }

  private void logStatus(String message) {
    log("status", message);
  }

}
