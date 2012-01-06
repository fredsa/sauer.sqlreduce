package sqlreduce.server;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class MainPageServlet extends RemoteServiceServlet {
  private UserService userService;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
      IOException {
    userService = UserServiceFactory.getUserService();

    // User requested logout, so let's do it
    if (currentUri(req).startsWith("/logout")) {
      resp.sendRedirect(userService.createLogoutURL("/"));
      return;
    }

    // User is not logged in, so redirect to login page
    if (!userService.isUserLoggedIn()) {
      resp.sendRedirect(userService.createLoginURL(currentUri(req)));
      return;
    }

    // User is not authorized, so apologize
    if (!userService.isUserAdmin()
        && !userService.getCurrentUser().getEmail().toLowerCase().endsWith("@google.com")) {
      resp.setContentType("text/html");
      resp.getWriter().print(
          "<html><body>Sorry, you are not an administrator. Click here to <a href='"
              + userService.createLogoutURL(currentUri(req)) + "'>logout</a>.");
      return;
    }

    // Yah. Show the page
    resp.setContentType("text/html");
    InputStream in = getClass().getResourceAsStream("Sqlreduce.html");
    byte[] b = new byte[8192];
    boolean done = false;
    while (in.available() > 0) {
      int count = in.read(b);
      resp.getOutputStream().write(b, 0, count);
    }
    in.available();
    in.read(b);
  }

  private String currentUri(HttpServletRequest req) {
    String queryString = req.getQueryString();
    String uri = req.getRequestURI();
    return queryString == null ? uri : uri + "?" + queryString;
  }
}