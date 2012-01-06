package sqlreduce.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("rpc")
public interface RpcService extends RemoteService {
  String executeDatastoreQueries(String namespace, String sql);

  String executeRelationalQueries(String sql);

  String seedDatastore(String namespace);

  String initRelational();
}
