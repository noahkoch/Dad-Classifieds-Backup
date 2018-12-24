/**
 *  File:  Update2DBServlet.java
 *  Purpose: This servlet does an update based on email address and uniqueDateId
 *  then displays the updated ad. This servlet is called from the
 *  UpDateDBServlet.java program which received its data from the
 *  UpdateClassified.jsp page. This servlet updates the AdInfoView table.
 *  Notes: Alias for Update2DBServlet is update2, alias for UpdateDBServlet
 *  is update.
 *  
 *  @author Tom Kochanowicz,  blue-j@worldnet.att.net
 *  @version 0.1, 23 April, 2000
 *  @author Tom Kochanowicz, tkoc@cox.net
 *  @version 0.2 March 5, 2003
 */

package com.classified;

import java.awt.*;
import java.util.*;
import java.util.Properties;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import java.io.*;
import java.text.*;
import javax.sql.*;
import javax.naming.*;

import com.classified.common.Classified;

public class Update2DBServlet extends HttpServlet{

  // data members
  private Connection dbConnection;
  private javax.sql.DataSource pool;

  /**
   * For updateStatementAdInfoView prepared statement to update data in the 
   * AdInfoView table.
   */ 
 
  private PreparedStatement updateStatementAdInfoView; // USE

  static final String Update_Statement_AdInfoView = 
    "UPDATE AdInfoView set WhatAdSays01 = ?, WebSite = ?, " +
    " Phone = ? WHERE Email = ? AND UniqueDateId = ?";
  
  // For updateStatementAdInfoView prepared statement.
  private final int FIND_WHAT_AD_SAYS_01_POSITION = 1;
  private final int FIND_WEB_SITE_POSITION = 2;
  private final int FIND_PHONE_POSITION = 3;
  private final int FIND_EMAIL_POSITION = 4;
  private final int FIND_UNIQUE_DATE_ID_POSITION = 5;

  private final String CR = "\n";

  public void init(ServletConfig config) throws ServletException{
    super.init(config);
      try{
          Context env = (Context) new InitialContext().lookup("java:comp/env");
          pool = (javax.sql.DataSource) env.lookup("jdbc/Classifieds");

          if (pool == null)
            throw new ServletException("`jdbc/Classifieds' is an unknown DataSource");
      }catch (NamingException e) {
        throw new ServletException(e);
      }
  }
  
  public void doPost(HttpServletRequest request, HttpServletResponse response)
           throws ServletException, IOException{

		PrintWriter outputToBrowser =  new PrintWriter(response.getOutputStream());
		HttpSession session = request.getSession(true);


		// We should always get a session back & check for database connection.
		if(session == null){
			outputToBrowser.println("ERROR: Manage Database session is null.");
			outputToBrowser.flush();
			outputToBrowser.close();
			return;
		}				

    /**
     * Get the action from the UpdateClassified.jsp step1 page for step2 (this page).
     */
		String action = request.getParameter("action");
    String adTitle = request.getParameter("AdTitle");
	  String whatAdSays01 = request.getParameter("WhatAdSays01");
	  String webSite = request.getParameter("WebSite");
	  String phone = request.getParameter("Phone");
		String email = request.getParameter("Email");
		String uniqueDateId = request.getParameter("UniqueDateId");
		
    /**
     * Get ClassifiedAd data from HTML form and put in Select statement for display
     */

		// Check if valid data for phone and whatAdSays01.  
		Classified aClassified = new Classified(request);  // GET RID OF THIS LINE

	 if(action.equals("step2") && checkWhatAdSays01(whatAdSays01) && checkPhone(phone)){

		System.out.println("Update2DBServlet init: Preparing update statement");
		try {

      /** Get a connection to the pool */
      dbConnection = null;
      dbConnection = pool.getConnection();

      // Turn on transactions
			dbConnection.setAutoCommit(false);

      /**
       * SQL to update the WhatAdSays01, webSite & phone based on email & uniqueDateId.
       */
      updateStatementAdInfoView =
        dbConnection.prepareStatement(Update_Statement_AdInfoView);

			// clear parameters
			updateStatementAdInfoView.clearParameters();

      /**
       * Set position for 5 parameters and append adTitle to whatAdSays01.
       * The Ad Title is added to the string WhatAdSays01 so that it can be searched from the same
       * blob field. I added 3 .'s so the adTitle will look centered when there
       * is a small banner with it. There is no database field for adTitle.
       */
      updateStatementAdInfoView.setString(FIND_WHAT_AD_SAYS_01_POSITION, "<b><center>" + 
          adTitle + " . . .</center></b>" + whatAdSays01);        
      
			///updateStatementAdInfoView.setString(FIND_WHAT_AD_SAYS_01_POSITION, whatAdSays01);
		  updateStatementAdInfoView.setString(FIND_WEB_SITE_POSITION, webSite);
		  updateStatementAdInfoView.setString(FIND_PHONE_POSITION, phone); 
		  updateStatementAdInfoView.setString(FIND_EMAIL_POSITION, email); 
		  updateStatementAdInfoView.setString(FIND_UNIQUE_DATE_ID_POSITION, uniqueDateId);

      System.out.println("Update2DBServlet init: End");

			try{
				// Turn on transactions
				////dbConnection.setAutoCommit(false); //this statement moved to above.

				// Call the executeUpdate statement & pass email & 
				// uniqueDateId fields. Note that adTitle is simply a getParameter String
				updateClassifieds(request, response, adTitle, whatAdSays01, webSite, email, phone, uniqueDateId);

				// close statement
				updateStatementAdInfoView.close();

				// Commit statement if no errors.
				dbConnection.commit();
			}
			catch(Exception e){	
				try{			
					// Any error is grounds for rollback.
					dbConnection.rollback();
				}
				catch(SQLException exp){ 
          System.out.println("SQL Exception in Update2DBServlet: " + exp);
				}
				outputToBrowser.println("Insert failed. Please contact technical support.");
			}
		}
		catch (Exception e){
			System.out.println("A problem occured while deleting your classifiedAd. "
				+ " Please try again." + e);
			///cleanUp();
   			e.printStackTrace();
		}
		// put a finally clause here to release connection pool p269 Jason H.
		///finally{
		///	if (dbConnection != null)
    ///    try { 
    ///      dbConnection.close();   	
		///	}catch (SQLException sqe){
    ///    System.out.println("SQL exception in Update2DBServlet " + sqe);
    ///  }			
		///}
//////////////////////   TEST 
    finally{
      try{
        // Close prepared statement after execution.
        updateStatementAdInfoView.close();        
      }catch (SQLException sqe){
        System.out.println("Exception in finally or Update2DBServlet " + sqe);
      }			
      try{ // close the database connection if not closed.
          if(!isConnectionClosed())
          dbConnection.close();
      }catch(Exception e){
         System.out.println("Problem with UpDateDBServet dbConnection.close() statement " + e);
      }
    } 
///////////////////// END TEST
    
	 }else if(!checkPhone(phone)){
		// build Phone error page

		StringBuffer htmlPage = new StringBuffer();
		htmlPage.append("<html><head><title>Phone Error Page</title></head>");
		htmlPage.append("<body background=../classified/servletimages/newsbg.jpg bgcolor=ff0000>");
		htmlPage.append("<center><h1>Phone Field Error Page</h1></center><hr>");
		htmlPage.append("<b>Sorry...</b>The information for the <b>Phone</b> field does not ");
		htmlPage.append("appear to be valid. <b>Press the Edit button if</b>");
		htmlPage.append("<b> you wish to correct the information you provided.</b>");
		htmlPage.append(" Make sure you use the format<b> nnn-nnn-nnnn.</b>");

		// Put back button in cell or it will not show up
		htmlPage.append("<div align=\"right\"><TABLE BORDER=\"1\" WIDTH=\"7%\"><TR><TD WIDTH=\"100%\" BGCOLOR=\"silver\">");
		htmlPage.append("<ALIGN=\"CENTER\"><A HREF=# ONCLICK=\"history.go(-1)\"><FONT COLOR=\"black\"><b>Edit</b></FONT></A>");
		htmlPage.append("</TD></TR></TABLE></DIV>");

		htmlPage.append("<hr>");
		htmlPage.append("<center><a href=../classified/index.jsp>Return to Home Page</a>");
		htmlPage.append("<p><i>" + this.getServletInfo() + "</i>");
		htmlPage.append("</center></body></html>");
		
		// now let's send this dynamic data
		// back to the browser

		response.setContentType("text/html");
		outputToBrowser.println(htmlPage);
		outputToBrowser.flush();
		outputToBrowser.close();
	 }else if(!checkWhatAdSays01(whatAdSays01)){
		// build Check What Ad Says error page

		StringBuffer htmlPage = new StringBuffer();
		htmlPage.append("<html><head><title>What Ad Says Error Page</title></head>");
		htmlPage.append("<body background=../classified/servletimages/newsbg.jpg bgcolor=ff0000>");
		htmlPage.append("<center><h1>What Ad Says Error Page</h1></center><hr>");
		htmlPage.append("<b>Sorry...</b>The information for the <b>What Ad Says</b> field is over ");
		htmlPage.append("the limit of 1000 character. Please shorten your ad. <b>Press the Edit button if</b>");
		htmlPage.append("<b> you wish to correct the information you provided.</b>");
		htmlPage.append(" Make sure you <b> have at least ten characters in your ad</b>");

		// Put back button in cell or it will not show up
		htmlPage.append("<div align=\"right\"><TABLE BORDER=\"1\" WIDTH=\"7%\"><TR><TD WIDTH=\"100%\" BGCOLOR=\"silver\">");
		htmlPage.append("<ALIGN=\"CENTER\"><A HREF=# ONCLICK=\"history.go(-1)\"><FONT COLOR=\"black\"><b>Edit</b></FONT></A>");
		htmlPage.append("</TD></TR></TABLE></DIV>");

		htmlPage.append("<hr>");
		htmlPage.append("<center><a href=../classified/index.jsp>Return to Home Page</a>");
		htmlPage.append("<p><i>" + this.getServletInfo() + "</i>");
		htmlPage.append("</center></body></html>");

    /**
     * now let's send this dynamic data
     * back to the browser
     */
    response.setContentType("text/html");
    outputToBrowser.println(htmlPage);
		outputToBrowser.flush();
    outputToBrowser.close();
	 }
  }


  public void updateClassifieds(HttpServletRequest request, HttpServletResponse response, String adTitle,
				 String whatAdSays01, String webSite, String email, String phone, String uniqueDateId){
        
	 try{
		// Update the Classified Ad from the AdInfoView Table and display it.
		int records = updateStatementAdInfoView.executeUpdate();

		// Get the original data from the AdInfoView table & display WhatAdSays01, Phone & WebSite
		StringBuffer htmlPage = new StringBuffer();
		htmlPage.append("<html><head><title>Find Classified Ad To Update</title></head>");
		///htmlPage.append("<BODY BACKGROUND=\"../classified/servletimages/newsbg.jpg\" >");
    htmlPage.append("<BODY BGCOLOR='#C9E8F8'>");

		htmlPage.append("<H3 ALIGN=\"CENTER\"><FONT FACE=\"Arial, Helvetica, sans-serif\" COLOR=\"#0000a0\">Thank You For Using ");
		htmlPage.append("Omaha's Premier Classified Ads</FONT></H3>");

        /** eagle.gif */
    htmlPage.append("</CENTER>");
    htmlPage.append("<P ALIGN=\"CENTER\"><IMG src=\"servletimages/eagle.gif\" width=\"32\" height=\"32\" align=\"BOTTOM\" border=\"0\"></P>");
    htmlPage.append("<CENTER>");
    
		htmlPage.append("<CENTER><P><TABLE BORDER=\"0\" CELLPADDING=\"0\" CELLSPACING=\"0\" WIDTH=\"40%\" BGCOLOR=\"#0000A0\">");
		htmlPage.append("<TR><TD WIDTH=\"100%\" NOWRAP><P ALIGN=\"CENTER\"><B>");
    htmlPage.append("<FONT FACE=\"Arial, Helvetica, sans-serif\" COLOR=\"#ffffff\">");
    htmlPage.append("This is what your updated ad says.</FONT></B>");
		htmlPage.append("</TD></TR></TABLE></P></CENTER><CENTER><P><TABLE BORDER=\"0\" WIDTH=\"70%\"><TR>");
		htmlPage.append("<TD VALIGN=\"TOP\" COLSPAN=\"2\" BGCOLOR=\"white\"><FONT SIZE='2' FACE=\"Arial, Helvetica, sans-serif\">");
System.out.println(whatAdSays01);	
/// TEST //////////////


//"<b><center>" +  adTitle + " . . .</center></b>" + whatAdSays01);    

htmlPage.append("<b><center>" +  adTitle + " . . .</center></b>" + whatAdSays01).append(" ").append(CR);			


//////////// END TEST
		///htmlPage.append(whatAdSays01).append(" ").append(CR);			
		htmlPage.append("</FONT></TD></TR>");
		htmlPage.append("<TR>");
		htmlPage.append("<TD WIDTH=\"33%\"  NOWRAP BGCOLOR=\"white\"><B><FONT SIZE=\"4\" FACE=\"Wingdings\">F</FONT>");
		htmlPage.append("<FONT FACE=\"Times New Roman\">Phone: ").append("</FONT></B>").append(phone).append("</TD>");
		htmlPage.append("<TD WIDTH=\"37%\"  NOWRAP BGCOLOR=\"white\"><B><FONT SIZE=\"4\" FACE=\"Wingdings\">F</FONT>");
		htmlPage.append("<FONT FACE=\"Times New Roman\">Web Site: ").append("</FONT></B>").append(webSite).append("</TD>");			
		htmlPage.append("</TR></TABLE></P>");
		htmlPage.append("<BR><CENTER><A HREF=\"../classified/index.jsp\"><B><FONT SIZE=\"2\" FACE=\"Arial, Helvetica, sans-serif\">Click Here To Continue</FONT></CENTER>");		

		htmlPage.append("</FONT></B></CENTER></FORM>");
		htmlPage.append("</BODY>");
		htmlPage.append("</HTML>");

  /**
   * now let's send this dynamic data
   * back to the browser
   */

    /**
    * You don't want browser to cache the resultset etc.
    */
    PrintWriter outputToBrowser =  new PrintWriter(response.getOutputStream());
    response.setContentType("text/html");
    if( request.getProtocol().equals( "HTTP/1.1"))  
      response.setHeader( "Cache-Control", "no-cache"); 
    response.setHeader("Pragma","no-cache"); //HTTP 1.0
    response.setDateHeader ("Expires", 0); //prevents caching at the proxy server
        
    /**
     * now let's send this dynamic data
     * back to the browser.
     */
		outputToBrowser.println(htmlPage);
		outputToBrowser.flush();
	  outputToBrowser.close();
		return;
					
	 }catch (Exception e){
      System.out.println("Problem in Update2DBServlet " + e);
    ///cleanUp();
    e.printStackTrace();
   }
  }


  public boolean checkWhatAdSays01(String whatAdSays01){
    int length = whatAdSays01.length();
		// Check if field is null or contains blanks
		if((length < 9) || (length > 1000) || (whatAdSays01 == null) || (whatAdSays01.trim().equals("")))
			return false;
		else
			return true;	
  }


  public boolean checkPhone(String phone){
 	// Make sure the numbers entered are numbers by checking
	// between the spaces and dashes.

    int 	length=phone.length();
    char	array [] = phone.toCharArray ();

    // If no phone, make sure it's blank.
    if ((phone.trim().length() < 1) || (phone == null) || (phone.trim() == "")) return true;
	
    // Not blank so they must want a phone number.
    // Make sure 12 chars. 
    else if (phone.length() != 12) {
      return false;
    }
		 	
    for(int i = length - 1; i >= 0; i--){
      if(Character.isWhitespace (array [i])
                       	|| array [i] == '-'){
        length -= 1;
        for (int j = i; j < length; j++){
          array [j] = array [j + 1];
        }
      }else if (!Character.isDigit (array [i]))
        return false;
    }

    // Make sure state matches NE or IA phone area codes
    if((phone.charAt(3) == '-') && (phone.charAt(7) == '-')){
      String new_string = phone.substring(0,3);
      new_string += phone.substring(4,7);
      new_string += phone.substring(8,phone.length());

  	  // check NE area codes 308 & 402
      if((phone.charAt(0)=='3') && (phone.charAt(1)=='0') &&
      (phone.charAt(2)=='8') && (new_string.length() == 10))
        return true;
 
      else if	((phone.charAt(0)=='4') && (phone.charAt(1)=='0') && 
      (phone.charAt(2)=='2') && (new_string.length() == 10))
        return true;

      // check IA area codes 319, 515 && 712
      if((phone.charAt(0)=='3') && (phone.charAt(1)=='1') &&
      (phone.charAt(2)=='9') && (new_string.length() == 10))
        return true;
 
      else if	((phone.charAt(0)=='5') && (phone.charAt(1)=='1') && 
      (phone.charAt(2)=='5') && (new_string.length() == 10))
        return true;
			
      else if	((phone.charAt(0)=='7') && (phone.charAt(1)=='1') && 
      (phone.charAt(2)=='2') && (new_string.length() == 10))
        return true;				
    }	
    return false;			
  }

  public void cleanUp(){
    try {
      System.out.println("Closing database connection");
        dbConnection.close();
    }catch (SQLException e){
      e.printStackTrace();
    }
  }

  public void destroy(){
    System.out.println("Update2DBServlet: destroy");
    cleanUp();
  }

  public String getServletInfo(){
    return "<i>Classified Ads v.02</i>";
  }

	// Test to see if you lost a database
	// connection with Symantec dbANYWHERE

	public boolean isConnectionClosed() {
		try {
			if (dbConnection != null)
			return dbConnection.isClosed();
			else
				return true;
			}
			catch (SQLException e){
        System.out.println("SQL Exception closing database UpDate2DBServlet " + e);
				return true;
      }
	}
}
