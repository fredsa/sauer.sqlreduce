package sqlreduce.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface RpcServiceAsync {
  void executeDatastoreQueries(String namespace, String sql, AsyncCallback<String> callback);

  void executeRelationalQueries(String sql, AsyncCallback<String> callback);

  void seedDatastore(String namespace, AsyncCallback<String> callback);

  void initRelational(AsyncCallback<String> callback);
}
