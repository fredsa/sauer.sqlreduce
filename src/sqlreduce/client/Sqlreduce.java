package sqlreduce.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.RootPanel;

public class Sqlreduce implements EntryPoint {

  private final RpcServiceAsync service = GWT.create(RpcService.class);

  public void onModuleLoad() {
    RootPanel.get("loading").removeFromParent();
    final DatastorePage datastorePage = new DatastorePage(service);
    final SqlPage sqlPage = new SqlPage(service);

    History.addValueChangeHandler(new ValueChangeHandler<String>() {
      public void onValueChange(ValueChangeEvent<String> event) {
        if ("sql".equals(event.getValue())) {
          final RootLayoutPanel root = RootLayoutPanel.get();
          root.add(sqlPage);
        } else if ("datastore".equals(event.getValue())) {
          final RootLayoutPanel root = RootLayoutPanel.get();
          root.add(datastorePage);
        } else if ("both".equals(event.getValue())) {
          final RootLayoutPanel root = RootLayoutPanel.get();
          root.add(datastorePage);
          root.add(sqlPage);
          root.setWidgetLeftWidth(datastorePage, 0, Unit.PX, 50, Unit.PCT);
          root.setWidgetRightWidth(sqlPage, 0, Unit.PX, 50, Unit.PCT);
        } else {
          Window.Location.reload();
        }
      }
    });
    if (History.getToken().length() > 0) {
      History.fireCurrentHistoryState();
    } else {
      History.newItem("both");
    }

  }
}
