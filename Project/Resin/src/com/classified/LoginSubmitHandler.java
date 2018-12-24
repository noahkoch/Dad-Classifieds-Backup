package com.classified;

/**
 * LoginSubmitHandler.java 
 * @version 0.2 1/28/03
 * This servlet works with the SubmitNowDBServlet, SubmitLogin.jsp
 * and the SubmitNow.jsp java server pages. What it does is makes 
 * the user login before they can submit any ads by redirecting them
 * to a login page.
 * Example reference from Jason Hunters book p.230
 * 
 */

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class LoginSubmitHandler extends HttpServlet {

	public void doPost(HttpServletRequest req, HttpServletResponse res)
		throws ServletException, IOException {
			res.setContentType("text/html");
			PrintWriter out = res.getWriter();

			// Get the user's name and password
			String name = req.getParameter("name");
			String passwd = req.getParameter("passwd");

			// Check the name and password for validity
			if(!allowUser(name, passwd)){
///////////////   START
        out.println("<html><head><br>");
        // SET THE BASE HREF for a reference point for HREF
        out.println("<base href=http://1.1.1.33" + req.getContextPath() + "/classified/ ><br>");
        out.println("<title>Access Denied</title></head>");
        out.println("<body>Your login or password is invalid.<br>");
        out.println("You may want to <a href=\"/SubmitLogin.jsp\">try again</a>");
        out.println("</body></head><br>");
/////////////   END 
/////////////////TEST LINE BELOW ////////////////
System.out.println("This contextPath from LoginSubmitHandler" + req.getContextPath());

/**      
 *  ORIGINAL
				out.println("<HTML><HEAD><TITLE>Access Denied</TITLE></HEAD>");
				out.println("<BODY>Your login and password are invalid.<BR>");
				out.println("You may want to <A HREF=\"/login.jsp\">try again</A>");
				out.println("</BODY></HTML>");
*/
			}
			else{
				// Valid login. Make a note in the session object.
				HttpSession session = req.getSession(true);
				session.setAttribute("Slogin.isDone", name); // just a marker object

				// Try redirecting the client to the page he first tried to access
				try{
            String target = (String) session.getAttribute("Slogin.target");

            if (target != null)
              res.sendRedirect(target);
              else res.sendRedirect("../SubmitNow.jsp");
              
            return;
				}catch (Exception ex){
          System.out.println("Exception LoginSubmitHandler" + ex);
        }

          // Couldn't redirect to the target. Redirect to the site's home page.
          res.sendRedirect(req.getScheme() + "://" + 
								req.getServerName() + ":" + req.getServerPort());
			}
		}

		protected boolean allowUser(String user, String passwd){
				if((user.equals("blueads") && passwd.equals("password")) ||
				   (user.equals("blueads") && passwd.equals("blueads")) || 
				   (user.equals("Westside") && passwd.equals("Eilene")) || 
				   (user.equals("Ameritrade") && passwd.equals("Ashley"))){
					return true;
				}else{
					return false;
				}
		}
}

