package sqlreduce.server;

import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.tools.mapreduce.AppEngineMapper;
import com.google.appengine.tools.mapreduce.DatastoreInputFormat;

import org.apache.hadoop.io.NullWritable;

import java.sql.Connection;
import java.util.logging.Logger;

public class DeleteKindMapper extends AppEngineMapper<Key, Entity, NullWritable, NullWritable> {

  private static final Logger log = Logger.getLogger(DeleteKindMapper.class.getName());
  private Connection connection;

  private String kind;

  @Override
  public void cleanup(Context context) {
    log.warning("Doing per-worker cleanup");
  }

  @Override
  public void map(Key key, Entity entity, Context context) {
    log.warning("Mapping key: " + key);
    DatastoreServiceFactory.getDatastoreService().delete(key);
    context.getCounter(kind, "delete").increment(1);
  }

  @Override
  public void setup(Context context) {
    log.warning("Doing per-worker setup");
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

}
