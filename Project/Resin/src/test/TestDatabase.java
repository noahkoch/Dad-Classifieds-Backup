package test;

import java.io.*;

import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.naming.*;
import javax.sql.*;

public class TestDatabase extends HttpServlet {
  DataSource pool;

  public void init()
    throws ServletException
  {
    try {
      Context env = (Context) new InitialContext().lookup("java:comp/env");

      pool = (DataSource) env.lookup("jdbc/test");

      if (pool == null)
        throw new ServletException("`jdbc/test' is an unknown DataSource");
    } catch (NamingException e) {
      throw new ServletException(e);
    }
  }

  public void doGet(HttpServletRequest req,
                    HttpServletResponse res)
    throws IOException, ServletException
  {
    res.setContentType("text/html");
    PrintWriter out = res.getWriter();

    Connection conn = null;
    try {
      conn = pool.getConnection();

      Statement stmt = conn.createStatement();

      ResultSet rs = stmt.executeQuery("select NAME, PRICE from BROOMS");

      out.println("Brooms:<br>");
      while (rs.next()) {
        out.print(rs.getString(1));
        out.print(" ");
        out.print(rs.getInt(2));
        out.println("<br>");
      }

      rs.close();
      stmt.close();
    } catch (SQLException e) {
      throw new ServletException(e);
    } finally {
      try {
        if (conn != null)
          conn.close();
      } catch (SQLException e) {
      }
    }
  }
}

