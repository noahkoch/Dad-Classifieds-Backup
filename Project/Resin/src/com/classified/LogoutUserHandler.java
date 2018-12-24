/**
 * Purpose: This servlet takes the value from a URL to end
 * the session by invalidating the session.
 * @param  logout
 * example: /classified/logoutUser (logoutUser is the alias for this servlet.)
 * @author Tom Kochanowicz
 * @version 0.1 4/9/03
 */

package com.classified;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.PrintWriter;
import java.io.IOException;

public class LogoutUserHandler extends HttpServlet{

  public void init(ServletConfig config) throws ServletException{
    super.init(config);
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response) 
        throws ServletException, IOException{
 
    /**
     * Get the session object or create one if not exist. A session will
     * persist as long as the client browser maintains a connection with
     * the server.
     */
  
    PrintWriter outputToBrowser =  new PrintWriter(response.getOutputStream());
    HttpSession session = request.getSession(true);
				
    // We should always get a session back & check for database connection.
    if(session == null){
      outputToBrowser.println("ERROR: You are logged out.");
      outputToBrowser.flush();
      outputToBrowser.close();
      return;
    }

    /**
     * Log the user out by ending the session.
     * Done by adding /classified/logoutUser to the hyperlink
     * @param none
     */
    try{
      // Invalidate and send to home page.
      session.invalidate();
      response.sendRedirect(request.getScheme() + "://" +
      request.getServerName() + ":" + request.getServerPort() +	"/classified/");
      return;
		}catch(IllegalStateException e){
			System.out.println("Exception " + e);
    } 
  }
}