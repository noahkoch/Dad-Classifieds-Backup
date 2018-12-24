
// From Jason Hunters book p.230

/**
 * File: LoginUserHandler
 * This servlet checks the database to see if a user is logged in. If the user
 * is not logged in, they will be redirected to a login page. It works by checking
 * the database for the users name and password. If it can be found in the database
 * a session object is set and they are allowed to access the page.
 * Note: a similar servlet "LoginHandlet.java" is used to manage the
 * Classified Ads for the ManageClassifed.jsp page.
 * @author Tom Kochanowicz tkoc@cox.net
 * @version 0.2 April 06, 2003
 */

package com.classified;


import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import com.classified.customer.Customer;
import com.classified.exceptions.CustomerActivityException;

public class LoginUserHandler extends HttpServlet {

	public void doPost(HttpServletRequest req, HttpServletResponse res)
		throws ServletException, IOException {
    
			res.setContentType("text/html");
			PrintWriter out = res.getWriter();

			// Get the user's name and password
			String name = req.getParameter("name");
			String passwd = req.getParameter("passwd");

      boolean verified = false;


     /**
      * Verify the user has logged in by checking there name in the database.
      */

    Customer cust = new Customer(); 
    try{
      verified = cust.verifyUserQuery(name, passwd);
    }catch(CustomerActivityException e){
      System.out.println("error with LoginUserHander.verifyUserQuery() error: " + e);
    }

			// Check the name and password for validity
      if(!verified){
				out.println("<HTML><HEAD><TITLE>Access Denied</TITLE></HEAD>");
				out.println("<BODY>Your login and password are invalid.<BR>");
				out.println("You may want to <A HREF=\"../classified/loginUser.jsp\">try again</A>");
				out.println("</BODY></HTML>");
			}
			else{
				// Valid login. Make a note in the session object.
				HttpSession session = req.getSession(true);
        session.setAttribute("loginUser.isDone", name); // just a marker object
				// Try redirecting the client to the page he first tried to access
				try{
          String target = (String) session.getAttribute("loginUser.target");

  
/////////////////////////  TEST  //////////////////////////////
/** THE CODE BELOW MUST BE IN THE SERVLET THAT YOU NEED TO LOG INTO BEFORE
 * YOU CAN USE IT. Put the code just bellow the code where you check the 
 * session for null ... in the doPost section.
 
			// Does the session indicate this user already logged in?
			Object done = session.getAttribute("loginUser.isDone");		// marker object
			if(done == null){
				// No loginUser.isDone means he hasn't logged in.
				// Save the request URL as the true target and redirect to the login page.

				session.("loginUser.target", "/The javaServerPageUwant.jsp");
				// session.putValue("loginUser.target",
				// HttpUtils.getRequestURL(request).toString());
        // ** HttpUtils is depricated, try HttpServletRequest //
				response.sendRedirect(request.getScheme() + "://" +
				request.getServerName() + ":" + request.getServerPort() +
					"/classified/loginUser.jsp");             
				return;
			}

*/
/////////////////////  TEST  /////////////////////////////////////

					if (target != null)
            // you are logged in now so go to target page.
						res.sendRedirect("../classified" + target);
					else res.sendRedirect("../classified/UpdateClassified.jsp");

					return;
				}catch (Exception ignored){}

				// Couldn't redirect to the target. Redirect to the site's home page.
				res.sendRedirect(req.getScheme() + "://" + 
								req.getServerName() + ":" + req.getServerPort());
			}
		}


// Call Customer.verifyUserQuery(user_email, passwd) instead of below.
///	protected boolean allowUser(String user, String passwd) {
///		if((user.equals("blueads") && passwd.equals("password")) || 
///                (user.equals("blueads") && passwd.equals("blueads"))){
///			return true;
///		}else{
///			return false;
///		}
///	}

}

