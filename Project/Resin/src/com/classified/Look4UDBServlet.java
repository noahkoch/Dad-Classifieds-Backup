/**
 *  File:  Look4UDBServlet.java
 *  This servlet puts an email address, phrase & End date 
 *  into the Look4U database table. Works with the 
 *  Look4UFeature servlet.
 *
 *  @author Tom Kochanowicz,  blue-j@worldnet.att.net
 *  @version 0.1, 13 Febr. 2000
 *  @version 0.2, Feb, 03
 *
 */

package com.classified;

import java.awt.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import javax.sql.*;
import javax.naming.*;
import java.io.*;
import javax.servlet.*;
import java.text.*; // for SimpleDateFormater

public class Look4UDBServlet extends HttpServlet{
  // data members
  private Connection dbConnection;
  private DataSource pool;
  final static String CR = "\n";
  
  /**
   * Look4U table. This inserts the email address, phrase
   * ad end date into the database.
   */
  protected PreparedStatement registerStatementLook4U;
  static final String Register_Statement_Look4U =
    "insert into Look4U " +	"(Email, Phrase, EndDate)" + " values (?, ?, ?)";    
  protected final int EMAIL_POSITION = 1;
  protected final int PHRASE_POSITION = 2;
  protected final int END_DATE_POSITION  = 3;

  /**
   * Delete matching email in Look4U table that have
   * expired or when they want to stop Look4U.
   */
  protected PreparedStatement registerStatementStopLook4U;
  static final String Register_Statement_Stop_Look4U =
    "delete from Look4U where Email = ?";
  protected final int EMAIL_DELETE_POSITION = 1;
	
  public void init(ServletConfig config) throws ServletException{
    super.init(config);
      try{
          System.out.println("Look4UDBServlet init: Start");
          Context env = (Context) new InitialContext().lookup("java:comp/env");
          pool = (DataSource) env.lookup("jdbc/Classifieds");

          if (pool == null)
            throw new ServletException("`jdbc/Classifieds' is an unknown DataSource");
      }catch (NamingException e) {
        throw new ServletException(e);
      }
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException{
    // Have todaysDate to reference for comparison wiht BLUEADS cookie
    Calendar calendar1;
    SimpleDateFormat formatter1;
    java.util.Date todaysDate;

    // Format todaysDate
    calendar1 = Calendar.getInstance();
    formatter1 = new SimpleDateFormat("MM/dd/yy");
    formatter1.setTimeZone(java.util.TimeZone.getDefault());  // bug fix so not PST & is CST
    todaysDate = calendar1.getTime();

    // Send out a cookie and test if it's accepted.
                                        // Name	   // Value
    Cookie cookieThere = new Cookie ("IS_COOKIE_THERE", "YES");
	  cookieThere.setMaxAge (60);     // cookie expires in 60 seconds
	  cookieThere.setPath ("/");
	  cookieThere.setSecure(false);	// Change to true when using SSL.
	  response.addCookie (cookieThere);

    // Is the cookie there? Search the received cookies
    String IS_COOKIE_THERE = null;
    String valueOfCookie = null;
    Cookie[] anyCookies = request.getCookies();

    if(anyCookies != null){ // Accepts cookies, check if IS_COOKIE_THERE there.
      for (int i = 0; i < anyCookies.length; i++){
        if (anyCookies[i].getName().equals("IS_COOKIE_THERE")) {
          valueOfCookie = anyCookies[i].getValue();
          System.out.println("IS_COOKIE_THERE " + valueOfCookie);
          if (valueOfCookie.equals("YES"))
            System.out.println("IT DOES ACCEPT Cookies");
          else
            response.sendRedirect("../classified/Notice.html"); // Not there.
        }
      }
    }else{	// Doesn't accept cookies.
      response.sendRedirect("../classified/Notice.html");
    }
	

    /**
     * Get the session object or create one if not exist. A session will
     * persist as long as the client browser maintains a connection with
     * the server. Also check if their is a connection with the database.
     */
    PrintWriter outputToBrowser =  new PrintWriter(response.getOutputStream());
	 	HttpSession session = request.getSession(true);


    // We should always get a session back & check for database connection.
    if(session == null){
      outputToBrowser.println("ERROR: Look4U Database session is null.");
      outputToBrowser.flush();
      outputToBrowser.close();
      return;
    }


 synchronized(session){
			
    // Test if button was pressed
   	String action = request.getParameter("action");

    // Create a new classified ad based on the form data
    Look4UFeature aFeature = new Look4UFeature(request);

   	if (action.equals("step1")){ 
    /**
     * This is a hidden field in the Look4U.jsp form. If the user
     * clicks on the submit button, the servlet will do all the users input
     * field tests. If an error occurs the user will get an error page,
     * otherwise they will proceed to step2 where they will finalize it to
     * the database.
     */

    try{
        /** get variable look & check for Start/StopLook4U */
        String look = request.getParameter("Look");

      /**
       * Validate fields, checkPhrase fuction checks if Start/StopLook4U 
       */
	  	if(aFeature.checkEmail() && aFeature.checkPhrase(look)){

				// Get all parameters from the html page and store them for a hidden value.
				String eMail = request.getParameter("Email").trim();								
				String phrase = request.getParameter("Phrase").trim();
				String duration = request.getParameter("Duration");

				// End Date field is calculated off of duration field.
				String endDate = aFeature.calculateEndDate(todaysDate, Integer.valueOf(duration).intValue());		
			
				/////dbConnection.setAutoCommit(false); // weblogic pooling does not like.
/// Causes errors		synchronized(registerStatementLook4U){
          /**
           * Set up Look4U to insert email, phrase, and
           * end date into the Look4U database.
           */
          dbConnection = null;
          dbConnection = pool.getConnection();

          registerStatementLook4U =	
                dbConnection.prepareStatement(Register_Statement_Look4U);
        
          // set Look4U sql parameters
					registerStatementLook4U.clearParameters();
          
					registerStatementLook4U.setString(EMAIL_POSITION, eMail);
					registerStatementLook4U.setString(PHRASE_POSITION, phrase);
          registerStatementLook4U.setString(END_DATE_POSITION, endDate);
///				}

				//System.out.println(aFeature.getClassifiedAd()); //***************
			
        if(look.equals("StartLook4U")){
          // Put an html Proof Read Page back to the user so they can read over
          // their ad.
          StringBuffer htmlPage = new StringBuffer();
          htmlPage.append("<html><head><title>Start Look4U Proof Read Page</title></head>");
          htmlPage.append("<body>");
          htmlPage.append("<center><h1>Start Look4U Proof Read Page</h1></center><hr>");
          htmlPage.append("<body background=../classified/servletimages/newsbg.jpg bgcolor=ffffff>");
          htmlPage.append("<b>Please read over the information you entered below.</b> ");
          htmlPage.append("<b>If the information is correct, click the Submit button,</b> ");
          htmlPage.append("<b>below otherwise click the Edit button to make changes.</b> ");

          // Put back button in cell or it will not show up
          htmlPage.append("<div align=\"right\"><TABLE BORDER=\"1\" WIDTH=\"7%\"><TR><TD WIDTH=\"100%\" BGCOLOR=\"silver\">");
          htmlPage.append("<ALIGN=\"CENTER\"><A HREF=# ONCLICK=\"history.go(-1)\"><FONT COLOR=\"black\"><b>Edit</b></FONT></A>");
          htmlPage.append("</TD></TR></TABLE></DIV>");
          htmlPage.append("<hr>");

          // JavaScript To prevent user hitting Submit button too many times.
          htmlPage.append("<SCRIPT LANGUAGE=\"JavaScript\"> <!-- Hide script from old browsers\n ");
          htmlPage.append(" var submitted = false;");
          htmlPage.append(" function doSubmit(form)");
          htmlPage.append(" {");
          htmlPage.append(" if (!submitted) {");
          htmlPage.append(" submitted = true;");
          htmlPage.append(" form.submit();");
          htmlPage.append(" }");
          htmlPage.append(" }");
          htmlPage.append("// End hiding script from old browsers-->\n</SCRIPT>");
          htmlPage.append(aFeature.toStartLookWebString());

          // Create a "Submit" button.
          htmlPage.append("<FORM METHOD=POST>");

          /**
           * Put a hidden values for each field plus a step2 hidden field for 
           * a confirmation page. For security, do not cache the page "setHeader("Expires","-1")".
           */
          htmlPage.append("<INPUT TYPE=\"hidden\" NAME=\"Email\" VALUE=\"").append(eMail).append("\">");			
          htmlPage.append("<INPUT TYPE=\"hidden\" NAME=\"Look\" VALUE=\"").append(look).append("\">");			
          htmlPage.append("<INPUT TYPE=\"hidden\" NAME=\"Phrase\" VALUE=\"").append(phrase).append("\">");
          htmlPage.append("<INPUT TYPE=\"hidden\" NAME=\"Duration\" VALUE=\"").append(duration).append("\">");
          htmlPage.append("<INPUT TYPE=\"hidden\" NAME=\"EndDate\" VALUE=\"").append(endDate).append("\">");
          htmlPage.append("<INPUT TYPE=\"hidden\" NAME=\"action\" VALUE=\"step2\">");
          htmlPage.append("<center><b><i><font color=\"#0033CC\">Please be patient, processing will take a few seconds.</font></i><b><br></center>");

          htmlPage.append("<div align=\"right\"><INPUT TYPE=\"button\" VALUE=\"Edit\" ONCLICK=\"history.go(-1)\">");
          htmlPage.append("<INPUT TYPE=\"button\" VALUE=\"Submit\" ONCLICK=\"doSubmit(this.form)\"></div>");
          htmlPage.append("</body></html>");

          /**
           * now let's send this dynamic data
           * back to the browser
           */
          response.setHeader("Expires", "Mon, 01 Jan 1990 06:00:01 GMT");
          response.setHeader("Cache-Control", "no-cache");
          response.setHeader("Pragma", "no-cache");

          ////response.setHeader("Expires","-1"); // do not cache page.
          response.setContentType("text/html");
          outputToBrowser.println(htmlPage);
          outputToBrowser.flush();
          outputToBrowser.close();
          return;
        }else if(look.equals("StopLook4U")){
          /**
           * Stops Look4U by deleting rows based on email address.
           */
          dbConnection = null;
          dbConnection = pool.getConnection();

          registerStatementStopLook4U = 
            dbConnection.prepareStatement(Register_Statement_Stop_Look4U); 
					registerStatementStopLook4U.clearParameters();
          
					registerStatementStopLook4U.setString(EMAIL_DELETE_POSITION, eMail);

          /**
           * Put an html Proof Read Page back to the user so they can read over
           * their ad.
           */
          StringBuffer htmlPage = new StringBuffer();
          htmlPage.append("<html><head><title>Stop Look4U Proof Read Page</title></head>");
          htmlPage.append("<body>");
          htmlPage.append("<center><h1>Stop Look4U Proof Read Page</h1></center><hr>");
          htmlPage.append("<body background=../classified/servletimages/newsbg.jpg bgcolor=ffffff>");
          htmlPage.append("<b>Please read over the information you entered below.</b> ");
          htmlPage.append("<b>If the information is correct, click the Submit button,</b> ");
          htmlPage.append("<b>below otherwise click the Edit button to make changes.</b> ");

          // Put back button in cell or it will not show up
          htmlPage.append("<div align=\"right\"><TABLE BORDER=\"1\" WIDTH=\"7%\"><TR><TD WIDTH=\"100%\" BGCOLOR=\"silver\">");
          htmlPage.append("<ALIGN=\"CENTER\"><A HREF=# ONCLICK=\"history.go(-1)\"><FONT COLOR=\"black\"><b>Edit</b></FONT></A>");
          htmlPage.append("</TD></TR></TABLE></DIV>");
          htmlPage.append("<hr>");

          // JavaScript To prevent user hitting Submit button too many times.
          htmlPage.append("<SCRIPT LANGUAGE=\"JavaScript\"> <!-- Hide script from old browsers\n ");
          htmlPage.append(" var submitted = false;");
          htmlPage.append(" function doSubmit(form)");
          htmlPage.append(" {");
          htmlPage.append(" if (!submitted) {");
          htmlPage.append(" submitted = true;");
          htmlPage.append(" form.submit();");
          htmlPage.append(" }");
          htmlPage.append(" }");
          htmlPage.append("// End hiding script from old browsers-->\n</SCRIPT>");
          htmlPage.append(aFeature.toStoptLookWebString());

          // Create a "Submit" button.
          htmlPage.append("<FORM METHOD=POST>");

          /**
           * Put a hidden values for each field plus a step2 hidden field for 
           * a confirmation page. For security, do not cache the page "setHeader("Expires","-1")".
           */
          htmlPage.append("<INPUT TYPE=\"hidden\" NAME=\"Email\" VALUE=\"").append(eMail).append("\">");			
          htmlPage.append("<INPUT TYPE=\"hidden\" NAME=\"Look\" VALUE=\"").append(look).append("\">");			
          htmlPage.append("<INPUT TYPE=\"hidden\" NAME=\"Phrase\" VALUE=\"").append(phrase).append("\">");
          htmlPage.append("<INPUT TYPE=\"hidden\" NAME=\"Duration\" VALUE=\"").append(duration).append("\">");
          htmlPage.append("<INPUT TYPE=\"hidden\" NAME=\"EndDate\" VALUE=\"").append(endDate).append("\">");
          htmlPage.append("<INPUT TYPE=\"hidden\" NAME=\"action\" VALUE=\"step2\">");
          htmlPage.append("<center><b><i><font color=\"#0033CC\">Please be patient, processing will take a few seconds.</font></i><b><br></center>");

          htmlPage.append("<div align=\"right\"><INPUT TYPE=\"button\" VALUE=\"Edit\" ONCLICK=\"history.go(-1)\">");
          htmlPage.append("<INPUT TYPE=\"button\" VALUE=\"Submit\" ONCLICK=\"doSubmit(this.form)\"></div>");
          htmlPage.append("</body></html>");

          /**
           * now let's send this dynamic data
           * back to the browser
           */
          response.setHeader("Expires", "Mon, 01 Jan 1990 06:00:01 GMT");
          response.setHeader("Cache-Control", "no-cache");
          response.setHeader("Pragma", "no-cache");

          ////response.setHeader("Expires","-1"); // do not cache page.
	  	    response.setContentType("text/html");
			    outputToBrowser.println(htmlPage);
          outputToBrowser.flush();
	        outputToBrowser.close();
          return;
			  }
		  }else if(!aFeature.checkEmail()){
        /**
         * build Email error page
         */
				StringBuffer htmlPage = new StringBuffer();
				htmlPage.append("<html><head><title>Email Error Page</title></head>");
				htmlPage.append("<body background=../classified/servletimages/newsbg.jpg bgcolor=ff0000>");
				htmlPage.append("<center><h1>Email Field Error Page</h1></center><hr>");
				htmlPage.append("<b>Sorry...</b>The information for the <b>Email</b> field does not ");
				htmlPage.append("appear to be valid. <b>Press the Edit button if</b>");
				htmlPage.append("<b> you wish to correct the information you provided.</b>");
				
				// Put back button in cell or it will not show up
				htmlPage.append("<div align=\"right\"><TABLE BORDER=\"1\" WIDTH=\"7%\"><TR><TD WIDTH=\"100%\" BGCOLOR=\"silver\">");
				htmlPage.append("<ALIGN=\"CENTER\"><A HREF=# ONCLICK=\"history.go(-1)\"><FONT COLOR=\"black\"><b>Edit</b></FONT></A>");
				htmlPage.append("</TD></TR></TABLE></DIV>");

				htmlPage.append("<hr>");
				htmlPage.append(aFeature.toStartLookWebString());
				htmlPage.append("<center><a href=../classified/index.jsp>Return to Search/Home Page</a>");
				htmlPage.append("<p><i>" + this.getServletInfo() + "</i>");
				htmlPage.append("</center></body></html>");

        // now let's send this dynamic data
        // back to the browser

        response.setContentType("text/html");
        outputToBrowser.println(htmlPage);
        outputToBrowser.flush();
        outputToBrowser.close();
			}else if(!aFeature.checkPhrase(look)){
        // build Phrase error page

				StringBuffer htmlPage = new StringBuffer();
				htmlPage.append("<html><head><title>Phrase Error Page</title></head>");
				htmlPage.append("<body background=../classified/servletimages/newsbg.jpg bgcolor=ff0000>");
				htmlPage.append("<center><h1>Phrase Field Error Page</h1></center><hr>");
				htmlPage.append("<b>Sorry...</b>The information for the <b>Phrase</b> field does not ");
				htmlPage.append("appear to be valid. <b>Press the Edit button if</b>");
				htmlPage.append("<b> you wish to correct the information you provided.</b>");

				// Put back button in cell or it will not show up
				htmlPage.append("<div align=\"right\"><TABLE BORDER=\"1\" WIDTH=\"7%\"><TR><TD WIDTH=\"100%\" BGCOLOR=\"silver\">");
				htmlPage.append("<ALIGN=\"CENTER\"><A HREF=# ONCLICK=\"history.go(-1)\"><FONT COLOR=\"black\"><b>Edit</b></FONT></A>");
				htmlPage.append("</TD></TR></TABLE></DIV>");

				htmlPage.append("<hr>");
				htmlPage.append(aFeature.toStartLookWebString());

				htmlPage.append("<center><a href=../classified/index.jsp>Return to Search/Home Page</a>");
				htmlPage.append("<p><i>" + this.getServletInfo() + "</i>");
				htmlPage.append("</center></body></html>");
				
        // now let's send this dynamic data
        // back to the browser

        response.setContentType("text/html");
        outputToBrowser.println(htmlPage);
        outputToBrowser.flush();
        outputToBrowser.close();
			}
		}catch (Exception e) {
      System.out.println(" 2-DID IT GET HERE? " + e); 
      //cleanUp();
      e.printStackTrace();
    }	
	}
			


	// Execute sql if "Submit" button for Step2 is pressed otherwise user
	// must click the back button on the browser to correct info.

	if (action.equals("step2")){ 
    //////		try {

    //// Put a cookie in /////

    // Check if the cookie for BLUEADS is there and if it is,
    // is it same date as today or null. If not the later, create a new cookie.
    String cookieName = null;
    String value = null;
    boolean beenHereBefore = false;
   	Cookie[] blueadsCookie = request.getCookies();

	  if((blueadsCookie != null) && (action.equals("step2"))){
      for (int i =0; i < blueadsCookie.length; i++){
        value = blueadsCookie[i].getValue();			
        if((blueadsCookie[i].getName().equals("BLUEADS")) && (value.equals(formatter1.format(todaysDate)))){
          beenHereBefore = true;
        }
      }
	  }
//////////////////////////////////////////////////////////////
// TOM UNCOMMENT ALL LINE BELOW TO GET COOKIES TO WORK AGAIN!!
//    if (beenHereBefore == true){
//		  response.sendRedirect("../classified/Notice.html");
//		  return;
//	  }else{
//		  // Put in new cookie with todays date.  // Name    // Value
//  		Cookie newBlueadsCookie = new Cookie ("BLUEADS", formatter1.format(todaysDate));
//		  // Cookie expires in 24 hours. // 60 sec * 60 min * 24 hrs = 86400 seconds
//		  newBlueadsCookie.setMaxAge (60 * 60 * 24); 
//		  newBlueadsCookie.setPath ("/");
//		  newBlueadsCookie.setSecure(false); // Change to true for SSL.
//  		response.addCookie (newBlueadsCookie);
//		  // FOR TESTSystem.out.println("New BLUEADS Cookie = " + newBlueadsCookie + " New Date " + formatter1.format(todaysDate));
//	  }		
//		
////////////////////////// UN-COMMENT UP TO HERE ONLY!


    // Get all parameters from the step1 html page to display their values.
    //
    String eMail =  request.getParameter("Email");
    String look =  request.getParameter("Look");
    String phrase = request.getParameter("Phrase");
    String duration = request.getParameter("Duration");
    String endDate = request.getParameter("EndDate");


    // This is where the processing takes place depending if we are stoping a seach or starting one.
    if(look.equals("StartLook4U")){
      try{
///        ResultSet dataResultSet = null;
///        dataResultSet = registerStatementCountEmail.executeQuery();
///        int countEmail = 0;
///        while (dataResultSet.next()){
///          countEmail++;
///          // check for duplicate email address & display error page if duplicate.
///          if(countEmail > 0){
///            response.sendRedirect("../classified/Notice.html");
///            return;
///          }	
///        }
			
	 
        // Execute the SQL query to put the data into the Look4U database table.
        registerStatementLook4U.executeUpdate();
        /////dbConnection.commit();// weblogic pooling does not like.
        System.out.println(" Submit Order Finilized ");

        // build confirmation page
        StringBuffer htmlPage = new StringBuffer();
        htmlPage.append("<html><head><title>Look4U Confirmation Page</title></head>");
        htmlPage.append("<body>");
        htmlPage.append("<center><h1>Look4U Confirmation Page</h1></center><hr>");
        htmlPage.append("<body background=../classified/servletimages/newsbg.jpg bgcolor=ffffff>");
        htmlPage.append("<b>Congratulations: </b>The following information was entered successfully. ");
        htmlPage.append("Please read the instructions at the bottom of this page. ");

        htmlPage.append("<ul>");
        htmlPage.append("<li><B>Email: </B>").append(eMail);
        htmlPage.append("<li><B>Phrase to search for:</B> ").append(phrase);
        htmlPage.append("<li><B>Your search will last for:</B> ").append(duration).append(" days.");
        htmlPage.append("<B> ending on</B> ").append(endDate);
        htmlPage.append("</ul>");

        htmlPage.append("<hr>");
        htmlPage.append("<b>Please Note: You can stop your search at anytime by going </b><br>");
        htmlPage.append("<b>back to the same page you entered your search criteria on </b><br>");
        htmlPage.append("<b>and choosing the Stop Look4U button.</b><br>");
        htmlPage.append("<b>Thank You for using our Classified Ads. </b><br><br>");
        //htmlPage.append("<address><li><b>blueads.com</b><br>");
        //htmlPage.append("<li><b>3203 S. 116 Ave.</b><br>");
        //htmlPage.append("<li><b>Omaha, NE 68144</b><br>");
        //htmlPage.append("</address>");
        htmlPage.append("<hr>");
        htmlPage.append("<center><b><a href=../classified/index.jsp>Click Here To Continue</a></b>");
        //htmlPage.append("<a href=/servlet/Look4UDBServlet> | View Classified List</a>");
        htmlPage.append("<p><i>").append(this.getServletInfo()).append("</i>");
        htmlPage.append("</center></body></html>");

        // now let's send this dynamic data
        // back to the browser

        outputToBrowser =  new PrintWriter(response.getOutputStream());

        // now let's send this dynamic data
        // back to the browser

        // Set so page is not saved
        response.setHeader("Expires", "Mon, 01 Jan 1990 06:00:01 GMT");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Pragma", "no-cache");

        response.setContentType("text/html");
        outputToBrowser.println(htmlPage);
        outputToBrowser.flush();
        outputToBrowser.close();

        //Compiler.disable();  //turn off JIT so we can see line numbers when debugging
        return;
      }catch (SQLException e){
        /** 
         * Duplicate entry exception, redirect them to the Notice page. 
         */
        response.sendRedirect("../classified/Notice.html");
        System.out.println("Look4U SQL exception " + e);
      }catch(Exception e){
        System.out.println("Look4U regular exception " + e);
      }finally{
        if (dbConnection != null)
          try { 
            registerStatementLook4U.close();
            dbConnection.close(); 
            //dataResultSet.close();
          }catch (SQLException sqe){}		
	    }	  
	  }else if (look.equals("StopLook4U")){
      try{
        int countAdView = registerStatementStopLook4U.executeUpdate();
        if (countAdView >= 1){ // Look4U email was found in table to delete
          StringBuffer htmlPage = new StringBuffer();
          htmlPage.append("<html><head><title>Stop Look4U</title></head>").append(CR);
          htmlPage.append("<body background=../classified/servletimages/newsbg.jpg bgcolor=ff0000>");
          htmlPage.append("<body><center>").append(CR);
          htmlPage.append("<h1>Thank You For Doing Business With Us.</h1>").append(CR);
          htmlPage.append("<hr></center><p>").append(CR);
          htmlPage.append("<center><h3><b> Your Look4U Request Is Deleted, THANK YOU! </b></h3></center>").append(CR);
          htmlPage.append("</table></center>");
          htmlPage.append("<p><hr>");
          htmlPage.append("<center><a href=../classified/index.jsp>Return to Search/Home Page</a>");
          htmlPage.append("<p><i>").append(this.getServletInfo()).append("</i></center>");
          htmlPage.append("</body></html>");

          // now let's send this dynamic data
          // back to the browser
	
          ////PrintWriter outputToBrowser =  new PrintWriter(response.getOutputStream());
          response.setContentType("text/html");
          outputToBrowser.println(htmlPage);
          outputToBrowser.close();
        }else{
          // Look4U is already deleted or has never existed.

          StringBuffer htmlPage = new StringBuffer();
          htmlPage.append("<html><head><title>Stop Look4U Service</title></head>").append(CR);
          htmlPage.append("<body background=../classified/servletimages/newsbg.jpg bgcolor=ff0000>");
          htmlPage.append("<body><center>").append(CR);
          htmlPage.append("<h1>Thank You For Doing Business With Us!</h1>").append(CR);
          htmlPage.append("<hr></center><p>").append(CR);
          htmlPage.append("<center><h3><b> No Look4U Request Was Found To Delete, PRESS BACK TO TRY AGAIN! </b></h3></center>").append(CR);
          htmlPage.append("</table></center>");
          htmlPage.append("<p><hr>");
          htmlPage.append("<center><a href=../classified/index.jsp>Return to Search/Home Page</a>");
          htmlPage.append("<p><i>").append(this.getServletInfo()).append("</i></center>");
          htmlPage.append("</body></html>");

          // now let's send this dynamic data
          // back to the browser
	
          /////PrintWriter outputToBrowser =  new PrintWriter(response.getOutputStream());
          response.setContentType("text/html");
          outputToBrowser.println(htmlPage);
          outputToBrowser.close();

        }
	    }catch (SQLException e){
        e.printStackTrace();
      }catch(Exception e){
        e.printStackTrace();
      }finally{
        if (dbConnection != null)
          try{  
            registerStatementStopLook4U.close(); 	
            dbConnection.close();
          }catch (SQLException sqe){
            System.out.println("Look4U SQL Exception Closing " + sqe);
          }			
	    }	
	  }
  }
 } // end Sync
}// end doPost
	
  public void cleanUp(){
    try {
      System.out.println(" 3-DID IT GET HERE? "); //*****************
      System.out.println("Closing database connection");
      dbConnection.close();
    }catch (SQLException e){
      e.printStackTrace();
    }
  }

  public void destroy(){
    System.out.println("Look4UDBServlet: destroy");
    cleanUp();
  }

  public String getServletInfo(){
    return "<i>Classified Ads, v.02</i>";
  }
}


