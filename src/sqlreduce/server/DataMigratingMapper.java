package sqlreduce.server;

import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.FetchOptions.Builder;
import com.google.appengine.tools.mapreduce.AppEngineMapper;
import com.google.appengine.tools.mapreduce.DatastoreInputFormat;

import org.apache.hadoop.io.NullWritable;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataMigratingMapper extends AppEngineMapper<Key, Entity, NullWritable, NullWritable> {

  private static final Logger log = Logger.getLogger(DataMigratingMapper.class.getName());
  private Connection connection;

  private String kind;

  @Override
  public void cleanup(Context context) {
    log.warning("Doing per-worker cleanup");
  }

  @Override
  public void map(Key key, Entity entity, Context context) {
    log.warning("Mapping key: " + key);
    Map<String, Object> props = entity.getProperties();
    String paramList = "";
    String valueList = "";
    for (Entry<String, Object> entry : props.entrySet()) {
      if (paramList.length() > 0) {
        paramList += ", ";
        valueList += ", ";
      }
      paramList += entry.getKey();
      valueList += propertyValueToSql(entry.getValue());
    }

    String sql = "INSERT INTO " + kind + " (" + paramList + ") VALUES (" + valueList + ")";
    log.info(sql);
    try {
      try {
        connection.createStatement().executeUpdate(sql);
      } catch (SQLException e) {
        for (Entry<String, Object> entry : props.entrySet()) {
          String s = "ALTER TABLE " + kind + " ADD COLUMN " + entry.getKey() + " "
              + getSqlTypeForProperty(entry.getValue());
          try {
            connection.createStatement().execute(s);
          } catch (Exception ignore) {
          }
        }
        // second try
        connection.createStatement().executeUpdate(sql);
      }
    } catch (SQLException e) {
      log.log(Level.WARNING, "failed to insert values " + valueList + " due to " + e.getMessage());
      context.getCounter(kind, e.getClass().getCanonicalName()).increment(1);
    }

    context.getCounter(kind, "migrated").increment(1);
  }

  @Override
  public void setup(Context context) {
    log.warning("Doing per-worker setup");
    String kind = context.getConfiguration().get(DatastoreInputFormat.ENTITY_KIND_KEY);
    Entity entity = getFirstEntity(kind);

    Map<String, Object> props = entity.getProperties();
    String columnList = "";
    for (Entry<String, Object> entry : props.entrySet()) {
      if (columnList.length() > 0) {
        columnList += ", ";
      }
      columnList += entry.getKey() + " " + getSqlTypeForProperty(entry.getValue());
    }

    Connection c = Util.getConnection();
    String sql = "CREATE TABLE " + kind + "(" + columnList + ")";
    try {
      c.createStatement().execute(sql);
    } catch (SQLException e) {
      log.log(Level.WARNING, "Failed to create table due to " + e.getMessage());
      //      throw new RuntimeException(e);
    } finally {
      try {
        c.close();
      } catch (SQLException ignore) {
      }
    }
  }

  @Override
  public void taskCleanup(Context context) {
    log.warning("Doing per-task cleanup");
    Util.closeConnection(connection);
  }

  @Override
  public void taskSetup(Context context) {
    log.warning("Doing per-task setup");
    kind = context.getConfiguration().get(DatastoreInputFormat.ENTITY_KIND_KEY);
    connection = Util.getConnection();
  }

  private Entity getFirstEntity(String kind) {
    Query query = new Query(kind);
    List<Entity> results = DatastoreServiceFactory.getDatastoreService().prepare(query).asList(
        Builder.withLimit(1));
    return results.get(0);
  }

  private String getSqlTypeForProperty(Object value) {
    if (value instanceof Number) {
      return "NUMERIC";
    } else if (value instanceof Date) {
      return "VARCHAR(200)";
    } else if (value instanceof String) {
      return "VARCHAR(200)";
    } else {
      throw new RuntimeException("Unrecognized property value class: " + value.getClass().getName());
    }
  }

  private String propertyValueToSql(Object value) {
    if (value instanceof Number) {
      return "" + value;
    } else if (value instanceof Date) {
      return "'" + Util.DATE_TIME_FORMAT.format(value) + "'";
    } else if (value instanceof String) {
      return "'" + value + "'";
    } else {
      throw new RuntimeException("Unrecognized property value class: " + value.getClass().getName());
    }
  }

}
