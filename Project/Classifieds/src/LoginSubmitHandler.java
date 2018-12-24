//
// From Jason Hunters book p.230
/*
 * This servlet redirects the user to a login page, if they have not logged in.
 * @verson 0.2 Tom Kochanowicz 1/17/03
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
			if(!allowUser(name, passwd)) {
				out.println("<HTML><HEAD><TITLE>Access Denied</TITLE></HEAD>");
				out.println("<BODY>Your login and password are invalid.<BR>");
				out.println("You may want to <A HREF=\"/classified/SubmitLogin.html\">try again</A>");
				out.println("</BODY></HTML>");
			}
			else{
				// Valid login. Make a note in the session object.
				HttpSession session = req.getSession(true);
				//session.putValue("Slogin.isDone", name); // just a marker object
        session.setAttribute("Slogin.isDone", name); // just a marker object

				// Try redirecting the client to the page he first tried to access
				try{
					//String target = (String) session.getValue("Slogin.target");
          String target = (String) session.getAttribute("Slogin.target");

					if (target != null)
						res.sendRedirect(target);
					else res.sendRedirect("/classified/SubmitNow.html");

					return;
				}
				catch (Exception ignored){}

				// Couldn't redirect to the target. Redirect to the site's home page.
				res.sendRedirect(req.getScheme() + "://" + 
								req.getServerName() + ":" + req.getServerPort());
			}
		}

		protected boolean allowUser(String user, String passwd) {
				if((user.equals("blueads") && passwd.equals("password")) ||
				   (user.equals("blueads") && passwd.equals("blueads")) || 
				   (user.equals("Westside") && passwd.equals("Eilene")) || 
				   (user.equals("Ameritrade") && passwd.equals("Ashley"))){
					return true;
				}
				else{
					return false;
				}
		}
}

