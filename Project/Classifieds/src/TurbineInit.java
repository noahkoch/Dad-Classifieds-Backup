package com.bfg.services;
 
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.IOException;
import org.apache.turbine.util.TurbineConfig;

public class TurbineInit extends HttpServlet {
    private static boolean inited = false;

    public void init() { 
	if (inited) return;
	System.out.println("Initing Turbine!");
	inited = true;
	String prefix =  getServletContext().getRealPath("/");
	String dir = getInitParameter("turbine-resource-directory");
	TurbineConfig tc = new TurbineConfig(dir, "TurbineResources.properties");
	tc.init();
    }

  public  void doGet(HttpServletRequest req, HttpServletResponse res) {
  }
}
