package sqlreduce.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import sqlreduce.shared.Constants;

public class DatastorePage extends Composite {

  interface DatastorePageUiBinder extends UiBinder<Widget, DatastorePage> {
  }

  private static final String NAMESPACE_HINT_TEXT = "Namespace";

  private static DatastorePageUiBinder uiBinder = GWT.create(DatastorePageUiBinder.class);

  @UiField
  Button go;

  @UiField
  TextBox namespace;

  @UiField
  Button queryData;

  @UiField
  HTML results;

  @UiField
  Button seedDatastore;

  @UiField
  TextArea sql;

  private final RpcServiceAsync service;

  public DatastorePage(RpcServiceAsync service) {
    this.service = service;
    initWidget(uiBinder.createAndBindUi(this));
    sql.setText(Constants.INITIAL_QUERY);
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    if (getNamespace().length() == 0) {
      namespace.setText(NAMESPACE_HINT_TEXT);
      namespace.getElement().getStyle().setColor("gray");
    }
  }

  @UiHandler("go")
  void onGoClick(ClickEvent e) {
    execute();
  }

  @UiHandler("seedDatastore")
  void onInitDatastoreClick(ClickEvent e) {
    seedDatastore.setEnabled(false);
    service.seedDatastore(getNamespace(), new AsyncCallback<String>() {

      public void onFailure(Throwable caught) {
        seedDatastore.setEnabled(true);
        Window.alert("Seeding datastore failed: " + caught.getMessage());
      }

      public void onSuccess(String result) {
        seedDatastore.setEnabled(true);
        results.setHTML(result);
      }
    });
  }

  @UiHandler("sql")
  void onKeyDown(KeyDownEvent e) {
    if (e.getNativeKeyCode() == KeyCodes.KEY_ENTER && e.isAnyModifierKeyDown()) {
      e.preventDefault();
      execute();
    }
  }

  @UiHandler("namespace")
  void onNamespaceFocus(FocusEvent e) {
    if (NAMESPACE_HINT_TEXT.equals(namespace.getText())) {
      namespace.setText("");
      namespace.getElement().getStyle().setColor("");
    }
  }

  @UiHandler("queryData")
  void onQueryDataClick(ClickEvent e) {
    sql.setText(Constants.INITIAL_QUERY);
  }

  private void execute() {
    go.setEnabled(false);
    results.setText("");
    service.executeDatastoreQueries(getNamespace(), sql.getText(), new AsyncCallback<String>() {

      public void onFailure(Throwable caught) {
        results.setStylePrimaryName("error");
        results.setHTML(caught.getClass().getName());
        go.setEnabled(true);
      }

      public void onSuccess(String result) {
        results.setHTML(result);
        sql.setFocus(true);
        sql.selectAll();
        go.setEnabled(true);
      }
    });
  }

  private String getNamespace() {
    String ns = namespace.getText();
    if (NAMESPACE_HINT_TEXT.equals(ns)) {
      return "";
    }
    return ns;
  }

}
