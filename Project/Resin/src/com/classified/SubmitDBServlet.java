/**
 * File:  SubmitDBServlet.java
 * This servlet provides checks the data before entering
 * classified ad data in a database. It lets the user proof read
 * their ad before submitting it.
 * @author Tom Kochanowicz,  blue-j@worldnet.att.net
 * @version 0.1, 21 April 1999
 * @version 0.2, 26 Jan. 2003 
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

// For mail
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;

import com.classified.common.Classified;

public class SubmitDBServlet extends HttpServlet{
  // For mail
  private static ResourceBundle email_bundle =
    ResourceBundle.getBundle("com.classified.common.emailProperties");
  ///private final static String MAIL_HOST = "smtp.central.cox.net";
  ///private final static String MAIL_FROM = "tkoc@cox.net";

  // because the below enters data into two tables, AdInfo & AdInfoView you must work with
  // uniqueDateId in Classified.java p.293-6 example of  currentTimeMillis(p. 296 jh)
  //private long uniqueDateId =0; // primary key of database table, converted to String ...For Now
  
  // For counting number of Ads per user & to set a limit
  private int cookieAdCounter = 0;

  // data members
  private Connection dbConnection;
  private javax.sql.DataSource pool;

  /**
   * For registerStatementCustInfo prepared statement to insert data into the 
   * CustInfo table.
   */ 
  private PreparedStatement registerStatementCustInfo;
  //registerStatementCustInfo =
  static final String Register_Statement_Cust_Into= "insert into CustInfo "
		 	+ "(UniqueDateId, Name, Address, City, State, Zip, PayMethod, CardNumber, CardMo, CardYr, StartDate, EndDate, AmtForDayMoYr)"
			+ " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	// CustInfo table
	// UniqueDateId is a 1:1 relationship in both CustInfo & AdInfo tables.
	private final int CUST_UNIQUE_DATE_ID_POSITION = 1;
	private final int CUSTOMER_NAME_POSITION = 2;
  private final int BILLING_ADDRESS_POSITION  = 3;
  private final int CITY_POSITION = 4;
	private final int STATE_POSITION = 5;
	private final int ZIP_POSITION = 6;
	private final int PAYMETHOD_POSITION = 7;
	private final int CARD_NUMBER_POSITION = 8;
	private final int CARD_MO_POSITION = 9;
	private final int CARD_YR_POSITION = 10;
	private final int START_DATE_POSITION = 11;
	private final int END_DATE_POSITION = 12;
	private final int AMT_FOR_DAY_MO_YR_POSITION = 13;

  private PreparedStatement registerStatementAdInfo;
  static final String Register_Statement_AdInfo = "insert into AdInfo "
		 		+ "(UniqueDateId, ClassifiedAd, Color, Banner, WhatAdSays01, Phone, Email, WebSite)"
				+ " values (?, ?, ?, ?, ?, ?, ?, ?)";

	// AdInfo table
	private final int AD_UNIQUE_DATE_ID_POSITION = 1;
	private final int CLASSIFIED_AD_POSITION = 2;
	private final int COLOR_POSITION = 3;
	private final int BANNER_POSITION = 4;
  private final int WHATADSAYS_POSITION_01 = 5; // TEST was 4
	private final int PHONE_POSITION = 6;
	private final int EMAIL_POSITION = 7;
	private final int WEBSITE_POSITION = 8;	

  private final String CR = "\n";
  private int submitCount = 0;

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
    // Have todaysDate to reference for comparison wiht BLUEADS cookie
    Calendar calendar1;
    SimpleDateFormat formatter1;
    java.util.Date todaysDate;

    // Format todaysDate
    calendar1 = Calendar.getInstance();
    formatter1 = new SimpleDateFormat("MM/dd/yy");
    formatter1.setTimeZone(java.util.TimeZone.getDefault());  // bug fix so not PST & is CST
    todaysDate = calendar1.getTime();

    /**
     * Get the session object or create one if not exist. A session will
     * persist as long as the client browser maintains a connection with
     * the server. Also check if their is a connection with the database.
     */
  
    PrintWriter outputToBrowser =  new PrintWriter(response.getOutputStream());
    HttpSession session = request.getSession(true);
				
    // We should always get a session back & check for database connection.
    if(session == null){
      outputToBrowser.println("ERROR: Submit Database session is null.");
      outputToBrowser.flush();
      outputToBrowser.close();
      return;
    }

    ///////////////////////// LOGIN TEST  //////////////////////////////
    // THE CODE BELOW MUST BE IN THE SERVLET THAT YOU NEED TO LOG INTO BEFORE
    // YOU CAN USE IT. 
    /**
     * Purpose of code block below checks to see if the user is logged in.
     * If the user is not logged in, they are re-directed to a loginUser.jsp
     * page where they login. Then they are redirected back to the page 
     * they originally tried to log into.
     */
 
    // Does the session indicate this user already logged in?
    Object done = session.getAttribute("loginUser.isDone");		// marker object
    if(done == null){
      // No loginUser.isDone means he hasn't logged in.
      // Save the request URL as the true target and redirect to the login page.

///session.setAttribute("loginUser.target", "/SubmitClassified.jsp");
//session.setAttribute("loginUser.target", request.getRequestURL(request).toString());
session.setAttribute("loginUser.target", request.getRequestURL().toString());
      ///response.sendRedirect(request.getScheme() + "://" +
      ///request.getServerName() + ":" + request.getServerPort() +
      ///	"/loginUser.jsp");
      /** Redirect to login page */
      response.sendRedirect("/classified/loginUser.jsp");
      return;
    }
    /////////////////////  END TEST  ////////////////////////////

   synchronized(session){
    // Test if button was pressed
   	String action = request.getParameter("action");

    // Create a new classified ad based on the form data
    Classified aClassified = new Classified(request);

   	if (action.equals("step1")){
    /**
     * This is a hidden field in the SubmitClassified.jsp page. If the user
     * clicks on the submit button, the servlet will do all the users input
     * field tests. If an error occurs the user will get an error page,
     * otherwise they will proceed to step2 where they will finalize it to
     * the database.
     */

		try{
      /**
       * Send out a cookie and test if it's accepted in step-2 below.
       * 			                            // Name	    // Value
       */
			Cookie cookieThere = new Cookie ("IS_COOKIE_THERE", "YES");
			cookieThere.setMaxAge (120);     // cookie expires in 120 seconds
			cookieThere.setPath ("/");
			cookieThere.setSecure(false);	// Change to true when using SSL.
			response.addCookie (cookieThere);
		} catch (Exception e){
        System.out.println("Problem with accepting cookies " + e);
    }

    try{
	  		// Validate the fields
	  		if(aClassified.checkName() && 
				aClassified.checkEmail() && aClassified.checkPhone() &&
				aClassified.checkStartDate(request, response) && 
				aClassified.checkAmtForDayMoYr() && aClassified.checkWhatAdSays01()){

				// Get all parameters from the html page and store them for a hidden value.
				//
				// NOTE: amtForDayMoYr below uses accesor function amtForDayMoYr()
				String amtForDayMoYr =  request.getParameter("AmtForDayMoYr");
				String name = request.getParameter("Name");
				String address = request.getParameter("Address");
				String city = request.getParameter("City");
				String state = request.getParameter("State");
				String zip = request.getParameter("Zip");
				String email = request.getParameter("Email");
				String phone = request.getParameter("Phone");
				String payMethod = request.getParameter("PayMethod");
				String cardNumber = request.getParameter("CardNumber");
				String cardMo = request.getParameter("CardMo");
				String cardYr = request.getParameter("CardYr");
				String startDate = request.getParameter("StartDate");
				String endDate = aClassified.getEndDate();      
				String classifiedAd = request.getParameter("ClassifiedAd");
				String color = request.getParameter("Color");
				String banner = request.getParameter("Banner");
String adTitle = request.getParameter("AdTitle");
				String whatAdSays01 = request.getParameter("WhatAdSays01");

        //TEST
        // Append Heading onto What the ad says.
//				String whatAdSays01 = "<center><b>" + heading + "</b></center>" 
//            + request.getParameter("WhatAdSays01");

        //END TEST
                
				String webSite = request.getParameter("WebSite");
				// NOTE: uniqueDateId is used in registerStatement below.
				// NOTE: also uniqueDateId uses accesor function getUniqueDateId
				String uniqueDateId = aClassified.getUniqueDateId();

        dbConnection = null;
        /** Should only need one connection for both prepared statements */
        dbConnection = pool.getConnection();

        // Turn on transactions
        dbConnection.setAutoCommit(false);
        
        /**
         * set CustInfo sql parameters
         */      
        registerStatementCustInfo =	
          dbConnection.prepareStatement(Register_Statement_Cust_Into);

        // clear parameters
        registerStatementCustInfo.clearParameters();
    
        // set CustInfo sql parameters
        // p296 JH
        //uniqueDateId = System.currentTimeMillis();
				registerStatementCustInfo.setString(CUST_UNIQUE_DATE_ID_POSITION, uniqueDateId);
        //registerStatementCustInfo.setString(CUST_UNIQUE_DATE_ID_POSITION, Long.toString(uniqueDateId));
				registerStatementCustInfo.setString(CUSTOMER_NAME_POSITION, name);
        registerStatementCustInfo.setString(BILLING_ADDRESS_POSITION, address);
        registerStatementCustInfo.setString(CITY_POSITION, city);
		    registerStatementCustInfo.setString(STATE_POSITION, state);
	    	registerStatementCustInfo.setString(ZIP_POSITION, zip);
				registerStatementCustInfo.setString(PAYMETHOD_POSITION, payMethod);
				registerStatementCustInfo.setString(CARD_NUMBER_POSITION, cardNumber);
				registerStatementCustInfo.setString(CARD_MO_POSITION, cardMo);
				registerStatementCustInfo.setString(CARD_YR_POSITION, cardYr);
				//????registerStatementCustInfo.setDate(START_DATE_POSITION, startDate);
		    registerStatementCustInfo.setString(START_DATE_POSITION, startDate);
		    registerStatementCustInfo.setString(END_DATE_POSITION, endDate); 
				registerStatementCustInfo.setString(AMT_FOR_DAY_MO_YR_POSITION, amtForDayMoYr);

        /**
         * set AdInfo sql parameters
         */
        registerStatementAdInfo =
          dbConnection.prepareStatement(Register_Statement_AdInfo);

        // clear parameters
        registerStatementAdInfo.clearParameters();

				// set AdInfo sql parameters
///				registerStatementAdInfo.setString(AD_UNIQUE_DATE_ID_POSITION, uniqueDateId);
 				registerStatementAdInfo.setString(AD_UNIQUE_DATE_ID_POSITION, uniqueDateId);
				registerStatementAdInfo.setString(CLASSIFIED_AD_POSITION, classifiedAd);
				registerStatementAdInfo.setString(COLOR_POSITION, color);
				registerStatementAdInfo.setString(BANNER_POSITION, banner);
///registerStatementAdInfo.setString(WHATADSAYS_POSITION_01, whatAdSays01);
        /**
         * The Ad Title is added to the string WhatAdSays01 so that it can be searched from the same
         * blob field. I added 3 .'s so the adTitle will look centered when there
         * is a small banner with it. There is no database field for adTitle.
         */
        registerStatementAdInfo.setString(WHATADSAYS_POSITION_01, "<b><center>" + 
          adTitle + " . . .</center></b>" + whatAdSays01);        
	      registerStatementAdInfo.setString(PHONE_POSITION, phone);
        registerStatementAdInfo.setString(EMAIL_POSITION, email);
		    registerStatementAdInfo.setString(WEBSITE_POSITION, webSite);
///			}

        /**
         * Put an html Proof_Read_Page back to the user so they can read over
         * their ad.
         */
				StringBuffer htmlPage = new StringBuffer();
				htmlPage.append("<html><head>");                                                                                                     
        htmlPage.append("<title>Proof Read Page</title></head>");      
				htmlPage.append("<body>");
				htmlPage.append("<center><h1>Proof Read Page</h1></center><hr>");
				htmlPage.append("<body background=../classified/servletimages/newsbg.jpg bgcolor=ffffff>");
				htmlPage.append("<b>Please read over the information you entered below.</b> ");
				//htmlPage.append("<i>Ignore the \"Amount to send in\", this ad is <b>FREE!</b></i> ");
				htmlPage.append("<b>If the information is correct, click the Submit button</b> ");
				htmlPage.append("<b>below otherwise click the Edit button to make changes.</b> ");

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

				htmlPage.append(aClassified.toWebString());

				// Create a "Submit" button.
				htmlPage.append("<FORM METHOD=POST>");
        /**
         * Put a hidden values for each field plus a step2 hidden field for
         * a confirmation page. For security, do not cache the page "setHeader("Expires","-1")".
         */
				htmlPage.append("<INPUT TYPE=\"hidden\" NAME=\"AmtForDayMoYr\" VALUE=\"").append(amtForDayMoYr).append("\">");
				htmlPage.append("<INPUT TYPE=\"hidden\" NAME=\"Name\" VALUE=\"").append(name).append("\">");
				htmlPage.append("<INPUT TYPE=\"hidden\" NAME=\"Address\" VALUE=\"").append(address).append("\">");
				htmlPage.append("<INPUT TYPE=\"hidden\" NAME=\"City\" VALUE=\"").append(city).append("\">");
				htmlPage.append("<INPUT TYPE=\"hidden\" NAME=\"State\" VALUE=\"").append(state).append("\">");
				htmlPage.append("<INPUT TYPE=\"hidden\" NAME=\"Zip\" VALUE=\"").append(zip).append("\">");
				htmlPage.append("<INPUT TYPE=\"hidden\" NAME=\"Email\" VALUE=\"").append(email).append("\">");
				htmlPage.append("<INPUT TYPE=\"hidden\" NAME=\"Phone\" VALUE=\"").append(phone).append("\">");
				htmlPage.append("<INPUT TYPE=\"hidden\" NAME=\"PayMethod\" VALUE=\"").append(payMethod).append("\">");
				htmlPage.append("<INPUT TYPE=\"hidden\" NAME=\"CardNumber\" VALUE=\"").append(cardNumber).append("\">");
				htmlPage.append("<INPUT TYPE=\"hidden\" NAME=\"CardMo\" VALUE=\"").append(cardMo).append("\">");
				htmlPage.append("<INPUT TYPE=\"hidden\" NAME=\"CardYr\" VALUE=\"").append(cardYr).append("\">");
				htmlPage.append("<INPUT TYPE=\"hidden\" NAME=\"StartDate\" VALUE=\"").append(startDate).append("\">");
				htmlPage.append("<INPUT TYPE=\"hidden\" NAME=\"EndDate\" VALUE=\"").append(endDate).append("\">");
				htmlPage.append("<INPUT TYPE=\"hidden\" NAME=\"ClassifiedAd\" VALUE=\"").append(classifiedAd).append("\">");
				htmlPage.append("<INPUT TYPE=\"hidden\" NAME=\"Color\" VALUE=\"").append(color).append("\">");
				htmlPage.append("<INPUT TYPE=\"hidden\" NAME=\"Banner\" VALUE=\"").append(banner).append("\">");
htmlPage.append("<INPUT TYPE=\"hidden\" NAME=\"AdTitle\" VALUE=\"").append(adTitle).append("\">");        
				htmlPage.append("<INPUT TYPE=\"hidden\" NAME=\"WhatAdSays01\" VALUE=\"").append(whatAdSays01).append("\">");
				htmlPage.append("<INPUT TYPE=\"hidden\" NAME=\"WebSite\" VALUE=\"").append(webSite).append("\">");
				htmlPage.append("<INPUT TYPE=\"hidden\" NAME=\"UniqueDateId\" VALUE=\"").append(uniqueDateId).append("\">");
				htmlPage.append("<INPUT TYPE=\"hidden\" NAME=\"action\" VALUE=\"step2\">");
				htmlPage.append("<center><b><i><font color=\"#0033CC\">Please be patient, processing will take a few seconds.</font></i><b><br></center>");

				htmlPage.append("<div align=\"right\"><INPUT TYPE=\"button\" VALUE=\"Edit\" ONCLICK=\"history.go(-1)\">");
				htmlPage.append("<INPUT TYPE=\"button\" VALUE=\"Submit\" ONCLICK=\"doSubmit(this.form)\"></div>");
				htmlPage.append("</body></html>");
        /**
         * You don't want browser to cache the resultset etc.
         */
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
		   	}

			else if(!aClassified.checkName()){
				// build Name error page
				StringBuffer htmlPage = new StringBuffer();
				htmlPage.append("<html><head>");
        htmlPage.append("<title>Name Error Page</title></head>");         
				htmlPage.append("<body background=../classified/servletimages/newsbg.jpg bgcolor=ff0000>");
				htmlPage.append("<center><h1>Name Field Error Page</h1></center><hr>");
				htmlPage.append("<b>Sorry...</b>The information for the <b>Name</b> field does not ");
				htmlPage.append("appear to be valid. <b>Press the Edit button if</b>");
				htmlPage.append("<b> you wish to correct the information you provided.</b>");

				// Put back button in cell or it will not show up
				htmlPage.append("<div align=\"right\"><TABLE BORDER=\"1\" WIDTH=\"7%\"><TR><TD WIDTH=\"100%\" BGCOLOR=\"silver\">");
				//htmlPage.append("<ALIGN=\"CENTER\"><A HREF=# ONCLICK=\"history.go(-1)\"><FONT COLOR=\"black\"><b>Edit</b></FONT></A>");
				htmlPage.append("<ALIGN=\"CENTER\"><A HREF=\"javascript:history.go(-1)\"><FONT COLOR=\"black\"><b>Edit</b></FONT></A>");        
				htmlPage.append("</TD></TR></TABLE></DIV>");

				htmlPage.append("<hr>");
				htmlPage.append(aClassified.toWebString());

				htmlPage.append("<center><a href=../classified/index.jsp>Return to Home Page</a>");
				htmlPage.append("<p><i>" + this.getServletInfo() + "</i>");
				htmlPage.append("</center></body></html>");
				
	      // now let's send this dynamic data
	      // back to the browser
	      response.setContentType("text/html");
	      outputToBrowser.println(htmlPage);
				outputToBrowser.flush();
	      outputToBrowser.close();
			}
/**
 *     // htmlPage.append("<base href=http://" + request.getServerName() + ":" + request.getServerPort()  + request.getContextPath() + "/classified/ >").append(CR);
 * 
 */
			else if(!aClassified.checkEmail()){
				// build Email error page
				StringBuffer htmlPage = new StringBuffer();
				htmlPage.append("<html><head>");
        htmlPage.append("<title>Email Error Page</title></head>");
				htmlPage.append("<body background=../classified/servletimages/newsbg.jpg bgcolor=ff0000>");
				htmlPage.append("<center><h1>Email Field Error Page</h1></center><hr>");
				htmlPage.append("<b>Sorry...</b>The information for the <b>Email</b> field does not ");
				htmlPage.append("appear to be valid. <b>Press the Edit button if</b>");
				htmlPage.append("<b> you wish to correct the information you provided.</b>");
				htmlPage.append(" Make sure you use the correct format <b>name@somewhere.com</b>");

				// Put back button in cell or it will not show up
				htmlPage.append("<div align=\"right\"><TABLE BORDER=\"1\" WIDTH=\"7%\"><TR><TD WIDTH=\"100%\" BGCOLOR=\"silver\">");
				htmlPage.append("<ALIGN=\"CENTER\"><A HREF=\"javascript:history.go(-1)\"><FONT COLOR=\"black\"><b>Edit</b></FONT></A>");       
				htmlPage.append("</TD></TR></TABLE></DIV>");

				htmlPage.append("<hr>");
				htmlPage.append(aClassified.toWebString());
				htmlPage.append("<center><a href=../classified/index.jsp>Return to Home Page</a>");
				htmlPage.append("<p><i>" + this.getServletInfo() + "</i>");
				htmlPage.append("</center></body></html>");

	      // now let's send this dynamic data
	      // back to the browser

	      response.setContentType("text/html");
	      outputToBrowser.println(htmlPage);
				outputToBrowser.flush();
	      outputToBrowser.close();
			}

			else if(!aClassified.checkPhone()){
				// build Phone error page
				StringBuffer htmlPage = new StringBuffer();
				htmlPage.append("<html><head>");
        htmlPage.append("<title>Phone Error Page</title></head>");      
				htmlPage.append("<body background=../classified/servletimages/newsbg.jpg bgcolor=ff0000>");
				htmlPage.append("<center><h1>Phone Field Error Page</h1></center><hr>");
				htmlPage.append("<b>Sorry...</b>The information for the <b>Phone</b> field does not ");
				htmlPage.append("appear to be valid. <b>Press the Edit button if</b>");
				htmlPage.append("<b> you wish to correct the information you provided.</b>");
				htmlPage.append(" Make sure you use the format<b> nnn-nnn-nnnn.</b>");
				htmlPage.append(" If you chose to only show E-mail, then the Phone field");
				htmlPage.append(" must be blank.");

				// Put back button in cell or it will not show up
				htmlPage.append("<div align=\"right\"><TABLE BORDER=\"1\" WIDTH=\"7%\"><TR><TD WIDTH=\"100%\" BGCOLOR=\"silver\">");
				htmlPage.append("<ALIGN=\"CENTER\"><A HREF=\"javascript:history.go(-1)\"><FONT COLOR=\"black\"><b>Edit</b></FONT></A>");       
				htmlPage.append("</TD></TR></TABLE></DIV>");

				htmlPage.append("<hr>");
				htmlPage.append(aClassified.toWebString());
				htmlPage.append("<center><a href=../classified/index.jsp>Return to Home Page</a>");
				htmlPage.append("<p><i>" + this.getServletInfo() + "</i>");
				htmlPage.append("</center></body></html>");
				
	      // now let's send this dynamic data
	      // back to the browser

	      response.setContentType("text/html");
	      outputToBrowser.println(htmlPage);
				outputToBrowser.flush();
	      outputToBrowser.close();
			}

			else if(!aClassified.checkStartDate(request, response)){
				// build Start Date error page
				StringBuffer htmlPage = new StringBuffer();
				htmlPage.append("<html><head>");
        htmlPage.append("<title>Start Date Error Page</title></head>");         
				htmlPage.append("<body background=../classified/servletimages/newsbg.jpg bgcolor=ff0000>");
				htmlPage.append("<center><h1>Start Date Field Error Page</h1></center><hr>");
				htmlPage.append("<b>Sorry...</b>The information for the <b>Start Date</b> field does not ");
				htmlPage.append("appear to be valid, the start date must be <b>THREE TO FIVE DAYS FROM</b> ");
				htmlPage.append("<b>TODAYS DATE</b> for context & check validation! For example, if ");
				htmlPage.append("todays date is: 01/05/04...valid dates are from 01/08/04 to 01/12/04 ");
				htmlPage.append("<b>Press the Edit button if</b> you wish to correct the information.</b> ");
				htmlPage.append("Make sure you use this exact format<b> MM/dd/yy</b> ");

				// Put back button in cell or it will not show up
				htmlPage.append("<div align=\"right\"><TABLE BORDER=\"1\" WIDTH=\"7%\"><TR><TD WIDTH=\"100%\" BGCOLOR=\"silver\">");
				htmlPage.append("<ALIGN=\"CENTER\"><A HREF=\"javascript:history.go(-1)\"><FONT COLOR=\"black\"><b>Edit</b></FONT></A>");       
				htmlPage.append("</TD></TR></TABLE></DIV>");

				htmlPage.append("<hr>");
				htmlPage.append(aClassified.toWebString());
				htmlPage.append("<center><a href=../classified/index.jsp>Return to Home Page</a>");
				htmlPage.append("<p><i>" + this.getServletInfo() + "</i>");
				htmlPage.append("</center></body></html>");

	      // now let's send this dynamic data
	      // back to the browser

	      response.setContentType("text/html");
	      outputToBrowser.println(htmlPage);
				outputToBrowser.flush();
	      outputToBrowser.close();
			}

			else if(!aClassified.checkAmtForDayMoYr()){
				// build Number Of Days error page
				StringBuffer htmlPage = new StringBuffer();
				htmlPage.append("<html><head>");
        htmlPage.append("<title>Number Of Days Error Page</title></head>");      
				htmlPage.append("<body background=../classified/servletimages/newsbg.jpg bgcolor=ff0000>");
				htmlPage.append("<center><h1>Number Of Days Field Error Page</h1></center><hr>");
				htmlPage.append("<b>Sorry...</b>The information for the <b>Number Of Days</b> field does not ");
				htmlPage.append("appear to be valid. <b>Press the Edit button if</b>");
				htmlPage.append("<b> you wish to correct the information you provided.</b>");
				htmlPage.append(" The minimum time to run an ad is<b> ten days.</b>");

				// Put back button in cell or it will not show up
				htmlPage.append("<div align=\"right\"><TABLE BORDER=\"1\" WIDTH=\"7%\"><TR><TD WIDTH=\"100%\" BGCOLOR=\"silver\">");
				htmlPage.append("<ALIGN=\"CENTER\"><A HREF=# ONCLICK=\"history.go(-1)\"><FONT COLOR=\"black\"><b>Edit</b></FONT></A>");
				htmlPage.append("</TD></TR></TABLE></DIV>");

				htmlPage.append("<hr>");
				htmlPage.append(aClassified.toWebString());
				htmlPage.append("<center><a href=../classified/index.jsp>Return to Home Page</a>");
				htmlPage.append("<p><i>" + this.getServletInfo() + "</i>");
				htmlPage.append("</center></body></html>");

        // now let's send this dynamic data
        // back to the browser
	      response.setContentType("text/html");
        outputToBrowser.println(htmlPage);
				outputToBrowser.flush();
        outputToBrowser.close();
			}

			else if(!aClassified.checkWhatAdSays01()){
				// build Check What Ad Says error page
				StringBuffer htmlPage = new StringBuffer();
				htmlPage.append("<html><head>");
        htmlPage.append("<title>What Ad Says Error Page</title></head>");      
				htmlPage.append("<body background=../classified/servletimages/newsbg.jpg bgcolor=ff0000>");
				htmlPage.append("<center><h1>What Ad Says Error Page</h1></center><hr>");
				htmlPage.append("<b>Sorry...</b>The information for the <b>What Ad Says</b> field does not ");
				htmlPage.append("appear to be valid. <b>Press the Edit button if</b>");
				htmlPage.append("<b> you wish to correct the information you provided.</b>");
				htmlPage.append(" Make sure you <b> have at least ten characters in your ad</b>");

				// Put back button in cell or it will not show up
				htmlPage.append("<div align=\"right\"><TABLE BORDER=\"1\" WIDTH=\"7%\"><TR><TD WIDTH=\"100%\" BGCOLOR=\"silver\">");
				htmlPage.append("<ALIGN=\"CENTER\"><A HREF=\"javascript:history.go(-1)\"><FONT COLOR=\"black\"><b>Edit</b></FONT></A>");       
				htmlPage.append("</TD></TR></TABLE></DIV>");

				htmlPage.append("<hr>");
				htmlPage.append(aClassified.toWebString());
				htmlPage.append("<center><a href=../classified/index.jsp>Return to Home Page</a>");
				htmlPage.append("<p><i>" + this.getServletInfo() + "</i>");
				htmlPage.append("</center></body></html>");

        // now let's send this dynamic data
        // back to the browser

        response.setContentType("text/html");
        outputToBrowser.println(htmlPage);
				outputToBrowser.flush();
        outputToBrowser.close();
			}
		}
    		catch (Exception e){
	    		System.out.println(" 2-DID IT GET HERE? " + e); //***************** NO
          ///cleanUp();
          e.printStackTrace();
    		}	
	}
			
	 // Execute sql if "Submit" button for Step2 is pressed otherwise user
	 // must click the back button on the browser to correct info.
	 if(action.equals("step2")){
		try {

			// Is the cookie there from step-1 above? Search the received cookies
			Cookie[] anyCookies = request.getCookies();

			if ((anyCookies == null) || (anyCookies.length == 0 )){
				System.out.println("Cookies Turned Off");
				response.sendRedirect("../classified/Notice.html"); // Not there.
				return;
			}
    
      /**
       * Put a date cookie in.
       * 
       * Check if the cookie for BLUEADS is there and if it is,
       * is it same date as today or null. If not the later, create a new cookie.
       */
///			String cookieName = null;
///			String value = null;
///			boolean beenHereBefore = false;
///			Cookie[] blueadsCookie = request.getCookies();

///   		if ((blueadsCookie != null) && (action.equals("step2"))){
///        for (int i =0; i < blueadsCookie.length; i++) {
///          value = blueadsCookie[i].getValue();
		
///          if ((blueadsCookie[i].getName().equals("BLUEADS")) 
///                        && (value.equals(formatter1.format(todaysDate)))) {
///                beenHereBefore = true;
///          }
///        }
///   		}

    /**
     * Cookie setup to allow three cookies (or ads) in a 24 hour period
     * otherwise the user gets redirected to an error page.
     */
     // THIS IS ALL TEST CODE much of which is coppied from above!
			String cookieName = null;
			String value = null;
///      int cookieAdCounter = 0;
			boolean beenHereBefore = false;
			Cookie[] blueadsCookie = request.getCookies();

   		if ((blueadsCookie != null) && (action.equals("step2"))){
        for (int i =0; i < blueadsCookie.length; i++) {
          value = blueadsCookie[i].getValue();
		
          if ((blueadsCookie[i].getName().equals("BLUEADS")) 
                        && (value.equals(formatter1.format(todaysDate)))) {
                  cookieAdCounter += 1;
System.out.println("cookieAdCounter is " + cookieAdCounter);                
                if(cookieAdCounter > 3)
                  beenHereBefore = true;
          }
        }
   		}		
    /** ---------------------  End Cookie Setup ---------------------------- */

      
/**
 * TOM UNCOMMENT ALL LINES BELOW TO GET COOKIES TO WORK AGAIN!!
 */
 
//   if (beenHereBefore == true){    
//      response.sendRedirect("../classified/Notice.html");
//      return;
//	  }else{
//      // Put in new cookie with todays date.  // Name    // Value
//      Cookie newBlueadsCookie = new Cookie ("BLUEADS", formatter1.format(todaysDate));
//      // Cookie expires in 24 hours. // 60 sec * 60 min * 24 hrs = 86400 seconds
//      newBlueadsCookie.setMaxAge (60 * 60 * 24); 
//      newBlueadsCookie.setPath ("/");
//      newBlueadsCookie.setSecure(false); // Change to true for SSL.
//      response.addCookie (newBlueadsCookie);
//      // FOR TEST System.out.println("New BLUEADS Cookie = " + newBlueadsCookie + " New Date " + formatter1.format(todaysDate));
//	   }		
/**
 *  UN-COMMENT UP TO HERE ONLY FOR COOKIES!
 */


		// Get all parameters from the step1 html page to display their values.
		//
		String amtForDayMoYr = request.getParameter("AmtForDayMoYr");
		String name = request.getParameter("Name");
		String address = request.getParameter("Address");
		String city = request.getParameter("City");
		String state = request.getParameter("State");
		String zip = request.getParameter("Zip");
		String email = request.getParameter("Email");
		String phone = request.getParameter("Phone");
		String payMethod = request.getParameter("PayMethod");
		String cardNumber = request.getParameter("CardNumber");
		String cardMo = request.getParameter("CardMo");
		String cardYr = request.getParameter("CardYr");
		String startDate = request.getParameter("StartDate");
		String endDate = request.getParameter("EndDate");
		String classifiedAd = request.getParameter("ClassifiedAd");
		String color = request.getParameter("Color");
		String banner = request.getParameter("Banner");
String adTitle = request.getParameter("AdTitle").trim();
		String whatAdSays01 = request.getParameter("WhatAdSays01");
		String webSite = request.getParameter("WebSite");
		String uniqueDateId =  request.getParameter("UniqueDateId");


		// Execute the SQL query and commit the statement for CustInfo table.
    try{
    	// Turn on transactions
///			dbConnection.setAutoCommit(false); done in code above
      // execute update
      registerStatementCustInfo.executeUpdate();
			// Commit statement if no errors.
			dbConnection.commit();      
    }catch (Exception exp){
      // Any error is grounds for rollback.
			dbConnection.rollback();	
      System.out.println("Have an exception, rolling back transaction " + exp);
			outputToBrowser.println("Insert in CustInfo failed. Please contact technical support.");      
    }

		// Execute the SQL query and commit the statement for AdInfo table.
    try{
    	// Turn on transactions
			dbConnection.setAutoCommit(false);
      // execute update    
      registerStatementAdInfo.executeUpdate();
      dbConnection.commit();// weblogic pooling does not like.
    }catch (Exception exp){
      // Any error is grounds for rollback.
			dbConnection.rollback();	
      System.out.println("Have an exception, rolling back transaction " + exp);
			outputToBrowser.println("Insert in AdInfo failed. Please contact technical support.");      
    }

    // build confirmation page
		StringBuffer htmlPage = new StringBuffer();
		htmlPage.append("<html><head>");
    htmlPage.append("<title>Confirmation Page</title></head>");   
		htmlPage.append("<body>");
		htmlPage.append("<center><h1>Confirmation Page</h1></center><hr>");
		htmlPage.append("<body background=../classified/servletimages/newsbg.jpg bgcolor=ffffff>");
		htmlPage.append("<b>Congratulations: </b>The following information was entered successfully. ");
		//htmlPage.append("<i>Ignore the \"Amount to send in\", this ad is <b>FREE!</b></i> ");
		htmlPage.append("Please read the instructions at the bottom of this page. ");

		htmlPage.append("<ul>");
		//htmlPage.append("<li><B>Amount to send in:</B> $").append(amtForDayMoYr);
		htmlPage.append("<li><B>Name:</B> ").append(name);
		///htmlPage.append("<li><B>Address:</B> ").append(address);
		///htmlPage.append("<li><B>City:</B> ").append(city);
		///htmlPage.append("<li><B>State:</B> ").append(state);
		///htmlPage.append("<li><B>Zip:</B> ").append(zip);
		htmlPage.append("<li><B>E-mail:</B> ").append(email);
		htmlPage.append("<li><B>Phone:</B> ").append(phone);
		///htmlPage.append("<li><B>Payment Method:</B> ").append(payMethod);
		///htmlPage.append("<li><B>Credit Card Number:</B> ").append(cardNumber).append("  <b>Expiration Mo:</b> ").append(cardMo).append(" <b>Yr:</b> ").append(cardYr);
		htmlPage.append("<li><B>Ad Start Date:</B> ").append(startDate).append("  <B>Ad Ending Date:</B> ").append(endDate);
		htmlPage.append("<li><B>Classified Ad:</B> ").append(classifiedAd);
		///htmlPage.append("<li><B>Color:</B> ").append(color).append("COLOR</FONT>");
		htmlPage.append("<li><B>").append(color).append("</B></FONT>");
		htmlPage.append("<li><B>Banner</B> ").append(banner).append("<br>");
///		htmlPage.append("<li><B>What Ad Says:</B> ");
///		htmlPage.append("<li></li>").append(whatAdSays01);
/////////////////////// TEST
    htmlPage.append("<br><br><li><B>Ad Title:</B></li> ").append(adTitle);
    htmlPage.append("<li><B>What Ad Says:</B></li> ").append(whatAdSays01);
		//htmlPage.append("<li></li>").append(whatAdSays01);


/// END TEST

    
		htmlPage.append("<li><B>WebSite:</B> ").append(webSite);
		htmlPage.append("<li><B>Ad I.D. Number: </B>").append(uniqueDateId);
		htmlPage.append("</ul>");

		htmlPage.append("<hr>");
		htmlPage.append("<b>Please Note: we reserve the right to refuse any ad. </b><br>");
		htmlPage.append("<b>Print a copy of this page so you have one for your records. </b><br>");
		htmlPage.append("<b>Make sure you save the 12-Digit Ad I.D. Number shown on this form, so you </b><br>");
		htmlPage.append("<b>can refer to it, if you want to delete your ad. </b><br><br>");
		//htmlPage.append("<address><li><b>blueads.com</b><br>");
		//htmlPage.append("<li><b>3203 S. 116 Ave.</b><br>");
		//htmlPage.append("<li><b>Omaha, NE 68144</b><br>");
		//htmlPage.append("</address>");
		htmlPage.append("<hr>");
		htmlPage.append("<center><b><a href=../classified/index.jsp>Click Here To Continue</a></b>");
		//htmlPage.append("<a href=/servlet/SubmitDBServlet> | View Classified List</a>");
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

		///////////////////////////// Mail configuration //////////////////////////

		
			// Now send e-mail with Ad i.d. number
			if(!(email.equals("")) && (action.equals("step2"))){
				
				String msgText = "Thank you for using http://www.classifiedAds.com.\n" +
				"\tWe are commited to keeping our customers satified." +
				" Here is your ad id number: " + uniqueDateId + ". Use this number if" +
				" your item(s) or service(s) have accomplished there results" + 
				" and you would like to delete your ad so you are no longer contacted." +
				" Your ad will run from " + startDate + " to " + endDate + ".\n" +
				///"Your Ad Reads As Follows:\n" + 
////// TEST        
"Your Ad Reads As Follows -> \n" + adTitle + ": " + whatAdSays01 + "\n";
////// END TEST
				///whatAdSays01 + "\n";
				
				/////String to = "blue-j@worldnet.att.net";
				///String from = "blueads.com";
        //String from = "javaman@worldnet.att.net";
        //String host = "smtp.central.cox.net";
				//String host = "mailhost.worldnet.att.net"; 
				//String host = "mail.nfinity.com"; 

				boolean debug = Boolean.valueOf("true").booleanValue(); // Change to false to turn off debug.
	
				// create some properties and get the default Session
				Properties props = new Properties();
				///props.put("mail.smtp.host", MAIL_HOST);        
        props.put("mail.smtp.host", email_bundle.getString("MAIL_HOST"));      

				if (debug) props.put("mail.debug", "true"); // Change to false to turn off debug.

				javax.mail.Session emailSession = javax.mail.Session.getDefaultInstance(props, null);
				emailSession.setDebug(debug);

				try {
            /**
             * Create a message
             * Note the MAIL_FROM & MAIL_HOST are declared final, static at top
             * of this page.
             * But now I use a ResourceBundle located in the directory
             * com.classified.common.emailProperties.properties
             */
				    Message msg = new MimeMessage(emailSession);
				    ///msg.setFrom(new InternetAddress(from));
				    ///msg.setFrom(new InternetAddress(MAIL_FROM));
            msg.setFrom(new InternetAddress(email_bundle.getString("MAIL_FROM")));   

            
				    //InternetAddress[] emailAddress = {new InternetAddress(to)};
				    InternetAddress[] emailAddress = {new InternetAddress(email)};
				    msg.setRecipients(Message.RecipientType.TO, emailAddress);
				    msg.setSubject("Classified Ad I.D. Number");
				    msg.setSentDate(new java.util.Date());
				    // If the desired charset is known, you can use
				    // setText(text, charset)
				    msg.setText(msgText);	    
				    Transport.send(msg);
				}catch (javax.mail.MessagingException mex){
				    System.out.println("\n--Exception Submit's Email");
					System.out.println("Email is = to " + email);
				}					
			}			
		////////////////////////// End Mail configuration //////////////////////////

		return;
		}
		catch (Exception e){
      System.out.println("You have an database exception " + e);
			// Any error is grounds for rollback
			try{
				dbConnection.rollback();
			}catch(Exception sqlx){
        System.out.println("Need to rollback SubmitDBServlet " + sqlx);
        /** Send the user back to the SubmitClassified.jsp page */
        response.sendRedirect(request.getScheme() + "://" +
        request.getServerName() + ":" + request.getServerPort() +
          "/classified/SubmitClassified.jsp");  
      }
            System.out.println(" 2-DID IT GET HERE? ");
        	  e.printStackTrace();
    }finally{ // put a finally clause here to release connection pool p269 Jason H.
      if (dbConnection != null)
        try{ 
            registerStatementCustInfo.close();
            registerStatementAdInfo.close();
            dbConnection.close();   
        }catch (SQLException sqe){
              System.out.println("Error on finally SubmitDBServlet " + sqe);
        }		
    }
   }
   } // end of synchronize(session)
  }

  public void destroy(){
    System.out.println("SubmitDBServlet: destroy");
     /// cleanUp();
  }

  public String getServletInfo(){
    return "<i>Classified Ads, v.02</i>";
  }

	// Test to see if you lost a database
	public boolean isConnectionClosed(){
    try{
          if (dbConnection != null)
            return dbConnection.isClosed();
          else
            return true;
		}catch (SQLException e){
				return true;
		}
	}
}


