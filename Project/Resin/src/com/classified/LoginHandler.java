package com.classified;

// From Jason Hunters book p.230

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class LoginHandler extends HttpServlet {

	public void doPost(HttpServletRequest req, HttpServletResponse res)
		throws ServletException, IOException {
    
			res.setContentType("text/html");
			PrintWriter out = res.getWriter();

			// Get the user's name and password
			String name = req.getParameter("name");
			String passwd = req.getParameter("passwd");

			// Check the name and password for validity
			if(!allowUser(name, passwd)){
				out.println("<HTML><HEAD><TITLE>Access Denied</TITLE></HEAD>");
				out.println("<BODY>Your login and password are invalid.<BR>");
				out.println("You may want to <A HREF=\"../classified/login.jsp\">try again</A>");
				out.println("</BODY></HTML>");
			}
			else{
				// Valid login. Make a note in the session object.
				HttpSession session = req.getSession(true);
				//session.putValue("login.isDone", name); // just a marker object
        session.setAttribute("login.isDone", name); // just a marker object

				// Try redirecting the client to the page he first tried to access
				try{
					//String target = (String) session.getValue("login.target");
          String target = (String) session.getAttribute("login.target");

					if (target != null)
            // you are logged in now so go to target page.
						res.sendRedirect("../classified" + target);
					else res.sendRedirect("../classified/ManageClassified.jsp");

					return;
				}catch (Exception ignored){}

				// Couldn't redirect to the target. Redirect to the site's home page.
				res.sendRedirect(req.getScheme() + "://" + 
								req.getServerName() + ":" + req.getServerPort());
			}
		}

	protected boolean allowUser(String user, String passwd) {
		if((user.equals("blueads") && passwd.equals("password")) || 
                (user.equals("blueads") && passwd.equals("blueads"))){
			return true;
		}else{
			return false;
		}
	}
}

