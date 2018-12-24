/* 
 * File:  SubmitNowDBServlet.java
 * 
 * @version 0.2 1/17/03
 * 
 * Tom Kochanowicz 
 * 
 * This Servlet allows the user to submit more then one ad, the user must login 
 * SubmitLogin.html and LoginSubmitHandler.java servlet. After the user logs in,
 * they can enter an ad with the SubmitNow.html page. After an ad is entered, 
 * this servlet sends out a confirmation email.
 */


import java.awt.*;
import java.util.*;
import java.util.Properties;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import java.io.*;
import java.text.*;

////////////////////////////////////////////////////////////////////import weblogic.db.jdbc.*;
////////////////////////////////////////////////////////////////////import weblogic.common.*;

// For mail
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;
import javax.naming.*;
import javax.sql.*;

// Put the line below this comment under the CLASSPATH D:\lib\java and 
// then the path bluejay\register. Classified.class will be in the directory
// D:\lib\java\bluejay\register so it works as a package.

import bluejay.register.Classified;

/**
 *  This servlet provides checks the data before entry
 *  classified ad data in a database and lets the user read over their data.
 *
 *  @author Tom Kochanowicz,  blue-j@worldnet.att.net
 *  @version 0.1, 21 April 1999
 *  @version 0.2, 15 Jan. 2003 
 *  
 */
public class SubmitNowDBServlet extends HttpServlet
{
  // data members
  protected Connection dbConnection;
  private javax.sql.DataSource ds;
  protected PreparedStatement registerStatementCustInfo;
  protected PreparedStatement registerStatementAdInfo;
  protected String CR = "\n";
  protected int submitCount = 0;

	// CustInfo table
	// UniqueDateId is a 1:1 relationship in both CustInfo & AdInfo tables.
	protected final int CUST_UNIQUE_DATE_ID_POSITION = 1;
	protected final int CUSTOMER_NAME_POSITION = 2;
  protected final int BILLING_ADDRESS_POSITION  = 3;
  protected final int CITY_POSITION = 4;
	protected final int STATE_POSITION = 5;
	protected final int ZIP_POSITION = 6;
	protected final int PAYMETHOD_POSITION = 7;
	protected final int CARD_NUMBER_POSITION = 8;
	protected final int CARD_MO_POSITION = 9;
	protected final int CARD_YR_POSITION = 10;
	protected final int START_DATE_POSITION = 11;
	protected final int END_DATE_POSITION = 12;
	protected final int AMT_FOR_DAY_MO_YR_POSITION = 13;

	// AdInfo table
	protected final int AD_UNIQUE_DATE_ID_POSITION = 1;
	protected final int CLASSIFIED_AD_POSITION = 2;
	protected final int COLOR_POSITION = 3;
	protected final int BANNER_POSITION = 4;
  protected final int WHATADSAYS_POSITION_01 = 5; // TEST was 4
	protected final int PHONE_POSITION = 6;
	protected final int EMAIL_POSITION = 7;
	protected final int WEBSITE_POSITION = 8;

	protected Properties p = new Properties();
	protected String dbURL = "jdbc:weblogic:pool:blueadsPool";
	//protected String dbURL = "jdbc:weblogic:mssqlserver:Classifieds";


	// The two lines below will bypass dbAnywhere server
	//protected String dbURL = "jdbc:odbc:Classifieds";
	//protected String driverName = "sun.jdbc.odbc.JdbcOdbcDriver";

    public void init(ServletConfig config) throws ServletException
    {
        super.init(config);

        // use println statements to send status messages to web server console
        try {
            System.out.println("SubmitNowDBServlet init: Start");
		
			//* p.put("user", "system");
			//* p.put("password", "cluster4");
			//p.put("server", "BLUEADS");
			//p.put("db", "Classifieds");
			//Driver driver = (Driver)Class.forName(driverName).newInstance();

			// FOR TESTING // Compiler.disable();  //turn off JIT so we can see line numbers when debugging      
		
			dbConnection = null;
///// Start new code ///////
     String myDBConnect = "No SubmitNow DB Source Connection";
      Context ctx = new InitialContext();
      if(ctx == null ) 
          throw new Exception("Boom - No Context" + myDBConnect);
      // Look up datasource in server.xml & Resource Ref in web.xml
      //javax.sql.DataSource ds = 
      ds = (javax.sql.DataSource)ctx.lookup("java:comp/env/jdbc/ClassifiedsDB");
      
      if (ds != null) {
         dbConnection = ds.getConnection();
      }
               
        if(dbConnection != null)  {
            myDBConnect = "Got Connection " + dbConnection.toString();
            System.out.println(myDBConnect);
        }
////// End new code ////////      
			//*Class.forName("weblogic.jdbc.pool.Driver").newInstance();
			//*Connection dbConnection = DriverManager.getConnection("jdbc:weblogic:pool:blueadsPool", p);
			//*System.out.println("SubmitNowDBServlet init: Loading Database Driver");			
 			//*System.out.println("SubmitNowDBServlet init: Getting a connection to - " + dbURL);

			//dbConnection = driver.connect(dbURL, p);
			synchronized(dbConnection){

         		registerStatementCustInfo =
               			dbConnection.prepareStatement("insert into CustInfo "
		 		+ "(UniqueDateId, Name, Address, City, State, Zip, PayMethod, CardNumber, CardMo, CardYr, StartDate, EndDate, AmtForDayMoYr)"
				+ " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

         		registerStatementAdInfo =
				dbConnection.prepareStatement("insert into AdInfo "
		 		+ "(UniqueDateId, ClassifiedAd, Color, Banner, WhatAdSays01, Phone, Email, WebSite)"
				+ " values (?, ?, ?, ?, ?, ?, ?, ?)");

            		System.out.println("SubmitNowDBServlet init: End");
			}
        }
        catch (Exception e)
        {
            cleanUp();
            e.printStackTrace();
        }
    }

   public void doPost(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException
   {
    // Have todaysDate to reference for comparison wiht BLUEADS cookie
    Calendar calendar1;
    SimpleDateFormat formatter1;
    java.util.Date todaysDate;

    // Format todaysDate
    calendar1 = Calendar.getInstance();
    formatter1 = new SimpleDateFormat("MM/dd/yy");
    formatter1.setTimeZone(java.util.TimeZone.getDefault());  // bug fix so not PST & is CST
    todaysDate = calendar1.getTime();

	 // Get the session object or create one if not exist. A session will
	 // persist as long as the client browser maintains a connection with
	 // the server. Also check if their is a connection with the database.
	 		PrintWriter outputToBrowser =  new PrintWriter(response.getOutputStream());
	 		HttpSession session = request.getSession(true);


			// Does the session indicate this user already logged in?
			// Note, SubmitNowDBServlet uses LoginSubmitHandler. 
			
			// Object done = session.getValue("Slogin.isDone");		// marker object
      Object done = session.getAttribute("Slogin.isDone");		// marker object
			if(done == null){
				// No Slogin.isDone means he hasn't logged in.
				// Save the request URL as the true target and redirect to the SubmitLogin.html page.

				//session.putValue("Slogin.target", "/SubmitNow.html");
        session.setAttribute("Slogin.target", "/classified/SubmitNow.html");
				//session.putValue("Slogin.target",
				//		HttpUtils.getRequestURL(request).toString());
				response.sendRedirect(request.getScheme() + "://" +
							request.getServerName() + ":" + request.getServerPort() +
							"/classified/SubmitLogin.html");
				return;
			}
				
				// We should always get a session back & check for database connection.
				if(session == null){
					outputToBrowser.println("ERROR: Submit Database session is null.");
					outputToBrowser.flush();
					outputToBrowser.close();
					return;
				}

				// Make sure database connection is NOT closed. If dataResultSet
				// is NOT closed before the datebase is closed dbAnywhere will crash.

// USE TO TEST DATABASE			if (isConnectionClosed()){
//					outputToBrowser.println("ERROR: Submit Database connection closed.");
//					return;
//				}


    synchronized(session){
			
	// Test if Place An Ad button was pressed
   	String action = request.getParameter("action");

	// Create a new classified ad based on the form data
    	Classified aClassified = new Classified(request);


   	if (action.equals("step1")){ 

		//
		// First check if the user pressed the LogOff button
		//
		try{
			String logOff = request.getParameter("LogOff");
			//System.out.println("LOGOFF = " + logOff);

			if(logOff.equals(null)){ // prevent null pointer
				logOff="LoggedOn";
			}
			else if (logOff.equals("LogOff")){
				try{
					session.invalidate();
					response.sendRedirect(request.getScheme() + "://" +
						request.getServerName() + ":" + request.getServerPort() +
							"/SubmitLogin.html");
						return;
				}
				catch(IllegalStateException e) {
					System.out.println("Exception " + e);
				}
			}
			//System.out.println("LOGOFF = " + logOff);
		}
		catch (NullPointerException e){System.out.println("PROBLEM WITH LOGOUT NULL POINTER" + e.getMessage());}
		catch (RuntimeException e){System.out.println("PROBLEM WITH LOGOUT" + e.getMessage());}

		// This is a hidden field in the SubmitClassified.shtml form. If the user
		// clicks on the submit button, the servlet will do all the users input
		// field tests. If an error occurs the user will get an error page,
		// otherwise they will proceed to step2 where they will finalize it to
		// the database.

        	try {
         
	  		// Validate the fields
	  		if(aClassified.checkName()  && 
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
				String whatAdSays01 = request.getParameter("WhatAdSays01");
				String webSite = request.getParameter("WebSite");
				// NOTE: uniqueDateId is used in registerStatement below.
				// NOTE: also uniqueDateId uses accesor function getUniqueDateId
				String uniqueDateId = aClassified.getUniqueDateId();

				/////dbConnection.setAutoCommit(false); // weblogic pooling does not like.
			synchronized(registerStatementCustInfo){	  		
            			// set CustInfo sql parameters
				registerStatementCustInfo.setString(CUST_UNIQUE_DATE_ID_POSITION, uniqueDateId);
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
			}

			synchronized(registerStatementAdInfo){
				// set AdInfo sql parameters
				registerStatementAdInfo.setString(AD_UNIQUE_DATE_ID_POSITION, uniqueDateId);
				registerStatementAdInfo.setString(CLASSIFIED_AD_POSITION, classifiedAd);
				registerStatementAdInfo.setString(COLOR_POSITION, color);
				registerStatementAdInfo.setString(BANNER_POSITION, banner);
	      registerStatementAdInfo.setString(WHATADSAYS_POSITION_01, whatAdSays01);
	      registerStatementAdInfo.setString(PHONE_POSITION, phone);
        registerStatementAdInfo.setString(EMAIL_POSITION, email);
		    registerStatementAdInfo.setString(WEBSITE_POSITION, webSite);
			}
		    	
				//System.out.println(aClassified.getClassifiedAd()); //***************
				
				// Put an html Proof Read Page back to the user so they can read over
				// their ad.
				StringBuffer htmlPage = new StringBuffer();
				htmlPage.append("<html><head><title>Proof Read Page</title></head>");
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

				// Put a hidden values for each field plus a step2 hidden field for 
				// a confirmation page. For security, do not cache the page "setHeader("Expires","-1")".
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
				htmlPage.append("<INPUT TYPE=\"hidden\" NAME=\"WhatAdSays01\" VALUE=\"").append(whatAdSays01).append("\">");
				htmlPage.append("<INPUT TYPE=\"hidden\" NAME=\"WebSite\" VALUE=\"").append(webSite).append("\">");
				htmlPage.append("<INPUT TYPE=\"hidden\" NAME=\"UniqueDateId\" VALUE=\"").append(uniqueDateId).append("\">");
				htmlPage.append("<INPUT TYPE=\"hidden\" NAME=\"action\" VALUE=\"step2\">");
				htmlPage.append("<center><b><i><font color=\"#0033CC\">Please be patient, processing will take a few seconds.</font></i><b><br></center>");

				htmlPage.append("<div align=\"right\"><INPUT TYPE=\"button\" VALUE=\"Edit\" ONCLICK=\"history.go(-1)\">");
				htmlPage.append("<INPUT TYPE=\"button\" VALUE=\"Submit\" ONCLICK=\"doSubmit(this.form)\"></div>");
				htmlPage.append("</body></html>");

			    	// now let's send this dynamic data
		       	// back to the browser

				// Don't Cache if uncommented
				response.setHeader("Expires", "Mon, 01 Jan 2090 06:00:01 GMT");
				//response.setHeader("Cache-Control", "no-cache");
				//response.setHeader("Pragma", "no-cache");

				////response.setHeader("Expires","-1"); // do not cache page.
	  	  response.setContentType("text/html");
			  outputToBrowser.println(htmlPage);
				outputToBrowser.flush();
	      outputToBrowser.close();
				return;
		   	}

			else if(!aClassified.checkName())
			{
				// build Name error page

				StringBuffer htmlPage = new StringBuffer();
				htmlPage.append("<html><head><title>Name Error Page</title></head>");
				htmlPage.append("<body background=../classified/servletimages/newsbg.jpg bgcolor=ff0000>");
				htmlPage.append("<center><h1>Name Field Error Page</h1></center><hr>");
				htmlPage.append("<b>Sorry...</b>The information for the <b>Name</b> field does not ");
				htmlPage.append("appear to be valid. <b>Press the Edit button if</b>");
				htmlPage.append("<b> you wish to correct the information you provided.</b>");


				// Put back button in cell or it will not show up
				htmlPage.append("<div align=\"right\"><TABLE BORDER=\"1\" WIDTH=\"7%\"><TR><TD WIDTH=\"100%\" BGCOLOR=\"silver\">");
				htmlPage.append("<ALIGN=\"CENTER\"><A HREF=# ONCLICK=\"history.go(-1)\"><FONT COLOR=\"black\"><b>Edit</b></FONT></A>");
				htmlPage.append("</TD></TR></TABLE></DIV>");

				htmlPage.append("<hr>");
				htmlPage.append(aClassified.toWebString());

				htmlPage.append("<center><a href=/classified/index.jsp>Return to Home Page</a>");
				htmlPage.append("<p><i>" + this.getServletInfo() + "</i>");
				htmlPage.append("</center></body></html>");
				
        // now let's send this dynamic data
        // back to the browser

        response.setContentType("text/html");
        outputToBrowser.println(htmlPage);
        outputToBrowser.flush();
        outputToBrowser.close();
			}

			else if(!aClassified.checkEmail())
			{
				// build Email error page

				StringBuffer htmlPage = new StringBuffer();
				htmlPage.append("<html><head><title>Email Error Page</title></head>");
				htmlPage.append("<body background=../classified/servletimages/newsbg.jpg bgcolor=ff0000>");
				htmlPage.append("<center><h1>Email Field Error Page</h1></center><hr>");
				htmlPage.append("<b>Sorry...</b>The information for the <b>Email</b> field does not ");
				htmlPage.append("appear to be valid. <b>Press the Edit button if</b>");
				htmlPage.append("<b> you wish to correct the information you provided.</b>");
				htmlPage.append(" Make sure you use the correct format <b>name@somewhere.com</b>");

				// Put back button in cell or it will not show up
				htmlPage.append("<div align=\"right\"><TABLE BORDER=\"1\" WIDTH=\"7%\"><TR><TD WIDTH=\"100%\" BGCOLOR=\"silver\">");
				htmlPage.append("<ALIGN=\"CENTER\"><A HREF=# ONCLICK=\"history.go(-1)\"><FONT COLOR=\"black\"><b>Edit</b></FONT></A>");
				htmlPage.append("</TD></TR></TABLE></DIV>");

				htmlPage.append("<hr>");
				htmlPage.append(aClassified.toWebString());
				htmlPage.append("<center><a href=/classified/index.jsp>Return to Home Page</a>");
				htmlPage.append("<p><i>" + this.getServletInfo() + "</i>");
				htmlPage.append("</center></body></html>");

        // now let's send this dynamic data
        // back to the browser

        response.setContentType("text/html");
        outputToBrowser.println(htmlPage);
        outputToBrowser.flush();
        outputToBrowser.close();
			}

			else if(!aClassified.checkPhone())
			{
				// build Phone error page

				StringBuffer htmlPage = new StringBuffer();
				htmlPage.append("<html><head><title>Phone Error Page</title></head>");
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
				htmlPage.append("<ALIGN=\"CENTER\"><A HREF=# ONCLICK=\"history.go(-1)\"><FONT COLOR=\"black\"><b>Edit</b></FONT></A>");
				htmlPage.append("</TD></TR></TABLE></DIV>");

				htmlPage.append("<hr>");
				htmlPage.append(aClassified.toWebString());
				htmlPage.append("<center><a href=/classified/index.jsp>Return to Home Page</a>");
				htmlPage.append("<p><i>" + this.getServletInfo() + "</i>");
				htmlPage.append("</center></body></html>");
				
        // now let's send this dynamic data
        // back to the browser

        response.setContentType("text/html");
        outputToBrowser.println(htmlPage);
        outputToBrowser.flush();
        outputToBrowser.close();
			}


			else if(!aClassified.checkStartDate(request, response))
			{
				// build Start Date error page

				StringBuffer htmlPage = new StringBuffer();
				htmlPage.append("<html><head><title>Start Date Error Page</title></head>");
				htmlPage.append("<body background=../classified/servletimages/newsbg.jpg bgcolor=ff0000>");
				htmlPage.append("<center><h1>Start Date Field Error Page</h1></center><hr>");
				htmlPage.append("<b>Sorry...</b>The information for the <b>Start Date</b> field does not ");
				htmlPage.append("appear to be valid, the start date must be <b>THREE TO FIVE DAYS FROM</b> ");
				htmlPage.append("<b>TODAYS DATE</b> for context & check validation! For example, if ");
				htmlPage.append("todays date is: 01/05/99...valid dates are from 01/08/99 to 01/12/99 ");
				htmlPage.append("<b>Press the Edit button if</b> you wish to correct the information.</b> ");
				htmlPage.append("Make sure you use this exact format<b> MM/dd/yy</b> ");

				// Put back button in cell or it will not show up
				htmlPage.append("<div align=\"right\"><TABLE BORDER=\"1\" WIDTH=\"7%\"><TR><TD WIDTH=\"100%\" BGCOLOR=\"silver\">");
				htmlPage.append("<ALIGN=\"CENTER\"><A HREF=# ONCLICK=\"history.go(-1)\"><FONT COLOR=\"black\"><b>Edit</b></FONT></A>");
				htmlPage.append("</TD></TR></TABLE></DIV>");

				htmlPage.append("<hr>");
				htmlPage.append(aClassified.toWebString());
				htmlPage.append("<center><a href=/classified/index.jsp>Return to Home Page</a>");
				htmlPage.append("<p><i>" + this.getServletInfo() + "</i>");
				htmlPage.append("</center></body></html>");

        // now let's send this dynamic data
        // back to the browser

        response.setContentType("text/html");
        outputToBrowser.println(htmlPage);
				outputToBrowser.flush();
        outputToBrowser.close();
			}


			else if(!aClassified.checkAmtForDayMoYr())
			{
				// build Number Of Days error page

				StringBuffer htmlPage = new StringBuffer();
				htmlPage.append("<html><head><title>Number Of Days Error Page</title></head>");
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
				htmlPage.append("<center><a href=/classified/index.jsp>Return to Home Page</a>");
				htmlPage.append("<p><i>" + this.getServletInfo() + "</i>");
				htmlPage.append("</center></body></html>");

        // now let's send this dynamic data
        // back to the browser

        response.setContentType("text/html");
        outputToBrowser.println(htmlPage);
				outputToBrowser.flush();
        outputToBrowser.close();
			}


			else if(!aClassified.checkWhatAdSays01())
			{
				// build Check What Ad Says error page

				StringBuffer htmlPage = new StringBuffer();
				htmlPage.append("<html><head><title>What Ad Says Error Page</title></head>");
				htmlPage.append("<body background=../classified/servletimages/newsbg.jpg bgcolor=ff0000>");
				htmlPage.append("<center><h1>What Ad Says Error Page</h1></center><hr>");
				htmlPage.append("<b>Sorry...</b>The information for the <b>What Ad Says</b> field does not ");
				htmlPage.append("appear to be valid. <b>Press the Edit button if</b>");
				htmlPage.append("<b> you wish to correct the information you provided.</b>");
				htmlPage.append(" Make sure you <b> have at least ten characters in your ad</b>");

				// Put back button in cell or it will not show up
				htmlPage.append("<div align=\"right\"><TABLE BORDER=\"1\" WIDTH=\"7%\"><TR><TD WIDTH=\"100%\" BGCOLOR=\"silver\">");
				htmlPage.append("<ALIGN=\"CENTER\"><A HREF=# ONCLICK=\"history.go(-1)\"><FONT COLOR=\"black\"><b>Edit</b></FONT></A>");
				htmlPage.append("</TD></TR></TABLE></DIV>");
				htmlPage.append("<hr>");
				htmlPage.append(aClassified.toWebString());
				htmlPage.append("<center><a href=/classified/index.jsp>Return to Home Page</a>");
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
    		catch (Exception e) {
	    		System.out.println(" 2-DID IT GET HERE? "); //***************** NO
            		cleanUp();
            		e.printStackTrace();
    		}	
	}
			

	 // Execute sql if "Submit" button for Step2 is pressed otherwise user
	 // must click the back button on the browser to correct info.

	

	 if (action.equals("step2")){ 
		try {

	//// Put a cookie in /////

	// Check if the cookie for BLUEADS is there and if it is,
	// is it same date as today or null. If not the later, create a new cookie.
	String cookieName = null;
	String value = null;
	boolean beenHereBefore = false;
   	Cookie[] blueadsCookie = request.getCookies();

	   if ((blueadsCookie != null) && (action.equals("step2"))) {
      for (int i =0; i < blueadsCookie.length; i++) {
        value = blueadsCookie[i].getValue();
			
        if ((blueadsCookie[i].getName().equals("BLUEADS")) && (value.equals(formatter1.format(todaysDate)))) {
				beenHereBefore = true;
        }
      }
	   }
//////////////////////////////////////////////////////////////
// TOM UNCOMMENT ALL LINE BELOW TO GET COOKIES TO WORK AGAIN!!
//	   if (beenHereBefore == true){
//		response.sendRedirect("/Notice.html");
//		return;
//	   }
//	   else{
//
//		// Put in new cookie with todays date.  // Name    // Value
//  		Cookie newBlueadsCookie = new Cookie ("BLUEADS", formatter1.format(todaysDate));
//		// Cookie expires in 24 hours. // 60 sec * 60 min * 24 hrs = 86400 seconds
//		newBlueadsCookie.setMaxAge (60 * 60 * 24); 
//		newBlueadsCookie.setPath ("/");
//		newBlueadsCookie.setSecure(false); // Change to true for SSL.
//  		response.addCookie (newBlueadsCookie);
//		// FOR TESTSystem.out.println("New BLUEADS Cookie = " + newBlueadsCookie + " New Date " + formatter1.format(todaysDate));
//	   }		
//		
////////////////////////// UN-COMMENT UP TO HERE ONLY!


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
		String whatAdSays01 = request.getParameter("WhatAdSays01");
		String webSite = request.getParameter("WebSite");
		String uniqueDateId =  request.getParameter("UniqueDateId");

		// Execute the SQL query and commit the statement.
   	registerStatementCustInfo.executeUpdate();
    registerStatementAdInfo.executeUpdate();
		/////dbConnection.commit();// weblogic pooling does not like.
		System.out.println(" Submit Order Finilized ");

            	// build confirmation page
		StringBuffer htmlPage = new StringBuffer();
		htmlPage.append("<html><head><title>Confirmation Page</title></head>");
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
		htmlPage.append("<li><B>Color:</B> ").append(color).append("COLOR</FONT>");
		htmlPage.append("<li><B>Banner:</B> ").append(banner);
		htmlPage.append("<li><B>What Ad Says:</B> ");
		htmlPage.append("<li></li>").append(whatAdSays01);
		htmlPage.append("<li><B>WebSite:</B> ").append(webSite);
		htmlPage.append("<li><B>Ad I.D. Number: </B>").append(uniqueDateId);
		htmlPage.append("</ul>");

		// Put in a back button so it is visible in IE & Netscape.
		htmlPage.append("<div align=\"right\">");
		htmlPage.append("<FONT SIZE=\"1\"><a href=/SubmitNow.html>Click here if \"Back\" button does not work.</a></FONT>");
		htmlPage.append("<P><TABLE BORDER=\"1\" WIDTH=\"7%\"><TR><TD WIDTH=\"100%\" BGCOLOR=\"silver\">");
		htmlPage.append("<P ALIGN=\"CENTER\"><A HREF=# ONCLICK=\"history.go(-2)\"><FONT COLOR=\"black\">Back</FONT></A>");
		htmlPage.append("</TD></TR></TABLE></DIV>");

		htmlPage.append("<hr>");
		htmlPage.append("<div align=\"left\"><b>Please Note: blueads.com reserves the right to refuse any ad. </b><br>");
		htmlPage.append("<b>Print a copy of this page so you have one for your records or </b><br>");
		htmlPage.append("<b>if you included your email address, a confirmation will be sent </b><br>");
		htmlPage.append("<b>to you with your 12-Digit Ad I.D. Number.  Make sure you save </b><br>");
		htmlPage.append("<b>the Ad I.D. Number so you can refer to it if you want to delete </b><br>");
		htmlPage.append("<b>your ad. </b><br><br>");
		//htmlPage.append("<address><li><b>blueads.com</b><br>");
		//htmlPage.append("<li><b>3203 S. 116 Ave.</b><br>");
		//htmlPage.append("<li><b>Omaha, NE 68144</b><br>");
		//htmlPage.append("</address>");
		htmlPage.append("<hr>");
		htmlPage.append("<center><b><a href=/classified/index.jsp>Click Here To Continue</a></b>");
		//htmlPage.append("<a href=/servlet/SubmitNowDBServlet> | View Classified List</a>");
		htmlPage.append("<p><i>").append(this.getServletInfo()).append("</i>");
		htmlPage.append("</center></body></html>");

    // now let's send this dynamic data
	  // back to the browser

		outputToBrowser =  new PrintWriter(response.getOutputStream());

		// now let's send this dynamic data
		// back to the browser

		// Set so page is not saved
		response.setHeader("Expires", "Mon, 01 Jan 2090 06:00:01 GMT");
		//response.setHeader("Cache-Control", "no-cache");
		//response.setHeader("Pragma", "no-cache");

		response.setContentType("text/html");
		outputToBrowser.println(htmlPage);
		outputToBrowser.flush();
		outputToBrowser.close();

		///////////////////////////// Mail configuration //////////////////////////

			//Compiler.disable();  //turn off JIT so we can see line numbers when debugging
		
			// Now send e-mail with Ad i.d. number
			if(!(email.equals("")) && (action.equals("step2"))){
				
				String msgText = "Thank you for using http://www.blueads.com.\n" +
				"\tWe are commited to keeping our customers satified." +
				" Here is your ad id number: " + uniqueDateId + ". Use this number if" +
				" your item(s) or service(s) have accomplished there results" + 
				" and you would like to delete your ad so you are no longer contacted." +
				" Your ad will run from " + startDate + " to " + endDate + ".\n" +
				"Your Ad Reads As Follows:\n" + 
				whatAdSays01 + "\n";
				
				/////String to = "blue-j@worldnet.att.net";
				String from = "blueads.com";

				String host = "mailhost.worldnet.att.net";
				//String host = "mail.nfinity.com"; 

				boolean debug = Boolean.valueOf("true").booleanValue(); // Change to false to turn off debug.
	
				// create some properties and get the default Session
				Properties props = new Properties();
				props.put("mail.smtp.host", host);

				if (debug) props.put("mail.debug", "true"); // Change to false to turn off debug.

				javax.mail.Session emailSession = javax.mail.Session.getDefaultInstance(props, null);
				emailSession.setDebug(debug);

				try {
				    // create a message
				    Message msg = new MimeMessage(emailSession);
				    msg.setFrom(new InternetAddress(from));
				    //InternetAddress[] emailAddress = {new InternetAddress(to)};
				    InternetAddress[] emailAddress = {new InternetAddress(email)};
				    msg.setRecipients(Message.RecipientType.TO, emailAddress);
				    msg.setSubject("blueads.com i.d. number");
				    msg.setSentDate(new java.util.Date());
				    // If the desired charset is known, you can use
				    // setText(text, charset)
				    msg.setText(msgText);	    
				    Transport.send(msg);
				} catch (MessagingException mex) {
				    System.out.println("\n--Exception Submit's Email");
					System.out.println("Email is = to " + email);
				}					
			}			
		////////////////////////// End Mail configuration //////////////////////////

		return;
		}
		catch (Exception e) {
			// Any error is grounds for rollback
			try{
				dbConnection.rollback();
			}
			catch(Exception ignored){}
			System.out.println(" 2-DID IT GET HERE? ");
    	        	cleanUp();
        	    	e.printStackTrace();
    		}

          // put a finally clause here to release connection pool p269 Jason H.
          finally {
              if (dbConnection != null)
            try { 
              // 1/23/03  registerStatementCustInfo.close();
              // 1/23/03 registerStatementAdInfo.close();
              dbConnection.close();   
            } 
            catch (SQLException sqe)  {}		
          }
	
   	 }
    } 
   }

	///////////////////return;

	
    public void cleanUp()
    {
        try {
            System.out.println(" 3-DID IT GET HERE? "); //*****************
            System.out.println("Closing database connection");
            dbConnection.close();
            ///ds.releaseConnection(dbConnection);
            
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }
/**
    public void destroy()
    {
        System.out.println("SubmitNowDBServlet: destroy");
        cleanUp();
    }
*/
    public String getServletInfo()
    {
        return "<i>blueads.com, v.02</i>";
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
				return true;
			}
	}

}
