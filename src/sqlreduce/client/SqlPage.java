package sqlreduce.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
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
import com.google.gwt.user.client.ui.Widget;

import sqlreduce.shared.Constants;

public class SqlPage extends Composite {

  interface MainPageUiBinder extends UiBinder<Widget, SqlPage> {
  }

  private static MainPageUiBinder uiBinder = GWT.create(MainPageUiBinder.class);

  @UiField
  Button dropTables;

  @UiField
  Button empSal;

  @UiField
  Button go;

  @UiField
  Button queryData;

  @UiField
  HTML results;

  @UiField
  TextArea sql;

  private final RpcServiceAsync service;

  public SqlPage(RpcServiceAsync service) {
    this.service = service;
    initWidget(uiBinder.createAndBindUi(this));
    sql.setText(Constants.INITIAL_QUERY);
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    // deferred to deal with Firefox layout issue
    Scheduler.get().scheduleDeferred(new ScheduledCommand() {
      public void execute() {
        sql.setFocus(true);
      }
    });
  }

  @UiHandler("empSal")
  void onEmpSalClick(ClickEvent e) {
    sql.setText(Constants.EMP_SAL_SQL);
  }

  @UiHandler("go")
  void onGoClick(ClickEvent e) {
    execute();
  }

  @UiHandler("dropTables")
  void onInitSqlClick(ClickEvent e) {
    dropTables.setEnabled(false);
    service.initRelational(new AsyncCallback<String>() {

      public void onFailure(Throwable caught) {
        dropTables.setEnabled(true);
        Window.alert("Initialization failed: " + caught.getMessage());
      }

      public void onSuccess(String result) {
        dropTables.setEnabled(true);
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

  @UiHandler("queryData")
  void onQueryDataClick(ClickEvent e) {
    sql.setText(Constants.INITIAL_QUERY);
  }

  @UiHandler("usageReport")
  void onUsageReportClick(ClickEvent e) {
    sql.setText(Constants.USAGE_SQL);
  }

  private void execute() {
    go.setEnabled(false);
    results.setText("");
    service.executeRelationalQueries(sql.getText(), new AsyncCallback<String>() {

      public void onFailure(Throwable caught) {
        results.setStylePrimaryName("error");
        if (caught instanceof RuntimeException) {
          results.setHTML(caught.getMessage());
        } else {
          results.setHTML(caught.toString());
        }
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

}
